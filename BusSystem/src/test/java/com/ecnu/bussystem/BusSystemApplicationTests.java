package com.ecnu.bussystem;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.entity.Timetable;
import com.ecnu.bussystem.service.LineServiceImpl;
import com.ecnu.bussystem.service.StationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@SpringBootTest
class BusSystemApplicationTests {
    @Autowired
    StationServiceImpl stationService;
    @Autowired
    LineServiceImpl lineService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void testFindStationById() {
        Station station = stationService.findStationById("21540");
        System.out.println(station.getName() + " " + station.getEnglishname() + " " + station.getType());
    }

    @Test
    void testFindStationByStationname() {
        List<Station> stations = stationService.findStationByVagueName("金河客运站");
        for (Station station : stations) {
            System.out.println(station.getName()+" "+station.getMyId());
        }
    }

    @Test
    void testFindStationByStationPrecisename() {
        List<Station> stations = stationService.findStationByPreciseName("金河客运站(终点站)");
        for (Station station : stations) {
            System.out.println(station.getName()+" "+station.getMyId());
        }
    }

    @Test
    void testFindRouteByPerciseName() {
        StationLine stationLine = lineService.findStationOfLineByPreciseName("1路上行");
        System.out.println("line: " + stationLine.getName());
        List<Station> stations = stationLine.getStations();
        System.out.println(stationLine.getName() + " " + stationLine.getDirectional());
        // 注意空指针（有一些station的begins和ends是空）
        for (Station cur : stations) {
            System.out.println((cur.getName()) + " " + cur.getMyId());
        }
    }

    @Test
    void testFindRouteByVagueName() {
        List<StationLine> stationLines = lineService.findStationOfLineByVagueName("1路");
        System.out.println("size: " + stationLines.size());

        for (StationLine cur : stationLines) {
            System.out.println((cur.getName()) + ":");
            List<Station> stations = cur.getStations();
            for (Station station : stations) {
                System.out.println(station.getName()+" "+station.getMyId());
            }
        }
    }

//    @Test
//    void testFindRelatedRoutesByStationName() {
//        List<StationLine> stationLines = busInfoService.findRelatedRoutesByStationName("灵龙大道北");
//        System.out.println("size: " + stationLines.size());
//
//        for (StationLine cur : stationLines) {
//            System.out.println((cur.getName()) + ":");
//            List<Station> stations = cur.getStations();
//            for (Station station : stations) {
//                System.out.println(station.getName());
//            }
//        }
//    }
//
//    //判断两个站点之间是否存在直达路线并显示“路线名称-路径”
//    @Test
//    void testFindTwoStationDirectPathByName() {
//        String station1name = "金河市政府";
//        String station2name = "金河市政府";
//        List<StationLine> stationLines = busInfoService.findTwoStationDirectPathByName(station1name, station2name);
//        if (stationLines.size() == 0) {
//            System.out.println("不存在直达线路");
//            return;
//        }
//        for (int i = 0; i < stationLines.size(); i++) {
//            System.out.print(stationLines.get(i).getName() + ":");
//            List<Station> directPath = stationLines.get(i).getStations();
//            for (int j = 0; j < directPath.size(); j++) {
//                if (j != directPath.size() - 1)
//                    System.out.printf(directPath.get(j).getName() + "->");
//                else System.out.println(directPath.get(j).getName());
//            }
//        }
//    }
//
//    //判断两个站点之间是否存在直达路线，并输出直达线路的名称
//    @Test
//    void testFindTwoStationDirectRoutenameByName() {
//        String name1 = "燎原";
//        String name2 = "北门立交西";
//        List<String> routenameList = busInfoService.findTwoStationDirectRoutenameByName(name1, name2);
//        if (routenameList.size() <= 0) {
//            System.out.println("不存在线路");
//        }
//        for (int i = 0; i < routenameList.size(); i++) {
//            System.out.println(routenameList.get(i));
//        }
//    }
//
//    //判断指定一条路线上两个站点之间的路线，（直达线路的路径）
//    @Test
//    void testFindTwoStationOnThisPathDirectPathByName() {
//        String name1 = "何家巷";
//        String name2 = "燎原";
//        String routename = "102路";
//        List<StationLine> directPath = busInfoService.findTwoStationOnThisPathDirectPathByName(routename, name1, name2);
//        if (directPath.size() == 0) {
//            System.out.println("线路不存在");
//        }
//        for (int i = 0; i < directPath.size(); i++) {
//            String thisRouteName = directPath.get(i).getName();
//            System.out.printf(thisRouteName + ":");
//            List<Station> stationList = directPath.get(i).getStations();
//            for (int j = 0; j < stationList.size(); j++) {
//                if (j != stationList.size() - 1)
//                    System.out.printf(stationList.get(j).getName() + "->");
//                else System.out.println(stationList.get(j).getName());
//            }
//        }
//    }

    @Test
    void testMongoDB() {
        Query query = new Query();
        query.addCriteria(Criteria.where("stationID").is("16560"));
        query.addCriteria(Criteria.where("passTime").is("06:00"));

        List<Timetable> find = mongoTemplate.find(query, Timetable.class, "timetable");
        for (Timetable timetable : find) {
            System.out.println(timetable.getRouteName());
        }
    }
}
