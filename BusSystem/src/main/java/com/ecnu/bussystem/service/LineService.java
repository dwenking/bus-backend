package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.StationLine;

import java.util.List;
import java.util.Map;

public interface LineService {
    // 根据线路精确名称查找线路（指明上行和下行）
    Line findLineByPerciseName(String routeName);

    // 根据线路模糊名称查找线路（不指明上行还是下行）
    List<Line> findLineByVagueName(String routeName);

    // 根据线路的名称返回路线上的站
    StationLine findStationOfLineByPreciseName(String routeName);

    // 根据模糊路线的名称返回路线上的站
    List<StationLine> findStationOfLineByVagueName(String routeName);

    // 根据站点数量对线路进行排序
    List<Map<String, String>> findTop15MostStationsRoutes();

    //找到两个线路之间的重复站点数
    List<Map<String, String>> findDuplicateStations(String lineName1, String lineName2);

    //根据起始站点和公交线路名返回路线上的站（方向，站点和运行时间）
    List<StationLine> findAlongStationLineByStartAndEndName(String name1,String name2,String routename);

    //根据两个站点求两个站点之间有没有直达路径
    List<StationLine> findDirectPathBetweenTwoStations(String name1,String name2);

    //根据两个站点返回之间直达线路的路径和方向
    List<JSONObject> findDirectPathNameBetweenTwoStations(String name1, String name2);
}

