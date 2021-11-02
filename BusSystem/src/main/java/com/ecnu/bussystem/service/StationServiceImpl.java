package com.ecnu.bussystem.service;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.respository.StationRepository;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
        String stationname1;
        String stationname2;
        String stationname3;
        if (stationName.length() >= 5 && (
                stationName.substring(stationName.length() - 5).equals("(首发站)") || stationName.substring(stationName.length() - 5).equals("(终点站)"))) {
            String substring = stationName.substring(0, stationName.length() - 5);
            stationname1 = substring;
            stationname2 = substring + "(首发站)";
            stationname3 = substring + "(终点站)";

        } else {
            stationname1 = stationName;
            stationname2 = stationName + "(首发站)";
            stationname3 = stationName + "(终点站)";
        }
        List<Station> stationList1 = stationRepository.findStationByName(stationname1);
        List<Station> stationList2 = stationRepository.findStationByName(stationname2);
        List<Station> stationList3 = stationRepository.findStationByName(stationname3);
        List<Station> alllist = new ArrayList<>();
        if(stationList1!=null)alllist.addAll(stationList1);
        if(stationList1!=null)alllist.addAll(stationList3);
        if(stationList1!=null)alllist.addAll(stationList2);
        return alllist;
    }

//    @Override
//    public StationLine findRouteByPerciseName(String routeName) {
//        StationLine stationLine = new StationLine();
//        List<Station> stations = new ArrayList<>();
//
//
//        stationLine.setName(routeName);
//
//        try (Session session = neo4jDriver.session()) {
//            String cypher = String.format("MATCH p=(s)-[r *.. {name:'%s'}]->(e) where '%s' in s.begins and '%s' in e.ends RETURN p order by length(p) desc", routeName, routeName, routeName);
//            Result result = session.run(cypher);
//
//            try {
//                List<Record> records = result.list();
//                Record record = null;
//
//                if (records != null) {
//                    record = records.get(0);
//                }
//
//                Value value = record.get("p");  // 因为是return p
//                Path path = value.asPath();  // 返回的是路径类型，故使用.asPath()
//
//                // 得到node结果后，类型转换并加入line的station list
//                for (Node node : path.nodes()) {
//                    Map<String, Object> map = node.asMap();
//                    String mapString = JSONObject.toJSONString(map);
//                    Station station = JSONObject.parseObject(mapString, Station.class); //json字符串直接转给java对象
//
//                    stations.add(station);
//                }
//            } catch (Exception e) {
//                System.out.println("没有找到Record, name:" + routeName);
//            }
//        }
//        stationLine.setStations(stations);
//
//        return stationLine;
//    }


}
