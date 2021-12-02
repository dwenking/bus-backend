package com.ecnu.bussystem.controller;


import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.entity.StationPath;
import com.ecnu.bussystem.service.LineServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/line")
public class LineController {
    @Autowired
    private LineServiceImpl lineService;


    @ApiOperation(value = "findLineByPerciseName根据线路精确名称查找线路", notes = "1根据线路精确名称查找线路（指明上下行）")
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

    @ApiOperation(value = "findLineByVagueName根据线路模糊名称查找线路", notes = "根据线路模糊名称查找线路（不指明上行还是下行）")
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


    @ApiOperation(value = "findTop15MostStationsRoutes根据站点数量对线路进行排序", notes = "16根据站点数量对线路进行排序，降序排列，显示前15条，线路含方向")
    @GetMapping(path = "/top15/most/station")
    public JSONResult<?> findTop15MostStationsRoutes() {
        List<Map<String, String>> mapList = lineService.findTop15MostStationsRoutes();
        if (mapList == null || mapList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(mapList);
    }


    @ApiOperation(value = "findOneWayStationsByRouteName统计某条线路的单行站", notes = "11B统计某条线路的单行站")
    @GetMapping(path = "/find/the/number/of/oneway/station/{name}")
    public JSONResult<?> findOneWayStationsByRouteName(
            @PathVariable String name
    ) {
        List<JSONObject> objectList = lineService.findOneWayStationsByRouteName(name);
        if (objectList == null || objectList.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(objectList);
    }

    @ApiOperation(value = "findTypeAndNumberOfLines分组统计各个类型线路的数量", notes = "12分组统计常规公交(包括干线、支线、城乡线、驳接线、社区线)、\n" +
            "快速公交(K字开头)、高峰公交(G字开头)、夜班公交(N字开头)的数量")
    @GetMapping(path = "/find/type/and/number/of/line")
    public JSONResult<?> findTypeAndNumberOfLines() {
        List<JSONObject> jsonObjects = lineService.findTypeAndNumberOfLines();
        if (jsonObjects == null || jsonObjects.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(jsonObjects);
    }


    @ApiOperation(value = "findTransferLines统计某个线路上每个站点可以换乘的线路", notes = "14查询换乘线路，站点根据id查找换乘路线。换乘线路数即线路停靠的所有站台停靠其他线路的数量的总和")
    @GetMapping(path = "/find/transfer/{name}")
    public JSONResult<?> findTransferLines(
            @PathVariable String name
    ) {
        List<JSONObject> res = lineService.findTransferLines(name);
        if (res == null || res.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "没有找到可换乘的线路");
        }
        return JSONResult.success(res);
    }

    // 删除某条线路并删除只有该线路经过的站点
    @DeleteMapping(path = "/delete")
    public JSONResult<?> deleteLineByPerciseName(String name) {
        JSONObject res = lineService.deleteLineByPerciseName(name);
        if (res == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到可删除线路");
        }
        return JSONResult.success(res);
    }


    @ResponseBody
    // 恢复某条线路并恢复只有该线路经过的站点
    @PostMapping(path = "/restore")
    public JSONResult<?> restoreLineByPerciseName( String name) {
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
    @ApiOperation(value = "findNotRepeating计算某条线路(带方向)的非重复系数",notes = "18计算某条线路(带方向)的非重复系数")
    @GetMapping(path = "/not/repeating/{name}")
    public JSONResult<?> findNotRepeating(
            @PathVariable String name
    ) {
        JSONObject res = lineService.findNotRepeating(name);
        if (res == null || res.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "不存在该线路，请重新输入");
        }
        return JSONResult.success(res);
    }


    //添加一条新的线路
    @RequestMapping(value = "/addNewLine", method = RequestMethod.POST, consumes = "application/json; charset=utf-8")
    public JSONResult<?> addNewLine(@RequestBody Line line){
        JSONObject ans = lineService.createNewLine(line);
        if (ans == null){
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "出错啦！");
        }
        return JSONResult.success(ans);
    }


    @ApiOperation(value = "findShortestPathByName根据id求出两站之间的最短路径路线", notes = "5.1使用id查询某两个站台之间的最短路径，仅考虑两个站点之间距离不超过10的最短路径")
    @GetMapping(path = "/get/shortest/path/id/{id1}/{id2}")
    public JSONResult<?> findShortestPathById(
            @PathVariable String id1,
            @PathVariable String id2
    ) {
        List<JSONObject> stations = lineService.findShortestPathById(id1, id2);
        if (stations == null || stations.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "不存在距离不超过10的最短路径");
        }
        return JSONResult.success(stations);
    }

    @ApiOperation(value = "findShortestPathByName根据name求出两站之间的最短路径路线", notes = "5.2使用name查询某两个站台之间的最短路径，仅考虑两个站点之间距离不超过10的最短路径")
    @GetMapping(path = "/get/shortest/path/name/{name1}/{name2}")
    public JSONResult<?> findShortestPathByName(
            @PathVariable String name1,
            @PathVariable String name2
    ) {
        List<JSONObject> stations = lineService.findShortestPathByName(name1, name2);
        if (stations == null || stations.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "不存在距离不超过10的最短路径");
        }
        return JSONResult.success(stations);
    }

    @ApiOperation(value = "findMinTimePathByNameREDUCE根据name求出两站之间的最少时间路线(REDUCE)", notes = "5.3如果有更多细节考虑(最少时间)，使用reduce函数计算，仅考虑两个站点之间距离不超过10的用时最少路径")
    @GetMapping(path = "/get/minTime/path/name/reduce/{name1}/{name2}")
    public JSONResult<?> findMinTimePathByNameREDUCE(
            @PathVariable String name1,
            @PathVariable String name2
    ) {
        List<JSONObject> stations = lineService.findMinTimePathByName_REDUCE(name1, name2);
        if (stations == null || stations.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "不存在距离不超过10的用时最少路径");
        }
        return JSONResult.success(stations);
    }

    @ApiOperation(value = "findMinTimePathByNameAPOC根据name求出两站之间的最少时间路线(APOC)", notes = "5.3如果有更多细节考虑(最少时间)，使用apoc的dijstra函数直接计算，考虑所有的以时间为权重的最短路径，但是根据相同权重进行去重")
    @GetMapping(path = "/get/minTime/path/name/apoc/{name1}/{name2}")
    public JSONResult<?> findMinTimePathByNameAPOC(
            @PathVariable String name1,
            @PathVariable String name2
    ) {
        List<JSONObject> stations = lineService.findMinTimePathByName_APOC(name1, name2);
        if (stations == null || stations.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "不存在最少用时路径");
        }
        return JSONResult.success(stations);
    }

    @ApiOperation(value = "findMinTimePathByNameALL根据name求出两站之间的最少时间路线(APOC+REDUCE)", notes = "5.3如果有更多细节考虑(最少时间),使用apoc和reduce函数，仅考虑两个站点之间距离不超过10的最少用时路径")
    @GetMapping(path = "/get/minTime/path/name/all/{name1}/{name2}")
    public JSONResult<?> findMinTimePathByNameALL(
            @PathVariable String name1,
            @PathVariable String name2
    ) {
        List<JSONObject> stations = lineService.findMinTimePathByName_ALL(name1, name2);
        if (stations == null || stations.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "不存在长度不超过10的最短时间路径");
        }
        return JSONResult.success(stations);
    }

    @ApiOperation(value = "findMinTransferPathByName根据name求出两站之间的最少换乘路线", notes = "5.3如果有更多细节考虑(最少换乘)，仅考虑长度不超过10的两点之间的简单路径")
    @GetMapping(path = "/get/minTransfer/path/name/{name1}/{name2}")
    public JSONResult<?> findMinTransferPathByName(
            @PathVariable String name1,
            @PathVariable String name2
    ) {
        List<JSONObject> stations = lineService.findMinTransferPathByName(name1, name2);
        if (stations == null || stations.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "不存在长度不超过10的最少换乘路径");
        }
        return JSONResult.success(stations);
    }

    @ApiOperation(value = "findAllShortestPathByName求出根据name两站之间的所有最短路径", notes = "显示所有最短路的信息，仅考虑两点之间距离不超过10的最短路径")
    @GetMapping(path = "/get/shortest/all/path/name/{name1}/{name2}")
    public JSONResult<?> findAllShortestPathByName(
            @PathVariable String name1,
            @PathVariable String name2
    ) {
        List<StationPath> stationpaths = lineService.findAllShortestPathByName(name1, name2);
        if (stationpaths == null || stationpaths.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "不存在路线长度不超过10的最短路径");
        }
        List<String>routes=new ArrayList<>();
        for(StationPath stationPath:stationpaths){
            String tmp="";
            for(int i=0;i<stationPath.getStationList().size();i++){
                if(i!=stationPath.getStationList().size()-1){
                    tmp+="["+stationPath.getStationList().get(i).getName()+"]-";
                    tmp+=stationPath.getStationRelationships().get(i).getName()+"T:"+stationPath.getStationRelationships().get(i).getTime()+"->";
                }
                else {
                    tmp+="["+stationPath.getStationList().get(i).getName()+"]";
                }
            }
            tmp+=" time: "+stationPath.getTime()+" transferCnt: "+stationPath.getTransferCnt();
            routes.add(tmp);
        }

        return JSONResult.success(routes);
    }
}
