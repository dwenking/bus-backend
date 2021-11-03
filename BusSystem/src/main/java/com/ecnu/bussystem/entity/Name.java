package com.ecnu.bussystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Node(labels = {"vNames"})
public class Name {
    @Id
    @GeneratedValue
    private Long identity; // 系统生成的假id

    @Property(name = "name")
    private String name;
}
