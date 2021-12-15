package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.Neo4jUtil;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.respository.StationRepository;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StationServiceImpl implements StationService {
    @Autowired
    StationRepository stationRepository;

    @Resource
    Driver neo4jDriver;

    @Override
    public Station findStationById(String Id) {
        return stationRepository.findStationById(Id);
    }

    @Override
    public List<Station> findStationByPreciseName(String stationName) {
        return stationRepository.findStationByName(stationName);
    }

    @Override
    public List<Station> findStationByVagueName(String stationName) {
        List<String> stations = getAllStationFromVagueName(stationName);
        List<Station> alllist = new ArrayList<>();

        for (String station : stations) {
            List<Station> stationList = stationRepository.findStationByName(station);
            if (stationList != null) {
                alllist.addAll(stationList);
            }
        }
        return alllist;
    }

    @Override
    public List<Station> findLineOfStationByVagueName(String stationName) {
        List<Station> allList;

        try (Session session = neo4jDriver.session()) {
            // 找到stationName对应的id-node
            String cypher = String.format("MATCH (m:vNames)-[]-(n:vStations) WHERE m.name='%s' RETURN n", stationName);
            Result result = session.run(cypher);

            List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
            allList = generateLineOfStationFromJson(session, mapStrings);

            // 在name表里查询失败，改成在station里查询
            if (mapStrings.size() == 0 || mapStrings == null) {
                cypher = String.format("MATCH (n:vStations) WHERE n.name='%s' RETURN n", stationName);
                result = session.run(cypher);

                mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                allList = generateLineOfStationFromJson(session, mapStrings);
            }
        }
        return allList;
    }

    private List<Station> generateLineOfStationFromJson(Session session, List<String> mapStrings) {
        List<Station> alllist = new ArrayList<>();
        String cypher;
        Result result;

        // 遍历每个station并找到对应line
        for (String mapString : mapStrings) {
            Station station = JSONObject.parseObject(mapString, Station.class);

            // 根据id-node查找对应lines, 并存储在station的lines成员变量
            cypher = String.format("MATCH (m:vStations)-[]-(n:vLines) WHERE m.myId='%s' RETURN COLLECT(n.name)", station.getMyId());
            result = session.run(cypher);

            List<Record> records = result.list();
            for (Record record : records) {
                Value value = record.get("COLLECT(n.name)");
                // 类型转换
                station.setLines((List<String>) (List) value.asList());
            }

            if (station.getLines() != null && !station.getLines().equals("")) {
                alllist.add(station);
            }
        }
        return alllist;
    }

    private List<String> getAllStationFromVagueName(String stationName) {
        List<String> stations = new ArrayList<>();
        String station1;
        String station2;
        String station3;

        if (stationName.length() >= 5 && (
                stationName.endsWith("(首发站)") || stationName.endsWith("(终点站)"))) {
            String substring = stationName.substring(0, stationName.length() - 5);
            station1 = substring;
            station2 = substring + "(首发站)";
            station3 = substring + "(终点站)";
        } else {
            station1 = stationName;
            station2 = stationName + "(首发站)";
            station3 = stationName + "(终点站)";
        }
        stations.add(station1);
        stations.add(station2);
        stations.add(station3);
        return stations;
    }

    @Override
    public Station findLineOfStationById(String stationId) {
        List<Station> alllist;
        try (Session session = neo4jDriver.session()) {
            // 找到stationId对应的node
            String cypher = String.format("MATCH (n:vStations) WHERE n.myId='%s' RETURN n", stationId);
            Result result = session.run(cypher);

            List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
            alllist = generateLineOfStationFromJson(session, mapStrings);

            if (alllist == null || alllist.size() < 1) {
                return null;
            }
        }
        return alllist.get(0);
    }

    @Override
    public List<Map<String, Object>> findTop15StationPairs() {
        List<Map<String, Object>> resMap = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("MATCH (n:vStations)-[r]->(m:vStations) " +
                    "with n as station1, m as station2,count(r) as number " +
                    "order by number DESC limit 15 return station1,station2,number");
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                Value value = record.get("station1");
                Value value2 = record.get("station2");
                Map<String, Object> map = value.asNode().asMap();
                Map<String, Object> map2 = value2.asNode().asMap();

                Map<String, Object> listMap = new HashMap<>();
                listMap.put("station1", map);
                listMap.put("station2", map2);
                listMap.put("number", record.get("number").toString());
                resMap.add(listMap);
            }
        }
        return resMap;
    }

    @Override
    public List<Map<String, Object>> findTop15LineNumberofStations() {

        List<Map<String, Object>> mapList = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            // 找到 start-end-count对应
            String cypher = String.format("match (n:vStations)-[]->(m:vLines)\n" +
                    "with n.myId as id,n as stationNode,collect(distinct m.name) as lineList,count(m.name) as lineNumber\n" +
                    "return id, stationNode,lineList,lineNumber order by lineNumber desc limit 15");
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                //将records映射为map对象，listMap是存储的结果集合
                Map<String, Object> listMap = new HashMap<>();
                //取出record中的节点node
                Value value = record.get("stationNode");
                Map<String, Object> map = value.asNode().asMap();
                String nodeString = JSONObject.toJSONString(map);
                Station station = JSONObject.parseObject(nodeString, Station.class);
                listMap.put("stationNode", station);

                Map<String, Object> objectMap = record.asMap();
                for (String key : objectMap.keySet()) {
                    if (key.equals("lineList")) {
                        String lines = JSONObject.toJSONString(objectMap.get(key));
                        List<String> lineslist = JSONObject.parseObject(lines, List.class);
                        listMap.put(key, lineslist);
                    } else if (key.equals("lineNumber")) {
                        Long lineNumber = (Long) objectMap.get(key);
                        listMap.put(key, lineNumber);
                    }
                }
                mapList.add(listMap);
            }
        }

        return mapList;
    }
    //返回地铁站的数量

    @Override
    public List<String> findNumberOfMetroStations() {
        List<String> stringList = stationRepository.findMetroStations();
        if (stringList == null || stringList.size() == 0) {
            return null;
        }
        return stringList;
    }

    @Override
    public List<String> findNumberOfBeginStations() {
        List<String> stringList = stationRepository.findBeginStations();
        if (stringList == null || stringList.size() == 0) {
            return null;
        }
        return stringList;
    }

    @Override
    public List<String> findNumberOfEndStations() {
        List<String> stringList = stationRepository.findEndStations();
        if (stringList == null || stringList.size() == 0) {
            return null;
        }
        return stringList;
    }

    @Override
    public JSONObject createNewStations(List<Station> stationList){
        JSONObject res = new JSONObject();
        try (Session session = neo4jDriver.session()) {
            String lineName = stationList.get(0).getName(); //取线路名
            // 根据站点名称创建vStation节点
            for (int i=1;i<stationList.size();i++){
                String cypher = String.format("CREATE (n:vStations \n" +
                        "{name:'%s', englishname:'default',myId:'%s'," +
                        "type:'normal'})\n" +
                        "return n",stationList.get(i).getName(),stationList.get(i).getMyId());
                Result result = session.run(cypher);
            }

            // 对首尾vStation节点添加线路关系
            String cypher = String.format("MATCH(a:vLines),(b:vStations)" +
                    " WHERE a.name='%s' AND b.name='%s' " +
                    "CREATE (b)-[r:begin]->(a) return r",lineName,stationList.get(1).getName());
            Result result = session.run(cypher);

            cypher = String.format("MATCH(a:vLines),(b:vStations)" +
                    " WHERE a.name='%s' AND b.name='%s' " +
                    "CREATE (b)-[r:end]->(a) return r",lineName,stationList.get(stationList.size()-1).getName());
            result = session.run(cypher);

            //对中间节点添加线路关系
            for (int i=2;i<stationList.size()-1;i++){
                 cypher = String.format("MATCH(a:vLines),(b:vStations)" +
                         " WHERE a.name='%s' AND b.name='%s' " +
                         "CREATE (b)-[r:in]->(a) return r",lineName,stationList.get(i).getName());
                 result = session.run(cypher);
            }

            //对中间节点添加vNear关系
            for (int i=1;i<stationList.size()-1;i++){

                cypher = String.format("MATCH(a:vStations),(b:vStations) WHERE " +
                        "a.name='%s' AND b.name='%s' " +
                        "CREATE (a)-[r:vNear { name:'%s' }]->(b) return r",
                        stationList.get(i).getName(),stationList.get(i+1).getName(),lineName);

                result = session.run(cypher);
            }
            // 根据站点名称创建vName节点和关系
            for (int i=1;i<stationList.size();i++){
                 cypher = String.format("CREATE (n:vNames \n" +
                        "{name:'%s'})\n" +
                        "return n",stationList.get(i).getName());
                 result = session.run(cypher);
                 cypher = String.format("MATCH(a:vNames),(b:vStations)" +
                        " WHERE a.name='%s' AND b.name='%s' " +
                        "CREATE (b)-[r:in]->(a) return r",stationList.get(i).getName(),stationList.get(i).getName());
                 result = session.run(cypher);
            }

            res.put("stationList",stationList);
        }
        return res;
    }
}
