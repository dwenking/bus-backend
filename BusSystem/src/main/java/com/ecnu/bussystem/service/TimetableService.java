package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.entity.timetable.LineTimetable;
import com.ecnu.bussystem.entity.timetable.StationTimetable;
import com.ecnu.bussystem.entity.timetable.Timetable;

import java.util.List;

public interface TimetableService {
    // 指定站点id、线路、时间，查找数量为count的班次
    StationTimetable findTimetableByIdAndTime(String time, String stationId, String lineName, String count);

    // 指定站点name、线路、时间，查找数量为count的班次
    List<StationTimetable> findTimetableByNameAndTime(String time, String stationName, String lineName, String count);

    // 指定站点id、时间，对每条线路返回数量为count的班次
    StationTimetable findAllTimetableByIdAndTime(String time, String stationId, String count);

    // 指定站点id、时间、查找范围，返回所有范围内的班次
    StationTimetable findTimetableByIdAndTimeRange(String time, int range, String stationId);

    // 指定站点name、时间、查找范围，返回所有范围内的班次
    List<StationTimetable> findTimetableByNameAndTimeRange(String time, int range, String stationName);

    // 指定线路名，返回班次信息（仅支持精确名称查找）
    LineTimetable findTimetableByName(String lineName);

    // 找出所有路线中运行时间最长的线路，倒序显示前15个线路
    List<JSONObject> findLinesOfLongestRuntime();

    //新建一个线路的时间表
    JSONObject createTimetable(List<Timetable> timetableList);
}
