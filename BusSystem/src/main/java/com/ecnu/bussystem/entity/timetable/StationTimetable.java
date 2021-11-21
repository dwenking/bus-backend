package com.ecnu.bussystem.entity.timetable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationTimetable implements Comparable {
    private String station;
    private String id;
    private List<Timetable> timetables;
    // 该实例的timetable数目
    private int timetableCount = 0;
    // 该station在整条路线上的顺序
    private Integer stationIndex = -1;

    public boolean isValid() {
        return (station != null) && (!station.equals("")) && (timetables != null) && (timetables.size() != 0);
    }

    @Override
    public int compareTo(Object o) {
        StationTimetable stationTimetable = (StationTimetable) o;
        if (this.getStationIndex().compareTo(((StationTimetable) o).getStationIndex()) > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
