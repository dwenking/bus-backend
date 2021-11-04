package com.ecnu.bussystem.controller;

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
    @GetMapping(path = "/timetableforid/{id}/{line}/{time}/{count}")
    public JSONResult<?> findTimetableByIdAndTime(
            @PathVariable(required = true) String id,
            @PathVariable(required = true) String line,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) String count
    ) {
        StationTimetable stationTimetable = timetableService.findTimetableByIdAndTime(time, id, line, count);
        if (stationTimetable == null || !stationTimetable.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到班次信息，请检查输入，站点id:" + id + " 线路：" + line + " 时间：" + time);
        }
        return JSONResult.success(stationTimetable);
    }

    // 根据站点name、线路名、时间查找符合要求的n条线路
    @GetMapping(path = "/timetableforname/{name}/{line}/{time}/{count}")
    public JSONResult<?> findTimetableByNameAndTime(
            @PathVariable(required = true) String name,
            @PathVariable(required = true) String line,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) String count
    ) {
        List<StationTimetable> stationTimetables = timetableService.findTimetableByNameAndTime(time, name, line, count);
        if (stationTimetables == null || stationTimetables.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到班次信息，请检查输入，站点name:" + name + " 线路：" + line + " 时间：" + time);
        }
        return JSONResult.success(stationTimetables);
    }

    // 根据站点id、线路名、时间查找符合要求的n条线路
    @GetMapping(path = "/timetableforidwithrange/{id}/{time}/{range}")
    public JSONResult<?> findTimetableByIdAndTimeRange(
            @PathVariable(required = true) String id,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) int range
    ) {
        StationTimetable stationTimetable = timetableService.findTimetableByIdAndTimeRange(time, range, id);
        if (stationTimetable == null || !stationTimetable.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到班次信息，请检查输入，站点id:" + id + " 时间：" + time);
        }
        return JSONResult.success(stationTimetable);
    }

    // 根据站点name、线路名、时间查找符合要求的n条线路
    @GetMapping(path = "/timetablefornamewithrange/{name}/{time}/{range}")
    public JSONResult<?> findTimetableByNameAndTimeRange(
            @PathVariable(required = true) String name,
            @PathVariable(required = true) String time,
            @PathVariable(required = true) int range
    ) {
        List<StationTimetable> stationTimetables = timetableService.findTimetableByNameAndTimeRange(time, range, name);
        if (stationTimetables == null || stationTimetables.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到班次信息，请检查输入，站点name:" + name + " 时间：" + time);
        }
        return JSONResult.success(stationTimetables);
    }

    // 指定线路名，返回班次信息（仅支持精确名称查找）
    @GetMapping(path = "/timetableforline/{name}")
    public JSONResult<?> findTimetableByName(
            @PathVariable String name
    ) {
        LineTimetable lineTimetable = timetableService.findTimetableByName(name);
        if (!lineTimetable.isValid()) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到班次信息，请检查输入，线路name:" + name);
        }
        return JSONResult.success(lineTimetable);
    }
}
