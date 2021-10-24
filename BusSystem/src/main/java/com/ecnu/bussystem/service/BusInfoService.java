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

    //求两个站之间的直达的路径，返回“路线名称”
    List<String> findTwoStationDirectRoutenameByName(String name1, String name2);

    //求两个站之间的直达的路径，返回“路线名称-路径数组”
    List<StationLine> findTwoStationDirectPathByName(String name1, String name2);

    //求一条线路上两个站点的数组
    List<StationLine> findTwoStationOnThisPathDirectPathByName(String routename, String name1, String name2);
}
