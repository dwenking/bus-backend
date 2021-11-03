package com.ecnu.bussystem.service;

import com.ecnu.bussystem.entity.StationTimetable;
import com.ecnu.bussystem.entity.Timetable;

import java.util.List;

public interface TimetableService {
    // 指定站点id、线路、时间查找数量为count的班次
    StationTimetable findTimetableByIdAndTime(String time, String stationId, String lineName, String count);

    // 指定站点name、线路、时间查找数量为count的班次
    List<StationTimetable> findTimetableByNameAndTime(String time, String stationName, String lineName, String count);

    // 指定站点name、时间、查找范围，返回所有范围内的班次
    StationTimetable findTimetableByIdAndTimeRange(String time, int range, String stationId);

    // 指定站点name、时间、查找范围，返回所有范围内的班次
    List<StationTimetable> findTimetableByNameAndTimeRange(String time, int range, String stationName);
}
