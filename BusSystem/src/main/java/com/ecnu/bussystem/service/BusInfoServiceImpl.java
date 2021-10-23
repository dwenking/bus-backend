package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.respository.StationRespository;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BusInfoServiceImpl implements BusInfoService{
    @Autowired
    StationRespository stationRespository;

    @Resource
    Driver neo4jDriver;

    @Override
    public Station findStationById(String Id) {
        return stationRespository.findStationById(Id);
    }

    @Override
    public Station findStationByName(String stationName) {
        return stationRespository.findStationByName(stationName);
    }

    @Override
    public StationLine findRouteByPerciseName(String routeName) {
        StationLine stationLine = new StationLine();
        List<Station> stations = new ArrayList<>();

        stationLine.setName(routeName);

        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("MATCH p=(s)-[r *.. {name:'%s'}]->(e) where '%s' in s.begins and '%s' in e.ends RETURN p", routeName, routeName, routeName);
            Result result = session.run(cypher);

            try {
                List<Record> records = result.list();
                Record record = null;

                if (records != null) {
                    record = records.get(0);
                }

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

        return stationLine;
    }

    @Override
    public List<StationLine> findRouteByVagueName(String routeVagueName) {
        StationLine tmp;
        List<StationLine> stationLines = new ArrayList<>();

        tmp = findRouteByPerciseName(routeVagueName + "上行");
        if (tmp.getStations().size() > 0) {
            stationLines.add(tmp);
        }

        tmp = findRouteByPerciseName(routeVagueName + "下行");
        if (tmp.getStations().size() > 0) {
            stationLines.add(tmp);
        }

        tmp = findRouteByPerciseName(routeVagueName);
        if (tmp.getStations().size() > 0) {
            stationLines.add(tmp);
        }

        return stationLines;
    }

    @Override
    public List<StationLine> findRelatedRoutesByStationName(String stationName) {
        Station station = findStationByName(stationName);
        List<StationLine> stationLines = new ArrayList<>();
        Set<String> lines = station.getLines();

        for (String line : lines) {
            StationLine stationLine = findRouteByPerciseName(line);
            if (stationLine != null) {
                stationLines.add(stationLine);
            }
        }

        return stationLines;
    }
}
