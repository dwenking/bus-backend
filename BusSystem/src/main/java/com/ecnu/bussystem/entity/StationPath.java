package com.ecnu.bussystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 一条线路的实体类，包括沿途的站点和线路信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationPath{
    /**
     * 路线长度
     */
    private int length;

    /**
     * 整条路线的时间
     */
    private int time;

    /**
     * 换乘次数
     */
    private int transferCnt;
    /**
     * 一条路线上的站信息，station[0]和station[1]之间的线路是stationRelationship[0]
     */
    private List<Station> stationList;

    /**
     * 一条路线上的站的路线信息
     */
    private List<StationRelationship> stationRelationships;

}
