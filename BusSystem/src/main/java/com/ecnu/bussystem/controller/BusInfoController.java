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
    ){
        return busInfoService.findStationById(id);
    }

    // 根据站点name查找站点信息
    @GetMapping(path = "/getStationByName")
    public Station findStationByName(
            @RequestParam(name = "name") String stationName
    ){
        return busInfoService.findStationByName(stationName);
    }

    // 根据线路精确名称查找线路（指明上行和下行）
    @GetMapping(path = "/getRouteByPerciseName")
    public StationLine findRouteByPerciseName(
            @RequestParam(name = "name") String routeName
    ){
        return busInfoService.findRouteByPerciseName(routeName);
    }

    // 根据线路名称模糊查找线路（不指明上行和下行）
    @GetMapping(path = "/getRouteByVagueName")
    public List<StationLine> findRouteByVagueName(
            @RequestParam(name = "name") String routeName
    ){
        return busInfoService.findRouteByVagueName(routeName);
    }

    // 根据站点名称查找经过该站点的线路
    @GetMapping(path = "/getRelatedRoutesByStationName")
    public List<StationLine> findRelatedRoutesByStationName(
            @RequestParam(name = "name") String stationName
    ){
        return busInfoService.findRelatedRoutesByStationName(stationName);
    }
}
