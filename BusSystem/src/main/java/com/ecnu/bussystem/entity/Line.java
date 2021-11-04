package com.ecnu.bussystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node(labels = {"vLines"})
public class Line implements Serializable, Comparable {

    @Id
    @GeneratedValue
    private Long identity; // 系统生成的假id

    @Property(name = "name")
    private String name;

    @Property(name = "directional")
    private Boolean directional;

    @Property(name = "startRuntime")
    private String startRuntime;

    @Property(name = "endRuntime")
    private String endRuntime;

    @Property(name = "interval")
    private Integer interval;

    @Property(name = "kilometer")
    private Double kilometer;

    @Property(name = "lineNumber")
    private String lineNumber;

    @Property(name = "onewayTime")
    private Integer onewayTime;

    @Property(name = "type")
    private String type;

    @Override
    public int compareTo(Object o) {
        Line line=(Line) o;
        if(this.getName().compareTo(line.getName())>0) {
            return 1;
        } else {
            return -1;
        }
    }
}
