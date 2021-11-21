package com.ecnu.bussystem.controller;


import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.service.LineServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/line")
public class LineController {
    @Autowired
    private LineServiceImpl lineService;

    // 根据线路精确名称查找线路（指明上行和下行）
    @GetMapping(path = "/perciselinename/{name}")
    public JSONResult<?> findLineByPerciseName(
            @PathVariable String name
    ) {
        Line line = lineService.findLineByPerciseName(name);
        if (line == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,name:" + name);
        }
        return JSONResult.success(line);
    }

    //根据线路模糊名称查找线路（不指明上行还是下行）
    @GetMapping(path = "/vaguelinename/{name}")
    public JSONResult<?> findLineByVagueName(
            @PathVariable String name
    ) {
        List<Line> lines = lineService.findLineByVagueName(name);
        if (lines == null || lines.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,name:" + name);
        }
        return JSONResult.success(lines);
    }

    // 根据站点数量对线路进行排序
    @GetMapping(path = "/top15moststationsroutes")
    public JSONResult<?> findTop15MostStationsRoutes() {
        List<Map<String, String>> mapList = lineService.findTop15MostStationsRoutes();
        if (mapList == null || mapList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(mapList);
    }

    @GetMapping(path = "/findthenumberofonewaystations")
    public JSONResult<?> findTheNumberOfOneWayStations() {
        JSONObject object = lineService.findTheNumberOfOneWayStations();
        if (object == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(object);
    }

    @GetMapping(path = "/findtypeandnumberoflines")
    public JSONResult<?> findTypeAndNumberOfLines() {
        List<JSONObject> jsonObjects = lineService.findTypeAndNumberOfLines();
        if (jsonObjects == null || jsonObjects.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(jsonObjects);
    }

    //统计某个线路上每个站点可以换乘的线路，站点根据id查找换乘路线
    @GetMapping(path = "/findtransferlines/{name}")
    public JSONResult<?> findTransferLines(
            @PathVariable String name
    ) {
        List<JSONObject> res = lineService.findTransferLines(name);
        if (res == null || res.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "该线路所有站点都没有可换乘的线路");
        }
        return JSONResult.success(res);
    }

    // 计算某条线路的非重复系数
    @GetMapping(path = "/notrepeating/{name}")
    public JSONResult<?> findNotRepeating(
            @PathVariable String name
    ) {
        JSONObject res = lineService.findNotRepeating(name);
        if (res == null || res.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "出错啦！");
        }
        return JSONResult.success(res);
    }

}
