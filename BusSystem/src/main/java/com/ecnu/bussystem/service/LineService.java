package com.ecnu.bussystem.service;

import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.StationLine;

import java.util.List;

public interface LineService {
    // 根据线路精确名称查找线路（指明上行和下行）
    Line findRouteByPerciseName(String routeName);

    //根据线路模糊名称查找线路（不指明上行还是下行）
    List<Line> findRouteByVagueName(String routeName);

    //根据线路的名称返回路线上的站
    StationLine findStationlineByPreciseRouteName(String routeName);

    //根据模糊路线的名称返回路线
    List<StationLine> findStationlineByVagueRouteName(String routeName);

}
