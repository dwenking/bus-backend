package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.Neo4jUtil;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.respository.LineRepository;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LineServiceImpl implements LineService {
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
        }
        else if (routeName.matches(regex)){
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
        }
        else if (routeName.endsWith("上行") || routeName.endsWith("下行")) {
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
}
