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

    @GetMapping(path = "/StationById")
    public Station findStationById(
            @RequestParam(name = "id") String id
    ){
        return busInfoService.findStationById(id);
    }

    @GetMapping(path = "/RouteByPerciseName")
    public StationLine findRouteByPerciseName(
            @RequestParam(name = "routeName") String routeName
    ){
        return busInfoService.findRouteByPerciseName(routeName);
    }

    @GetMapping(path = "/RouteByName")
    public List<StationLine> findRouteByName(
            @RequestParam(name = "routeName") String routeName
    ){
        return busInfoService.findRouteByName(routeName);
    }
}
