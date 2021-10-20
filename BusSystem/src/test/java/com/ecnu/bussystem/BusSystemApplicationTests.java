package com.ecnu.bussystem;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.respository.StationRespository;
import com.ecnu.bussystem.service.BusInfoService;
import com.ecnu.bussystem.service.BusInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
class BusSystemApplicationTests {
    @Autowired
    StationRespository stationRespository;

    @Autowired
    BusInfoServiceImpl busInfoService;

    @Test
    void testFindStationById() {
        Station station = stationRespository.findStationById("5425");
        System.out.println(station.getName()+" "+ station.getStationId());
    }

    @Test
    void testFindRouteByPerciseName() {
        // 如果查询不存在的线路返回的List size = 0
        List<Station> alongStations = stationRespository.findRouteByPerciseName("211路");
        System.out.println(alongStations.size());
        for (Station cur : alongStations) {
            System.out.println((cur.getName())+ " " + cur.getStationId());
        }
    }

    // 模糊查询
    @Test
    void testFindRouteByName() {
        List<StationLine> stationLines = busInfoService.findRouteByName("211路");
        System.out.println(stationLines.size());
        for (StationLine cur : stationLines) {
            System.out.println((cur.getName())+ " " + cur.getStations().size());
        }
    }
}
