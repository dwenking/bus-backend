package com.ecnu.bussystem.controller;


import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.service.LineServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/line")
public class LineController {
    @Autowired
    private LineServiceImpl lineService;


    @ApiOperation(value = "findLineByPerciseName根据线路精确名称查找线路",notes = "1根据线路精确名称查找线路（指明上下行）")
    @GetMapping(path = "/percise/name/{name}")
    public JSONResult<?> findLineByPerciseName(
            @PathVariable String name
    ) {
        Line line = lineService.findLineByPerciseName(name);
        if (line == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,name:" + name);
        }
        return JSONResult.success(line);
    }

    @ApiOperation(value = "findLineByVagueName根据线路模糊名称查找线路",notes = "根据线路模糊名称查找线路（不指明上行还是下行）")
    @GetMapping(path = "/vague/name/{name}")
    public JSONResult<?> findLineByVagueName(
            @PathVariable String name
    ) {
        List<Line> lines = lineService.findLineByVagueName(name);
        if (lines == null || lines.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到线路数据,name:" + name);
        }
        return JSONResult.success(lines);
    }


    @ApiOperation(value = "findTop15MostStationsRoutes根据站点数量对线路进行排序",notes = "16根据站点数量对线路进行排序，降序排列，显示前15条，线路含方向")
    @GetMapping(path = "/top15/most/station")
    public JSONResult<?> findTop15MostStationsRoutes() {
        List<Map<String, String>> mapList = lineService.findTop15MostStationsRoutes();
        if (mapList == null || mapList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(mapList);
    }


    @ApiOperation(value = "findOneWayStationsByRouteName统计某条线路的单行站",notes = "11B统计某条线路的单行站")
    @GetMapping(path = "/find/the/number/of/oneway/station/{name}")
    public JSONResult<?> findOneWayStationsByRouteName(
            @PathVariable String name
    ) {
        List<JSONObject> objectList = lineService.findOneWayStationsByRouteName(name);
        if (objectList == null||objectList.size()==0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(objectList);
    }

    @ApiOperation(value = "findTypeAndNumberOfLines分组统计各个类型线路的数量",notes = "12分组统计常规公交(包括干线、支线、城乡线、驳接线、社区线)、\n" +
            "快速公交(K字开头)、高峰公交(G字开头)、夜班公交(N字开头)的数量")
    @GetMapping(path = "/find/type/and/number/of/line")
    public JSONResult<?> findTypeAndNumberOfLines() {
        List<JSONObject> jsonObjects = lineService.findTypeAndNumberOfLines();
        if (jsonObjects == null || jsonObjects.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(jsonObjects);
    }



    @ApiOperation(value = "findTransferLines统计某个线路上每个站点可以换乘的线路",notes = "14查询换乘线路，站点根据id查找换乘路线。换乘线路数即线路停靠的所有站台停靠其他线路的数量的总和")
    @GetMapping(path = "/find/transfer/{name}")
    public JSONResult<?> findTransferLines(
            @PathVariable String name
    ) {
        List<JSONObject> res = lineService.findTransferLines(name);
        if (res == null || res.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "该线路所有站点都没有可换乘的线路");
        }
        return JSONResult.success(res);
    }

    // 删除某条线路并删除只有该线路经过的站点
    @DeleteMapping(path = "/{name}")
    public JSONResult<?> deleteLineByPerciseName(String name) {
        JSONObject res = lineService.deleteLineByPerciseName(name);
        if (res == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到可删除线路");
        }
        return JSONResult.success(res);
    }

    // 恢复某条线路并恢复只有该线路经过的站点
    @PostMapping(path = "/{name}")
    public JSONResult<?> restoreLineByPerciseName(String name) {
        JSONObject res = lineService.restoreLineByPerciseName(name);
        if (res == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到可恢复线路");
        }
        return JSONResult.success(res);
    }

    // 替换某条线路站点
    @PutMapping(path = "/{name}/{oldId}/{newId}")
    public JSONResult<?> replaceStationInLine(
            @PathVariable(required = true) String oldId,
            @PathVariable(required = true) String newId,
            @PathVariable(required = true) String name) {
        StationLine stationLine = lineService.replaceStationInLine(name, oldId, newId);
        if (stationLine == null || !stationLine.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "替换失败，请检查输入");
        }
        return JSONResult.success(stationLine);
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
