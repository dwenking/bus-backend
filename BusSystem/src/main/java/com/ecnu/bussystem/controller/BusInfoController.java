package com.ecnu.bussystem.controller;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.service.BusInfoServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BusInfoController {
    @Autowired
    private BusInfoServiceImpl busInfoService;

    // 根据站点id查找站点信息
    @GetMapping(path = "/getStationById")
    public Station findStationById(
            @RequestParam(name = "id") String id
    ) {
        return busInfoService.findStationById(id);
    }

    // 根据站点name查找站点信息
    @GetMapping(path = "/getStationByName")
    public Station findStationByName(
            @RequestParam(name = "name") String stationName
    ) {
        return busInfoService.findStationByName(stationName);
    }

    // 根据线路精确名称查找线路（指明上行和下行）
    @GetMapping(path = "/getRouteByPerciseName")
    public StationLine findRouteByPerciseName(
            @RequestParam(name = "name") String routeName
    ) {
        return busInfoService.findRouteByPerciseName(routeName);
    }

    // 根据线路名称模糊查找线路（不指明上行和下行）
    @GetMapping(path = "/getRouteByVagueName")
    public List<StationLine> findRouteByVagueName(
            @RequestParam(name = "name") String routeName
    ) {
        return busInfoService.findRouteByVagueName(routeName);
    }

    // 根据站点名称查找经过该站点的线路
    @GetMapping(path = "/getRelatedRoutesByStationName")
    public List<StationLine> findRelatedRoutesByStationName(
            @RequestParam(name = "name") String stationName
    ) {
        return busInfoService.findRelatedRoutesByStationName(stationName);
    }

    // 根据两个站点名称查找是否存在直达路线，返回直达路线的名称
    @GetMapping(path = "/getTwoStationDirectRoutenameByName")
    public List<String> findTwoStationDirectRoutenameByName(
            @RequestParam(name = "name1") String name1,
            @RequestParam(name = "name2") String name2

    ) {
        return busInfoService.findTwoStationDirectRoutenameByName(name1, name2);
    }

    // 根据两个站点名称查找是否存在直达路线，返回直达路径
    @GetMapping(path = "/getTwoStationDirectPathByName")
    public List<StationLine> findTwoStationDirectPathByName(
            @RequestParam(name = "name1") String name1,
            @RequestParam(name = "name2") String name2

    ) {
        return busInfoService.findTwoStationDirectPathByName(name1, name2);
    }

    // 根据路线名称，两个站点名称查找这条路线上是否存在直达路线
    @GetMapping(path = "/getTwoStationOnThisPathDirectPathByName")
    public List<StationLine> findTwoStationOnThisPathDirectPathByName(
            @RequestParam(name = "routename") String routename,
            @RequestParam(name = "name1") String name1,
            @RequestParam(name = "name2") String name2

    ) {
        return busInfoService.findTwoStationOnThisPathDirectPathByName(routename, name1, name2);
    }
}
