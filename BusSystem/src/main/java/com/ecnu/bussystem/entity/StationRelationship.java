package com.ecnu.bussystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 站和站之间的关系实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationRelationship {
    private String name;
    private int weight;
    private int time;
    private String lineNumber;
}
