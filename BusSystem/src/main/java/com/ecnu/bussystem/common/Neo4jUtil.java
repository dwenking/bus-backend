package com.ecnu.bussystem.common;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationPath;
import com.ecnu.bussystem.entity.StationRelationship;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jUtil {
    public static List<String> getJsonStringFromNodeResult(Result result) {
        List<Record> records = result.list();
        List<String> mapStrings = new ArrayList<>();

        for (Record record : records) {
            Value value = record.get("n");
            Map<String, Object> map = value.asNode().asMap();

            String mapString = JSONObject.toJSONString(map);
            if (mapString != null && !mapString.equals("")) {
                mapStrings.add(mapString);
            }
        }
        return mapStrings;
    }

    /**
     * 确保是return p的情况
     *
     * @param result 结果
     * @return {@code List<String>}
     */
    public static List<String> getJsonStringFromPathResult(Result result) {
        List<Record> records = result.list();
        List<String> nodeMapStrings = new ArrayList<>();
        Record record = null;

        if (records != null) {
            record = records.get(0);
        }
        else {
            return nodeMapStrings;
        }

        // 因为是return p
        Value value = record.get("p");
        Path path = value.asPath();
        // 得到node结果后，类型转换并加入line的station list
        for (Node node : path.nodes()) {
            Map<String, Object> map = node.asMap();
            String mapString = JSONObject.toJSONString(map);
            if (mapString != null && !mapString.equals("")) {
                nodeMapStrings.add(mapString);
            }
        }
        return nodeMapStrings;
    }

    /**
     * 将neo4j中的Node转换为Station实体
     *
     * @param node 节点
     * @return {@code Station}
     */
    public static Station getStationFromNode(Node node){
        Station station=null;
        try{
            Map<String, Object> map = node.asMap();
            String mapString = JSONObject.toJSONString(map);
            station = JSONObject.parseObject(mapString, Station.class);
        }
        catch (Exception e){
            System.out.println("无法将该Node转换为Station");
            return null;
        }
        return station;
    }

    /**
     * 从neo4j的Relationship映射到StationRelationship关系实体类
     *
     * @param relationship 关系
     * @return {@code StationRelationship}
     */
    public static StationRelationship getStationRelationshipFromRelationship(Relationship relationship){
        StationRelationship stationRelationship=null;
        try{
            Map<String, Object> map = relationship.asMap();
            String mapString = JSONObject.toJSONString(map);
            stationRelationship = JSONObject.parseObject(mapString, StationRelationship.class);
        }
        catch (Exception e){
            System.out.println("无法将该Relationship转换为StationRelationship");
            return null;
        }
        return stationRelationship;
    }

    /**
     * 从结果中返回stationPath的数组（所有路线）
     *
     * @param result
     * @return {@code List<StationPath>}
     */
    public static List<StationPath> getStationPathFromResult(Result result) {
        List<StationPath> stationPaths=new ArrayList<>();
        List<Record> records = result.list();
        for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            Value value = record.get("p");
            Path path = value.asPath();
            List<Station> thisStations = new ArrayList<>();
            List<StationRelationship> thisStationRelationship = new ArrayList<>();
            int sumTime=0;
            int transferCnt=0;
            String preLineName = new String();
            for (Path.Segment p : path) {
                Station startNode = getStationFromNode(p.start());
                Station endNode = getStationFromNode(p.end());
                StationRelationship stationRelationship = getStationRelationshipFromRelationship(p.relationship());
                if (startNode == null || endNode == null || stationRelationship == null) break;
                sumTime+=stationRelationship.getTime();
                if (thisStations.size() == 0) {
                    thisStations.add(startNode);
                    thisStations.add(endNode);
                    thisStationRelationship.add(stationRelationship);
                    preLineName=stationRelationship.getName();
                } else {
                    if(!preLineName.equals(stationRelationship.getName())){
                        preLineName=new String(stationRelationship.getName());
                        transferCnt++;
                    }
                    thisStations.add(endNode);
                    thisStationRelationship.add(stationRelationship);
                }
            }
            if (thisStationRelationship.size()!=0) {
                stationPaths.add(new StationPath(thisStationRelationship.size(),sumTime,transferCnt,new ArrayList<>(thisStations),new ArrayList<>(thisStationRelationship)));
            }
        }
        return stationPaths;
    }
}
