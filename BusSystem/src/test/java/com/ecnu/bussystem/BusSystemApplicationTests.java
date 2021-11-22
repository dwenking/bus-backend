package com.ecnu.bussystem;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.service.LineServiceImpl;
import com.ecnu.bussystem.service.StationServiceImpl;
import com.ecnu.bussystem.service.TimetableServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
class BusSystemApplicationTests {
    @Autowired
    StationServiceImpl stationService;

    @Autowired
    LineServiceImpl lineService;

    @Autowired
    TimetableServiceImpl timetableService;

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
            System.out.println(station.getName() + " " + station.getMyId());
        }
    }

    @Test
    void testFindStationByStationPrecisename() {
        List<Station> stations = stationService.findStationByPreciseName("金河客运站(终点站)");
        for (Station station : stations) {
            System.out.println(station.getName() + " " + station.getMyId());
        }
    }

    @Test
    void testFindTop15LineNumberofStations() {
        List<Map<String, Object>> anslist = stationService.findTop15LineNumberofStations();
        if (anslist == null || anslist.size() == 0) {
            System.out.println("不存在");
            return;
        }
        for (int i = 0; i < anslist.size(); i++) {
            System.out.println(anslist.get(i));
        }
    }

    @Test
    void testFindDirectPathBetweenTwoStations() {
        String name1 = "航天立交东";
        String name2 = "生态公园";
        List<StationLine> stationLines = lineService.findDirectPathBetweenTwoStations(name1, name2);
        if (stationLines == null || stationLines.size() == 0) {
            System.out.println("不存在直达线路");
            return;
        }
        for (StationLine stationLine : stationLines) {
            System.out.println("直达线路：" + stationLine.getName() + ":");
            if (stationLine.getDirectional()) System.out.println(name1 + "->" + name2);
            if (!stationLine.getDirectional()) System.out.println(name2 + "->" + name1);
            List<Station> stationList = stationLine.getStations();
            for (Station station : stationList)
                System.out.println(station);
        }
    }

    @Test
    void testFindAlongStationLineByStartAndEndName() {
        List<StationLine> stationLines = lineService.findAlongStationLineByStartAndEndName("画展中心", "金河客运站", "1路");
        if (stationLines == null) {
            System.out.println("不存在路线");
            return;
        }
        System.out.println("size: " + stationLines.size());
        for (StationLine cur : stationLines) {
            System.out.println((cur.getName()) + ":");
            System.out.println("time:" + cur.getTime());
            List<Station> stations = cur.getStations();
            for (Station station : stations) {
                System.out.println(station.getName() + " " + station.getMyId());
            }
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
        if (stationLines == null) {
            System.out.println("线路不存在");
            return;
        }
        System.out.println("size: " + stationLines.size());
        for (StationLine cur : stationLines) {
            System.out.println((cur.getName()) + ":");
            List<Station> stations = cur.getStations();
            for (Station station : stations) {
                System.out.println(station.getName() + " " + station.getMyId());
            }
        }
    }

    @Test
    void testDate() {
        timetableService.findTimetableByIdAndTimeRange("06:00", 10, "12345");
    }

    @Test
    void testFindTop15StationPairs() {
        System.out.println(stationService.findTop15StationPairs());
    }

    @Test
    void testFindTop15MostStationsRoutes() {
        System.out.println(lineService.findTop15MostStationsRoutes());
    }

    @Test
    void testFindDuplicateStations() {
        List<JSONObject> res = lineService.findDuplicateStations("30路下行", "15路上行");
        if (res == null) {
            System.out.println("数据不存在");
            return;
        }
        System.out.println(res);
    }

    @Test
    void testFindTheNumberOfOneWayStations() {
        String name="208路";
        List<JSONObject> res = lineService.findOneWayStationsByRouteName(name);
        if (res == null) {
            System.out.println("数据不存在");
            return;
        }
        System.out.println(res);
    }

    @Test
    void testFindNumberOfMetroStations() {
        System.out.println(stationService.findNumberOfMetroStations());
    }

    @Test
    void testFindNumberOfBeginStations() {
        System.out.println(stationService.findNumberOfBeginStations());
    }

    @Test
    void testFindNumberOfEndStations() {
        System.out.println(stationService.findNumberOfEndStations());
    }


    @Test
    void testfindTypeAndNumberOfLines() {
        List<JSONObject> res = lineService.findTypeAndNumberOfLines();
        if (res == null || res.size() == 0) {
            System.out.println("数据不存在");
            return;
        }
        for (JSONObject line : res) {
            System.out.println(line);
        }
    }

    @Test
    void testFindLinesOfLongestRuntime() {
        List<JSONObject> res = timetableService.findLinesOfLongestRuntime();
        System.out.println(res);
    }

    @Test
    void testFindTransferLines(){
        List<JSONObject> res = lineService.findTransferLines("1路上行");
        System.out.println(res);
    }
}
