package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.respository.StationRespository;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class BusInfoServiceImpl implements BusInfoService {
    @Autowired
    StationRespository stationRespository;

    @Resource
    Driver neo4jDriver;

    @Autowired
    MongoTemplate mongoTemplate;

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
            String cypher = String.format("MATCH p=(s)-[r *.. {name:'%s'}]->(e) where '%s' in s.begins and '%s' in e.ends RETURN p order by length(p) desc", routeName, routeName, routeName);
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

    // 求两个站之间的直达的路径，返回“路线名称”
    @Override
    public List<String> findTwoStationDirectRoutenameByName(String name1, String name2) {
        List<StationLine> directPath = new ArrayList<>(findTwoStationDirectPathByName(name1, name2));
        Set<String> routeNameSet = new HashSet<>();
        for (int i = 0; i < directPath.size(); i++) {
            routeNameSet.add(directPath.get(i).getName());
        }
        List<String>stringList= new ArrayList<>(routeNameSet);
        Collections.sort(stringList);
        return stringList;
    }

    // 求两个站之间的直达的路径，返回“路线名称-路径数组”
    @Override
    public List<StationLine> findTwoStationDirectPathByName(String name1, String name2) {
        // 找出两条路线中相同id的路线并判断是否可以直达
        Set<StationLine> station1Lines = new HashSet(findRelatedRoutesByStationName(name1));
        Set<StationLine> station2Lines = new HashSet(findRelatedRoutesByStationName(name2));
        station1Lines.retainAll(station2Lines); // 取交集

        Set<StationLine> twoStationSameLines = station1Lines; // 求两个站相同的路线
        List<StationLine> directPath = new ArrayList<>(); // 用来存储结果：两条路线中的直达路径

        if (name1.equals(name2)) {
            return directPath;
        }

        // 遍历这些相同的路线，求出合格的直达路线
        for (StationLine obj : twoStationSameLines) {
            List<Station> stationList = obj.getStations();
            List<Station> thisDirectPath; //存储这条路线的（两个站之间的）路径

            //遍历这条路线中的站，并取station1name和station2name所在的站在这条路线上的indx位置，并两两匹配得到直达路径
            List<Integer> station1IndxList = new ArrayList<>();
            List<Integer> station2IndxList = new ArrayList<>();

            for (int i = 0; i < stationList.size(); i++) {
                if (stationList.get(i).getName().equals(name1)) {
                    station1IndxList.add(i);
                }
                if (stationList.get(i).getName().equals(name2)) {
                    station2IndxList.add(i);
                }
            }

            for (int i = 0; i < station1IndxList.size(); i++) {
                for (int j = 0; j < station2IndxList.size(); j++) {
                    int indx1 = station1IndxList.get(i);
                    int indx2 = station2IndxList.get(j);
                    if (indx1 < indx2) {
                        thisDirectPath = new ArrayList<>(stationList.subList(indx1, indx2 + 1));
                        directPath.add(new StationLine(obj.getName(), new ArrayList<>(thisDirectPath)));
                    } else {
                        thisDirectPath = new ArrayList<>(stationList.subList(indx2, indx1 + 1));
                        directPath.add(new StationLine(obj.getName(), new ArrayList<>(thisDirectPath)));
                    }
                    thisDirectPath.clear();
                }
            }
        }
        Collections.sort(directPath);
        return directPath;
    }

    @Override
    public List<StationLine> findTwoStationOnThisPathDirectPathByName(String routename, String name1, String name2) {
        List<StationLine> directPath = new ArrayList<>(findTwoStationDirectPathByName(name1, name2));
        List<StationLine> routeList = new ArrayList<>();
        String routename1, routename2; // 模糊查询每个路线的上下行

        if (routename.length() >= 2) {
            String substring = routename.substring(routename.length() - 2, routename.length());
            if (routename.length() >= 2 && (substring.equals("上行") || substring.equals("下行"))) {
                routename = routename.substring(0, routename.length() - 2);
            }
        }

        routename1 = routename + "上行";
        routename2 = routename + "下行";

        for (int i = 0; i < directPath.size(); i++) {
            String thisRouteName = directPath.get(i).getName();
            if (thisRouteName.equals(routename) || thisRouteName.equals(routename1) || thisRouteName.equals(routename2)) {
                routeList.add(directPath.get(i));
            }
        }
        Collections.sort(routeList);
        return routeList;
    }
}
