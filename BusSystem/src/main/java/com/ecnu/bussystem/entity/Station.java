package com.ecnu.bussystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Node(labels = {"vStations"})
public class Station implements Serializable {
    // Station属性名必须和数据库里的属性名完全一样，因为代码里用到了json类型转换
    @Id
    @GeneratedValue
    private Long identity; // 系统生成的假id

    @Property(name = "name")
    private String name;

    @Property(name = "englishname")
    private String englishname;

    @Property(name = "type")
    private String type; // Station类型，有bus，train，metro，normal四种

    @Property(name = "myId")
    private String myId; // 真实使用的id

    private List<String> lines;

}
