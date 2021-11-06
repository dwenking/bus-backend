package com.ecnu.bussystem.entity.timetable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "runtime")
public class RuntimeTable {
    @Id
    private String id;
    //线路名字
    private String name;
    //线路首班车发车时间
    private String firstBus;
    //线路末班车发车时间
    private String lastBus;
    //线路运行时间==lastBus-firstBus
    private String runtime;
    //便于runtime排序的指标
    private Number rate;
}
