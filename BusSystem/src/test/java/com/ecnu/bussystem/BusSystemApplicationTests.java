package com.ecnu.bussystem;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.service.BusInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class BusSystemApplicationTests {
    @Autowired
    BusInfoServiceImpl busInfoService;

    @Test
    void testFindStationById() {
        Station station = busInfoService.findStationById("5425");

        System.out.println(station.getName() + " " + station.getEnglishname() + " " + station.getType());
    }

    @Test
    void testFindRouteByPerciseName() {
        StationLine stationLine = busInfoService.findRouteByPerciseName("211路上行");

        System.out.println("line: " + stationLine.getName());
        List<Station> stations = stationLine.getStations();

        // 注意空指针（有一些station的begins和ends是空）
        for (Station cur : stations) {
            System.out.println((cur.getName()) + " " + cur.getIdlist().size());
        }
    }

    @Test
    void testFindRouteByVagueName() {
        List<StationLine> stationLines = busInfoService.findRouteByVagueName("211路");
        System.out.println("size: " + stationLines.size());

        for (StationLine cur : stationLines) {
            System.out.println((cur.getName())+ ":");
            List<Station> stations = cur.getStations();
            for (Station station : stations) {
                System.out.println(station.getName());
            }
        }
    }

    @Test
    void testFindRelatedRoutesByStationName() {
        List<StationLine> stationLines = busInfoService.findRelatedRoutesByStationName("灵龙大道北");
        System.out.println("size: " + stationLines.size());

        for (StationLine cur : stationLines) {
            System.out.println((cur.getName())+ ":");
            List<Station> stations = cur.getStations();
            for (Station station : stations) {
                System.out.println(station.getName());
            }
        }
    }
}
