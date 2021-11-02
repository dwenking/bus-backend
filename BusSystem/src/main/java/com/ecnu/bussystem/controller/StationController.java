package com.ecnu.bussystem.controller;


import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.service.StationServiceImpl;
import io.swagger.annotations.ApiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Station")
public class StationController {
    @Autowired
    private StationServiceImpl stationService;

    // 根据站点id查找站点信息
    @GetMapping(path = "/GET/myId")
    public JSONResult<?> findStationById(
            @RequestParam(name = "id") String id
    ) {
        Station station= stationService.findStationById(id);
        return JSONResult.success(station);
    }

    // 根据站点name查找站点信息
    @GetMapping(path = "/GET/preciseName")
    public JSONResult<?> findStationByPreciseName(
            @RequestParam(name = "name") String stationName
    ) {
        List<Station> stationlist= stationService.findStationByPreciseName(stationName);
        return JSONResult.success(stationlist);
    }

    // 根据站点name查找站点信息
    @GetMapping(path = "/GET/vagueName")
    public JSONResult<?> findStationByVagueName(
            @RequestParam(name = "name") String stationName
    ) {
        List<Station> stationlist= stationService.findStationByVagueName(stationName);
        return JSONResult.success(stationlist);
    }



}
