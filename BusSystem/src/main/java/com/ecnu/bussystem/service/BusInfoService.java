package com.ecnu.bussystem.service;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;

import java.util.List;

public interface BusInfoService {
    // 根据站点id查找站点信息
    Station findStationById(String Id);

    // 根据站点name查找站点信息
    Station findStationByName(String stationName);

    // 根据线路精确名称查找线路（指明上行和下行）
    StationLine findRouteByPerciseName(String routeName);

    // 根据线路名称模糊查找线路
    List<StationLine> findRouteByVagueName(String routeVagueName);

    // 根据站点名称查找经过线路
    List<StationLine> findRelatedRoutesByStationName(String stationName);
}
