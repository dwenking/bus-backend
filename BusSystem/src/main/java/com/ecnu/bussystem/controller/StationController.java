package com.ecnu.bussystem.controller;


import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.service.StationServiceImpl;
import io.swagger.annotations.ApiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/station")
public class StationController {
    @Autowired
    private StationServiceImpl stationService;

    // 根据站点id查找站点信息
    @GetMapping(path = "/stationid/{id}")
    public JSONResult<?> findStationById(
            @PathVariable String id
    ) {
        Station station= stationService.findStationById(id);
        if (station == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到线路数据,id:" + id);
        }
        return JSONResult.success(station);
    }

    // 根据站点精确name查找站点信息
    @GetMapping(path = "/percisestationname/{name}")
    public JSONResult<?> findStationByPreciseName(
            @PathVariable String name
    ) {
        List<Station> stationList= stationService.findStationByPreciseName(name);
        if (stationList == null || stationList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到线路数据,name:" + name);
        }
        return JSONResult.success(stationList);
    }

    // 根据站点模糊name查找站点信息
    @GetMapping(path = "/vaguestationname/{name}")
    public JSONResult<?> findStationByVagueName(
            @PathVariable String name
    ) {
        List<Station> stationList= stationService.findStationByVagueName(name);
        if (stationList == null || stationList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到线路数据,name:" + name);
        }
        return JSONResult.success(stationList);
    }

    @GetMapping(path = "/top15stationpairs")
    public JSONResult<?> findTop15StationPairs(){
        List<Map<String,String>> mapList=stationService.findTop15StationPairs();
        if (mapList == null || mapList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到数据");
        }
        return JSONResult.success(mapList);
    }
}
