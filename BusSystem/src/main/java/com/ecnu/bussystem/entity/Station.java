package com.ecnu.bussystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Node(labels = {"Station"})
public class Station implements Serializable {
    @Id
    @GeneratedValue
    private Long id; // 这个是系统生成的假id

    @Property(name = "name")
    private String name;

    @Property(name = "englishname")
    private String englishName;

    @Property(name = "stationid")
    private String stationId; // 真实使用的id

    @Relationship(type = "NEAR", direction = Relationship.Direction.OUTGOING)
    private Set<Station> nearStations = new HashSet<>();
}
