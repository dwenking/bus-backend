package com.ecnu.bussystem.controller;


import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.service.StationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/station")
public class StationController {
    @Autowired
    private StationServiceImpl stationService;

    // 根据站点id查找站点信息
    @GetMapping(path = "/station/id/{id}")
    public JSONResult<?> findStationById(
            @PathVariable String id
    ) {
        Station station = stationService.findStationById(id);
        if (station == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,id:" + id);
        }
        return JSONResult.success(station);
    }

    // 根据站点精确name查找站点信息
    @GetMapping(path = "/percise/station/name/{name}")
    public JSONResult<?> findStationByPreciseName(
            @PathVariable String name
    ) {
        List<Station> stationList = stationService.findStationByPreciseName(name);
        if (stationList == null || stationList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,name:" + name);
        }
        return JSONResult.success(stationList);
    }

    // 根据站点模糊name查找站点信息
    @GetMapping(path = "/vague/station/name/{name}")
    public JSONResult<?> findStationByVagueName(
            @PathVariable String name
    ) {
        List<Station> stationList = stationService.findStationByVagueName(name);
        if (stationList == null || stationList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,name:" + name);
        }
        return JSONResult.success(stationList);
    }

    //返回站点间直达线路最多的两个站点的全部信息及线路数量，显示降序前十五个
    @GetMapping(path = "/top15/station/pair")
    public JSONResult<?> findTop15StationPairs() {
        List<Map<String, Object>> mapList = stationService.findTop15StationPairs();
        if (mapList == null || mapList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(mapList);
    }

    @GetMapping(path = "/find/top15/linenumber/of/station")
    public JSONResult<?> findTop15LineNumberofStations() {
        List<Map<String, Object>> mapList = stationService.findTop15LineNumberofStations();
        if (mapList == null || mapList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(mapList);
    }

    @GetMapping(path = "/find/number/of/metro/station")
    public JSONResult<?> findNumberOfMetroStations() {
        List<String> stringList = stationService.findNumberOfMetroStations();
        if (stringList == null || stringList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(stringList);
    }

    @GetMapping(path = "/find/number/of/begin/station")
    public JSONResult<?> findNumberOfBeginStations() {
        List<String> stringList = stationService.findNumberOfBeginStations();
        if (stringList == null || stringList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(stringList);
    }

    @GetMapping(path = "/find/number/of/end/station")
    public JSONResult<?> findNumberOfEndStations() {
        List<String> stringList = stationService.findNumberOfEndStations();
        if (stringList == null || stringList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(stringList);
    }
}
