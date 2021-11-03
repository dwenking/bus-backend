package com.ecnu.bussystem.service;

import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.StationLine;

import java.util.List;

public interface LineService {
    // 根据线路精确名称查找线路（指明上行和下行）
    Line findLineByPerciseName(String routeName);

    // 根据线路模糊名称查找线路（不指明上行还是下行）
    List<Line> findLineByVagueName(String routeName);

    // 根据线路的名称返回路线上的站
    StationLine findStationOfLineByPreciseName(String routeName);

    // 根据模糊路线的名称返回路线上的站
    List<StationLine> findStationOfLineByVagueName(String routeName);
}
