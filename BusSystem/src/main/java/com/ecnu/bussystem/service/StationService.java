package com.ecnu.bussystem.service;




import com.ecnu.bussystem.entity.Station;

import java.util.List;

public interface StationService {
    // 根据站点id查找站点信息
    Station findStationById(String Id);

    // 根据站点name查找站点信息
    List<Station> findStationByPreciseName(String stationName);

    // 根据站点的模糊name查找站点信息（所有的首发和终点站
    List<Station> findStationByVagueName(String stationName);



}
