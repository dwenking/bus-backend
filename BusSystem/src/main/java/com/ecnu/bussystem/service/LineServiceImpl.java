package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
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
    public Line findRouteByPerciseName(String routeName) {
        return lineRepository.findRouteByPerciseName(routeName);
    }

    @Override
    public List<Line> findRouteByVagueName(String routeName) {
        if (routeName.length() >= 2) {
            String substring = routeName.substring(routeName.length() - 2, routeName.length());
            if (routeName.length() >= 2 && (substring.equals("上行") || substring.equals("下行"))) {
                routeName = routeName.substring(0, routeName.length() - 2);
            }
        }
        String routename1 = routeName + "上行";
        String routename2 = routeName + "下行";
        Line line1 = lineRepository.findRouteByPerciseName(routename1);
        Line line2 = lineRepository.findRouteByPerciseName(routename2);
        List<Line> lines = new ArrayList<>();
        if (line1 != null) lines.add(line1);
        if (line2 != null) lines.add(line2);
        return lines;
    }


    @Override
    public StationLine findStationlineByPreciseRouteName(String routeName) {
        StationLine stationLine = new StationLine();
        List<Station> stations = new ArrayList<>();
        stationLine.setName(routeName);
        Station beginStation = null;
        Station endStation = null;
        Line lineNode = null;
        //找到begin的node
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("MATCH (n:vStations)-[r]-(m:vLines) where type(r)='begin'and m.name='%s' return n", routeName);
            Result result = session.run(cypher);
            try {
                List<Record> records = result.list();
                for (Record record : records) {
                    Value value = record.get("n");
                    Map<String, Object> map = value.asNode().asMap();
                    String mapString = JSONObject.toJSONString(map);
                    beginStation = JSONObject.parseObject(mapString, Station.class); //json字符串直接转给java对象
                }
            } catch (Exception e) {
                System.out.println("没有找到BeginNode, name:" + routeName);
            }
        }
        //找到end的node
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("MATCH (n:vStations)-[r]-(m:vLines) where type(r)='end'and m.name='%s' return n", routeName);
            Result result = session.run(cypher);
            try {
                List<Record> records = result.list();
                for (Record record : records) {
                    Value value = record.get("n");
                    Map<String, Object> map = value.asNode().asMap();
                    String mapString = JSONObject.toJSONString(map);
                    endStation = JSONObject.parseObject(mapString, Station.class); //json字符串直接转给java对象
                }
            } catch (Exception e) {
                System.out.println("没有找到endNode, name:" + routeName);
            }
        }
        //找到LineNode
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("MATCH (n:vLines) where n.name='%s' return n", routeName);
            Result result = session.run(cypher);
            try {
                List<Record> records = result.list();
                for (Record record : records) {
                    Value value = record.get("n");
                    Map<String, Object> map = value.asNode().asMap();
                    String mapString = JSONObject.toJSONString(map);
                    lineNode = JSONObject.parseObject(mapString, Line.class); //json字符串直接转给java对象
                }
            } catch (Exception e) {
                System.out.println("没有找到lineNode, name:" + routeName);
            }
        }
        if (beginStation == null || endStation == null || lineNode == null)
            return null;

        //根据begin和end找到路线
        try (Session session = neo4jDriver.session()) {

            String cypher = String.format("MATCH p=(s:vStations)-[r *.. {name:'%s'}]->(e:vStations) where '%s' =s.myId and '%s'=e.myId RETURN p order by length(p) desc", routeName, beginStation.getMyId(), endStation.getMyId());
            Result result = session.run(cypher);
            try {
                List<Record> records = result.list();
                Record record = null;
                if (records != null) {
                    record = records.get(0);
                }
                assert record != null;
                Value value = record.get("p");  // 因为是return p
                Path path = value.asPath();  // 返回的是路径类型，故使用.asPath()
                // 得到node结果后，类型转换并加入line的station list
                for (Node node : path.nodes()) {
                    Map<String, Object> map = node.asMap();
                    String mapString = JSONObject.toJSONString(map);
                    Station station = JSONObject.parseObject(mapString, Station.class); //json字符串直接转给java对象
                    stations.add(station);
                }
            } catch (Exception e) {
                System.out.println("没有找到Record, name:" + routeName);
            }
        }
        stationLine.setStations(stations);
        assert lineNode != null;
        stationLine.setDirectional(lineNode.getDirectional());
        return stationLine;
    }

    @Override
    public List<StationLine> findStationlineByVagueRouteName(String routeName) {
        List<StationLine> stationLines = new ArrayList<>();
        if (routeName.length() >= 2) {
            String substring = routeName.substring(routeName.length() - 2, routeName.length());
            if ((substring.equals("上行") || substring.equals("下行"))) {
                routeName = routeName.substring(0, routeName.length() - 2);
            }
        }

        System.out.println(routeName);
        String routename1 = routeName + "上行";
        String routename2 = routeName + "下行";

        StationLine stationLine = this.findStationlineByPreciseRouteName(routeName);
        StationLine stationLine1 = this.findStationlineByPreciseRouteName(routename1);
        StationLine stationLine2 = this.findStationlineByPreciseRouteName(routename2);
        if (stationLine1 != null) stationLines.add(stationLine1);
        if (stationLine2 != null) stationLines.add(stationLine2);
        if (stationLine != null) stationLines.add(stationLine);
        return stationLines;
    }
}
