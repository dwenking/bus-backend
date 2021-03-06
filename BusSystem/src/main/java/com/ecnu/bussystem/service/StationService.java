package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.Station;

import java.util.List;
import java.util.Map;

public interface StationService {
    // 根据站点id查找站点信息
    Station findStationById(String Id);

    // 根据站点name查找站点信息
    List<Station> findStationByPreciseName(String stationName);

    // 根据站点的模糊name查找站点信息（所有的首发和终点站）
    List<Station> findStationByVagueName(String stationName);

    // 根据站点模糊name查找经过站点的所有线路
    List<Station> findLineOfStationByVagueName(String stationName);

    // 根据站点id查找经过站点的所有线路
    Station findLineOfStationById(String stationId);

    /**
     * 找到两个站之间直达线路最多的两个站及线路数量
     *
     * @return {@code List<Map<String, String>>}
     */
    List<Map<String, Object>> findTop15StationPairs();

    /**
     * 根据id分组返回线路最多的15个站点
     *
     * @return {@code List<Map<String, Object>>}
     */
    List<Map<String, Object>> findTop15LineNumberofStations();


    /**
     * 查询地铁站的数量
     *
     * @return {@code List<String>}
     */
    List<String> findNumberOfMetroStations();

    /**
     * 统计始发站的数量
     *
     * @return {@code List<String>}
     */
    List<String> findNumberOfBeginStations();

    /**
     * 发现终点站的数量
     *
     * @return {@code List<String>}
     */
    List<String> findNumberOfEndStations();

    // 创建新的站点
    JSONObject createNewStations(List<Station> stationList);

}
