package com.ecnu.bussystem.controller;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.timetable.LineTimetable;
import com.ecnu.bussystem.entity.timetable.StationTimetable;
import com.ecnu.bussystem.service.TimetableServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/timetable")
public class TimetableController {
    @Autowired
    TimetableServiceImpl timetableService;

    // 根据站点id、线路名、时间查找符合要求的n条线路
    @GetMapping(path = "/timetable/for/id/{id}/{line}/{time}/{count}")
    public JSONResult<?> findTimetableByIdAndTime(
            @PathVariable(required = true) String id,
            @PathVariable(required = true) String line,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) String count
    ) {
        StationTimetable stationTimetable = timetableService.findTimetableByIdAndTime(time, id, line, count);
        if (stationTimetable == null || !stationTimetable.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到班次信息，请检查输入，站点id:" + id + " 线路：" + line + " 时间：" + time);
        }
        return JSONResult.success(stationTimetable);
    }

    // 根据站点name、线路名、时间查找符合要求的n条线路
    @GetMapping(path = "/timetable/for/name/{name}/{line}/{time}/{count}")
    public JSONResult<?> findTimetableByNameAndTime(
            @PathVariable(required = true) String name,
            @PathVariable(required = true) String line,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) String count
    ) {
        List<StationTimetable> stationTimetables = timetableService.findTimetableByNameAndTime(time, name, line, count);
        if (stationTimetables == null || stationTimetables.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到班次信息，请检查输入，站点name:" + name + " 线路：" + line + " 时间：" + time);
        }
        return JSONResult.success(stationTimetables);
    }

    // 指定站点id、时间，对每条线路返回数量为n的班次
    @GetMapping(path = "/all/timetable/for/id/{id}/{time}/{count}")
    public JSONResult<?> findAllTimetableByIdAndTime(
            @PathVariable(required = true) String id,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) String count
    ) {
        StationTimetable stationTimetable = timetableService.findAllTimetableByIdAndTime(time, id, count);
        if (stationTimetable == null || !stationTimetable.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到班次信息，请检查输入，站点id:" + id + " 时间：" + time);
        }
        return JSONResult.success(stationTimetable);
    }

    // 根据站点id、线路名、时间查找符合要求的n条线路
    @GetMapping(path = "/timetable/for/id/with/range/{id}/{time}/{range}")
    public JSONResult<?> findTimetableByIdAndTimeRange(
            @PathVariable(required = true) String id,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) int range
    ) {
        StationTimetable stationTimetable = timetableService.findTimetableByIdAndTimeRange(time, range, id);
        if (stationTimetable == null || !stationTimetable.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到班次信息，请检查输入，站点id:" + id + " 时间：" + time);
        }
        return JSONResult.success(stationTimetable);
    }

    // 根据站点name、线路名、时间查找符合要求的n条线路
    @GetMapping(path = "/timetable/for/name/with/range/{name}/{time}/{range}")
    public JSONResult<?> findTimetableByNameAndTimeRange(
            @PathVariable(required = true) String name,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) int range
    ) {
        List<StationTimetable> stationTimetables = timetableService.findTimetableByNameAndTimeRange(time, range, name);
        if (stationTimetables == null || stationTimetables.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到班次信息，请检查输入，站点name:" + name + " 时间：" + time);
        }
        return JSONResult.success(stationTimetables);
    }

    // 指定线路名，返回班次信息（仅支持精确名称查找）
    @GetMapping(path = "/timetable/for/line/{name}")
    public JSONResult<?> findTimetableByName(
            @PathVariable String name
    ) {
        LineTimetable lineTimetable = timetableService.findTimetableByName(name);
        if (lineTimetable == null || !lineTimetable.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到班次信息，请检查输入，线路name:" + name);
        }
        return JSONResult.success(lineTimetable);
    }

    // 找出所有路线中运行时间最长的线路，倒序显示前15个线路
    @GetMapping(path = "/line/of/longest/runtime")
    public JSONResult<?> findTimetableByName() {
        List<JSONObject> res = timetableService.findLinesOfLongestRuntime();
        if (res == null || res.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR, "未找到数据");
        }
        return JSONResult.success(res);
    }
}
