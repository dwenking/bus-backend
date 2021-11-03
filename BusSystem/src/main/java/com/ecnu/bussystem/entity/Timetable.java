package com.ecnu.bussystem.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "timetable")
public class Timetable {
    @Id
    @JSONField(serialize = false)
    private String id;

    private String routeName;
    private String passTime;
    private String stationName;
    private String stationID;

    // 当给定时间时，显示还有几分钟到达站点
    private int minutes;
}
