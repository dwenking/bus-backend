package com.ecnu.bussystem.controller;


import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.service.StationServiceImpl;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation(value = "findStationById根据站点id查找站点信息", notes = "根据站点id查找站点信息")
    @GetMapping(path = "/{id}")
    public JSONResult<?> findStationById(
            @PathVariable String id
    ) {
        Station station = stationService.findStationById(id);
        if (station == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,id:" + id);
        }
        return JSONResult.success(station);
    }


    @ApiOperation(value = "findStationByPreciseName根据站点精确name查找站点信息", notes = "根据站点精确name查找站点信息")
    @GetMapping(path = "/percise/name/{name}")
    public JSONResult<?> findStationByPreciseName(
            @PathVariable String name
    ) {
        List<Station> stationList = stationService.findStationByPreciseName(name);
        if (stationList == null || stationList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,name:" + name);
        }
        return JSONResult.success(stationList);
    }


    @ApiOperation(value = "findStationByVagueName根据站点模糊name查找站点信息", notes = "根据站点模糊name查找站点信息")
    @GetMapping(path = "/vague/name/{name}")
    public JSONResult<?> findStationByVagueName(
            @PathVariable String name
    ) {
        List<Station> stationList = stationService.findStationByVagueName(name);
        if (stationList == null || stationList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,name:" + name);
        }
        return JSONResult.success(stationList);
    }


    @ApiOperation(value = "findTop15StationPairs根据连接两个相邻站台之间线路数量排序两个相邻站台", notes = "15根据连接两个相邻站台之间线路数量排序两个相邻站台，" +
            "返回站点间直达线路最多的两个站点的全部信息及线路数量，显示降序前十五个")
    @GetMapping(path = "/top15/pair")
    public JSONResult<?> findTop15StationPairs() {
        List<Map<String, Object>> mapList = stationService.findTop15StationPairs();
        if (mapList == null || mapList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(mapList);
    }


    @ApiOperation(value = "findTop15LineNumberofStations统计停靠路线最多的站点并排序",notes = "统计停靠路线最多的站点并排序，按照ID统计，并根据数量降序排序，显示前15个")
    @GetMapping(path = "/top15/linenumber")
    public JSONResult<?> findTop15LineNumberofStations() {
        List<Map<String, Object>> mapList = stationService.findTop15LineNumberofStations();
        if (mapList == null || mapList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(mapList);
    }

    @GetMapping(path = "/find/number/of/metro")
    public JSONResult<?> findNumberOfMetroStations() {
        List<String> stringList = stationService.findNumberOfMetroStations();
        if (stringList == null || stringList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(stringList);
    }

    @GetMapping(path = "/find/number/of/begin")
    public JSONResult<?> findNumberOfBeginStations() {
        List<String> stringList = stationService.findNumberOfBeginStations();
        if (stringList == null || stringList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(stringList);
    }

    @GetMapping(path = "/find/number/of/end")
    public JSONResult<?> findNumberOfEndStations() {
        List<String> stringList = stationService.findNumberOfEndStations();
        if (stringList == null || stringList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(stringList);
    }
}
