package com.ecnu.bussystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationTimetable {
    private String station;
    private List<Timetable> timetables;

    public boolean isValid() {
        return (station != null) && (!station.equals("")) && (timetables != null) && (timetables.size() != 0);
    }
}
