package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.Neo4jUtil;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.respository.LineRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import io.swagger.models.auth.In;
import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.SpringDataMongoDB;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class LineServiceImpl implements LineService {
    @Autowired
    StationServiceImpl stationService;

    @Autowired
    LineRepository lineRepository;

    @Resource
    Driver neo4jDriver;

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Line findLineByPerciseName(String routeName) {
        return lineRepository.findLineByPerciseName(routeName);
    }

    @Override
    public List<Line> findLineByVagueName(String routeName) {
        if (routeName == null || routeName.equals("")) {
            return null;
        }

        String regex = "^[a-z0-9A-Z]+$";
        if (routeName.endsWith("上行") || routeName.endsWith("下行")) {
            routeName = routeName.substring(0, routeName.length() - 2);
        } else if (routeName.matches(regex)) {
            routeName += "路";
        }

        String routename1 = routeName + "上行";
        String routename2 = routeName + "下行";

        Line line1 = lineRepository.findLineByPerciseName(routename1);
        Line line2 = lineRepository.findLineByPerciseName(routename2);
        List<Line> lines = new ArrayList<>();

        if (line1 != null) {
            lines.add(line1);
        }
        if (line2 != null) {
            lines.add(line2);
        }

        return lines;
    }

    @Override
    public StationLine findStationOfLineByPreciseName(String routeName) {
        if (routeName == null || routeName.equals("")) {
            return null;
        }

        StationLine stationLine = new StationLine();
        stationLine.setName(routeName);

        List<Station> stations = new ArrayList<>();
        Station beginStation = null;
        Station endStation = null;
        Line lineNode = null;

        try (Session session = neo4jDriver.session()) {
            // 找到begin的node
            String cypher = String.format("MATCH (n:vStations)-[r]-(m:vLines) WHERE type(r)='begin' AND m.name='%s' RETURN n", routeName);
            Result result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                beginStation = JSONObject.parseObject(mapStrings.get(0), Station.class);
            } catch (Exception e) {
                System.out.println("没有找到BeginNode, name:" + routeName);
                return stationLine;
            }

            // 找到end的node
            cypher = String.format("MATCH (n:vStations)-[r]-(m:vLines) WHERE type(r)='end' AND m.name='%s' RETURN n", routeName);
            result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                endStation = JSONObject.parseObject(mapStrings.get(0), Station.class);
            } catch (Exception e) {
                System.out.println("没有找到endNode, name:" + routeName);
                return stationLine;
            }

            // 确定线路
            cypher = String.format("MATCH (n:vLines) WHERE n.name='%s' RETURN n", routeName);
            result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                lineNode = JSONObject.parseObject(mapStrings.get(0), Line.class);
            } catch (Exception e) {
                System.out.println("没有找到lineNode, name:" + routeName);
                return stationLine;
            }

            //根据begin和end找路线
            cypher = String.format("MATCH p=(s:vStations)-[r *.. {name:'%s'}]->(e:vStations) WHERE '%s' = s.myId AND '%s'= e.myId RETURN p ORDER BY length(p) DESC", routeName, beginStation.getMyId(), endStation.getMyId());
            result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromPathResult(result);
                for (String string : mapStrings) {
                    Station station = JSONObject.parseObject(string, Station.class); //json字符串直接转给java对象
                    stations.add(station);
                }
            } catch (Exception e) {
                System.out.println("没有找到Record, name:" + routeName);
                return stationLine;
            }
        }

        stationLine.setStations(stations);
        stationLine.setDirectional(lineNode.getDirectional());
        return stationLine;
    }

    @Override
    public List<StationLine> findStationOfLineByVagueName(String routeName) {
        if (routeName == null || routeName.equals("")) {
            return null;
        }

        List<StationLine> stationLines = new ArrayList<>();
        String regex = "^[a-z0-9A-Z]+$";
        if (routeName.matches(regex)) {
            routeName += "路";
        } else if (routeName.endsWith("上行") || routeName.endsWith("下行")) {
            routeName = routeName.substring(0, routeName.length() - 2);
        }

        String routename1 = routeName + "上行";
        String routename2 = routeName + "下行";

        StationLine stationLine = this.findStationOfLineByPreciseName(routeName);
        StationLine stationLine1 = this.findStationOfLineByPreciseName(routename1);
        StationLine stationLine2 = this.findStationOfLineByPreciseName(routename2);
        if (stationLine1 != null && stationLine1.isValid()) {
            stationLines.add(stationLine1);
        }
        if (stationLine2 != null && stationLine2.isValid()) {
            stationLines.add(stationLine2);
        }
        if (stationLine != null && stationLine.isValid()) {
            stationLines.add(stationLine);
        }

        return stationLines;
    }

    @Override
    public List<Map<String, String>> findTop15MostStationsRoutes() {
        List<Map<String, String>> mapList = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            // key为routename-number
            String cypher = String.format("MATCH (n:vStations)-[]->(l:vLines) " +
                    "with l.name as routename, count(n.name) as number " +
                    "order by number DESC limit 15 return routename,number");
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                //将records映射为map对象
                Map<String, Object> objectMap = record.asMap();
                Map<String, String> map = new HashMap<>();
                for (String cur : objectMap.keySet()) {
                    map.put(cur, objectMap.get(cur).toString());
                }
                mapList.add(map);
            }
        }
        return mapList;
    }

    @Override
    public List<Map<String, String>> findDuplicateStations(String lineName1, String lineName2) {
        List<Map<String, String>> mapList = new ArrayList<>();
        List<Station> lineStation1 = this.findStationOfLineByPreciseName(lineName1).getStations();
        List<Station> lineStation2 = this.findStationOfLineByPreciseName(lineName2).getStations();
        List<String> stationNames1 = new ArrayList<>();
        List<String> stationNames2 = new ArrayList<>();
        for (Station station : lineStation1) {
            stationNames1.add(station.getName());
        }
        for (Station station : lineStation2) {
            stationNames2.add(station.getName());
        }
        Collection<String> res = CollectionUtils.intersection(stationNames1, stationNames2);
        int cnt = res.size();
        Map<String, String> map = new HashMap<>();
        map.put("line1", lineName1);
        map.put("line2", lineName2);
        map.put("number", String.valueOf(cnt));
        map.put("stations", res.toString());
        mapList.add(map);
        return mapList;
    }

    @Override
    public List<StationLine> findAlongStationLineByStartAndEndName(String name1, String name2, String routename) {
        //首先根据线路模糊查询到每条线路沿线的站
        List<StationLine> stationLineList = new ArrayList<>();
        List<StationLine> stationLines = this.findStationOfLineByVagueName(routename);
        //没有找到相关的路线，返回null
        if (stationLines == null || stationLines.size() == 0) {
            return null;
        }
        //遍历每条线路上的站，判断是否存在name1->name2的路线，如果有则存入stationlines中
        for (int i = 0; i < stationLines.size(); i++) {
            List<Station> stationList = stationLines.get(i).getStations();
            List<Integer> indx1List = new ArrayList<>();
            List<Integer> indx2List = new ArrayList<>();
            //查找一条路线中从name1到name2的路线（防止有多条），直接记录下标计算
            for (int j = 0; j < stationList.size(); j++) {
                String thisStationName = stationList.get(j).getName();
                if (thisStationName.equals(name1) || thisStationName.equals(name1 + "(始发站)") || thisStationName.equals(name1 + "(终点站)")) {
                    indx1List.add(j);
                }
                if (thisStationName.equals(name2) || thisStationName.equals(name2 + "(始发站)") || thisStationName.equals(name2 + "(终点站)")) {
                    indx2List.add(j);
                }
            }
            //没有找到相关的站，之间continue
            if (indx1List.size() == 0 || indx2List.size() == 0) {
                continue;
            }
            //如果存在路线，则生成路线并计算时间
            for (Integer index1 : indx1List) {
                for (Integer index2 : indx2List) {
                    if (index1 < index2) {
                        String id1 = stationList.get(index1).getMyId();
                        String id2 = stationList.get(index2).getMyId();
                        Integer time = lineRepository.findTimebetweenTwoStations(id1, id2, stationLines.get(i).getName());
                        List<Station> alongStations = new ArrayList<>(stationList.subList(index1, index2 + 1));
                        stationLineList.add(new StationLine(stationLines.get(i).getName(), true, alongStations, time));
                    }
                }
            }
        }
        return stationLineList;
    }

    //(具体的直达线路）返回两个站之间是否存在直达线路，返回从name1到name2的线路（！注意：如果是name1到name2则线路的directional设为true，如果是相反，则为false）
    @Override
    public List<StationLine> findDirectPathBetweenTwoStations(String name1, String name2) {
        //存储直达线路的答案
        List<StationLine> DirectPathList = new ArrayList<>();
        //分别找出两个站name的站的集合，并已找到相关的线路，并返回在station中的lines数组中
        List<Station> stationList1 = stationService.findLineOfStationByVagueName(name1);
        List<Station> stationList2 = stationService.findLineOfStationByVagueName(name2);
        if (stationList1 == null || stationList1.size() == 0 || stationList2 == null || stationList2.size() == 0) {
            return null;
        }
        for (Station station1 : stationList1) {
            for (Station station2 : stationList2) {
                List<String> routename1list = station1.getLines();
                List<String> routename2list = station2.getLines();
                if (routename1list == null || routename1list.size() == 0 || routename2list == null || routename2list.size() == 0) {
                    continue;
                }
                List<String> routenameList=new ArrayList<>(CollectionUtils.intersection(routename1list,routename2list));
                //找到两个站的线路重合的线路，并遍历这些线路，找出线路中的两个站，截取直达的线路部分（注意方向）
                for (String routename : routenameList) {
                    StationLine stationLine = this.findStationOfLineByPreciseName(routename);
                    if (stationLine == null) {
                        break;
                    }
                    List<Station> stationList = stationLine.getStations();
                    if (stationList == null || stationList.size() == 0) {
                        break;
                    }
                    //找出该线路下两个站的下标位置
                    Integer indx1 = -1;
                    Integer indx2 = -1;
                    for (int i = 0; i < stationList.size(); i++) {
                        if (stationList.get(i).getMyId().equals(station1.getMyId())) {
                            indx1 = i;
                        }
                        if (stationList.get(i).getMyId().equals(station2.getMyId())) {
                            indx2 = i;
                        }
                    }
                    if (indx1 == -1 || indx2 == -1) {
                        break;
                    }
                    StationLine thisDirectpath = null;
                    if (indx1 < indx2) {
                        thisDirectpath = new StationLine(stationLine.getName(), true, new ArrayList<>(stationList.subList(indx1, indx2 + 1)), 0);
                    } else {
                        thisDirectpath = new StationLine(stationLine.getName(), false, new ArrayList<>(stationList.subList(indx2, indx1 + 1)), 0);
                    }
                    DirectPathList.add(thisDirectpath);
                }
            }
        }
        return DirectPathList;
    }

    @Override
    public List<JSONObject> findDirectPathNameBetweenTwoStations(String name1, String name2) {
        List<StationLine> stationLineList=this.findDirectPathBetweenTwoStations(name1,name2);
        List<JSONObject> objects=new ArrayList<>();
        if(stationLineList==null||stationLineList.size()==0) {
            return null;
        }
        Set<String> stationtruelist=new HashSet<>();
        Set<String> stationfalselist=new HashSet<>();
        for(StationLine stationLine:stationLineList){
            //只有从name1->name2的线路名称需要
            if(stationLine.getDirectional()) {
                stationtruelist.add(stationLine.getName());
            } else {
                stationfalselist.add(stationLine.getName());
            }
        }
        //对所有符合要求的线路名称进行去重，封装为JSONObject
        for(String name:stationtruelist){
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("name",name);
            jsonObject.put("directional",name1+"->"+name2);
            objects.add(jsonObject);
        }
        for(String name:stationfalselist){
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("name",name);
            jsonObject.put("directional",name2+"->"+name1);
            objects.add(jsonObject);
        }
        return objects;
    }
}
