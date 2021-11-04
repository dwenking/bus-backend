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
                System.out.println("value: " + value);

                // 类型转换
                station.setLines((List<String>) (List) value.asList());
            }

            if (station.getLines() != null && !station.getLines().equals("")) {
                alllist.add(station);
            }
        }
        return alllist;
    }

    @Override
    public List<String> getAllStationFromVagueName(String stationName) {
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

    //找到两个相邻站之间路径最多的两个站及数量
    @Override
    public List<Map<String, String>> findTop15StationPairs() {
        List<Map<String, String>> mapList = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            // 找到 start-end-count对应
            String cypher = String.format("MATCH (n:Station)-[r]->(m:Station) " +
                    "with n.name as start, m.name as end,count(r) as number order by number DESC limit 15 return start,end,number");
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                //System.out.println(record);
                //将records映射为map对象
                Map<String, Object> objectMap = record.asMap();
                //System.out.println(record.asMap());
                Map<String, String> map = new HashMap<>();
                for (String cur : objectMap.keySet()) {
                    map.put(cur, objectMap.get(cur).toString());
                }
                mapList.add(map);
            }
        }

        return mapList;
    }

}
