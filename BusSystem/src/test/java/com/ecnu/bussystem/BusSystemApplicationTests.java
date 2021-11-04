package com.ecnu.bussystem;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.entity.Timetable;
import com.ecnu.bussystem.service.LineServiceImpl;
import com.ecnu.bussystem.service.StationServiceImpl;
import com.ecnu.bussystem.service.TimetableServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

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
                System.out.println(station.getName() + " " + station.getMyId());
            }
        }
    }

    @Test
    void testDate() {
        timetableService.findTimetableByIdAndTimeRange("06:00",10,"12345");
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
    void findDuplicateStations() {
        List<Map<String, String>> res = lineService.findDuplicateStations("523路上行", "523路下行");
        System.out.println(res);
    }
}
