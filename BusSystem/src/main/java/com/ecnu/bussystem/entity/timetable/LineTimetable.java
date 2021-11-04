package com.ecnu.bussystem.entity.timetable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineTimetable {
    // 线路名字
    private String line;

    // 线路上每个站的时间表
    private List<StationTimetable> timetables;

    // 线路站点数
    private int stationCount = 0;

    public boolean isValid() {
        return (!line.equals("")) && (line != null) && (timetables != null) && (timetables.size() != 0);
    }
}
