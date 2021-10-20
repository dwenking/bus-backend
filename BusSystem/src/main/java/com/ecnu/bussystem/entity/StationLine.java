package com.ecnu.bussystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 存储线路信息
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationLine {
    private String name;
    private List<Station> stations;
}
