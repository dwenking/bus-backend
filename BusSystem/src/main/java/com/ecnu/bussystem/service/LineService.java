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


    /**
     * 找到两个线路之间重复的站点数
     *
     * @param lineName1 行name1
     * @param lineName2 行name2
     * @return {@code List<Map<String, String>>}
     */
    List<JSONObject> findDuplicateStations(String lineName1, String lineName2);

    /**
     * 发现在站逐开始和结束的名字
     * 根据起始站点和公交线路名返回路线上的站（方向，站点和运行时间）
     *
     * @param name1     name1
     * @param name2     name2
     * @param routename routename
     * @return {@code List<StationLine>}
     */
    List<StationLine> findAlongStationLineByStartAndEndName(String name1, String name2, String routename);

    /**
     * 找到两个站之间的直接路径
     * 根据两个站点求两个站点之间有没有直达路径
     *
     * @param name1 name1
     * @param name2 name2
     * @return {@code List<StationLine>}
     */
    List<StationLine> findDirectPathBetweenTwoStations(String name1, String name2);

    /**
     * 找到两个站之间的直接路径名
     *
     * @param name1 name1
     * @param name2 name2
     * @return {@code List<JSONObject>}
     */
    List<JSONObject> findDirectPathNameBetweenTwoStations(String name1, String name2);


    /**
     * 找到所有单行站的数量，根据上下行的路线上的站进行对比
     *
     * @return {@code List<Map<String, String>>}
     */
    JSONObject findTheNumberOfOneWayStations();


    /**
     * 按照路线的type分组计算数量
     *
     * @return {@code List<JSONObject>}
     */
    List<JSONObject> findTypeAndNumberOfLines();

    /**
     * 统计某个线路上每个站点可以换乘的线路，站点根据id查找换乘路线
     *
     * @param routeName name
     * @return {@code List<JSONObject>}
     */
    List<JSONObject> findTransferLines(String routeName);

    /**
     * 计算某条线路的非重复系数
     *
     * @param routeName name
     * @return {@code JSONObject}
     */
    JSONObject findNotRepeating(String routeName);
}

