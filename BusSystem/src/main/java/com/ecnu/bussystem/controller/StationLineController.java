package com.ecnu.bussystem.controller;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.service.LineServiceImpl;
import com.ecnu.bussystem.service.StationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stationLine")
public class StationLineController {
    @Autowired
    private LineServiceImpl lineService;

    @Autowired
    private StationServiceImpl stationService;

    //根据路线的名称返回路线上的站
    @GetMapping(path = "/stationofpreciseline/{name}")
    public JSONResult<?> findStationOfLineByPreciseName(
            @PathVariable String name
    ) {
        StationLine line = lineService.findStationOfLineByPreciseName(name);
        if (!line.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路站点数据,name:" + name);
        }
        return JSONResult.success(line);
    }

    //根据路线的名称模糊搜索返回路线上的站
    @GetMapping(path = "/stationofvagueline/{name}")
    public JSONResult<?> findStationOfLineByVagueName(
            @PathVariable String name
    ) {
        List<StationLine> lines = lineService.findStationOfLineByVagueName(name);
        if (lines == null || lines.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路站点数据,name:" + name);
        }
        return JSONResult.success(lines);
    }

    // 根据站点模糊name查找经过站点的所有线路
    @GetMapping(path = "/lineofvaguestation/{name}")
    public JSONResult<?> findLineOfStationByVagueName(
            @PathVariable String name
    ) {
        List<Station> stations = stationService.findLineOfStationByVagueName(name);
        if (stations == null || stations.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到站点线路数据,name:" + name);
        }
        return JSONResult.success(stations);
    }

    // 找出两个线路的重复站点名并统计数量
    @GetMapping(path = "/duplicatestationsoflines/{name1}{name2}")
    public JSONResult<?> findDuplicateStations(
            @RequestParam("name1") String lineName1,
            @RequestParam("name2") String lineName2
    ) {
        List<Map<String, String>> stations = lineService.findDuplicateStations(lineName1, lineName2);
        if (stations == null || stations.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到重复站点,name1:" + lineName1 + "name2" + lineName2);
        }
        return JSONResult.success(stations);
    }

    //根据线路数据和首末节点返回线路
    @GetMapping(path = "/lineofstartansendstation/{routename}:{name1}-{name2}")
    public JSONResult<?> findAlongStationLineByStartAndEndName(
            @PathVariable String routename,
            @PathVariable String name1,
            @PathVariable String name2
    ) {
        List<StationLine> stationLines = lineService.findAlongStationLineByStartAndEndName(name1, name2, routename);
        if (stationLines == null || stationLines.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, String.format("未找到线路'%s':'%s'->'%s'", routename, name1, name2));
        }
        return JSONResult.success(stationLines);
    }

    @GetMapping(path = "/directpathbetweentwostations/{name1}-{name2}")
    public JSONResult<?>findDirectPathBetweenTwoStations(
            @PathVariable String name1,
            @PathVariable String name2
    ){
        List<JSONObject> stationLines = lineService.findDirectPathNameBetweenTwoStations(name1, name2);
        if (stationLines == null || stationLines.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, String.format("未找到直达线路:'%s'->'%s'", name1, name2));
        }
        return JSONResult.success(stationLines);
    }
}
