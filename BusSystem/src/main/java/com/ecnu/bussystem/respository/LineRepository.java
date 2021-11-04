package com.ecnu.bussystem.respository;


import com.ecnu.bussystem.entity.Line;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LineRepository extends Neo4jRepository<Line,Long> {
    @Query("MATCH (c:vLines) WHERE $name = c.name RETURN c")
    Line findLineByPerciseName(String name);

    //返回指定公交线路中两个站之间的运行时间
    @Query("MATCH p=(s:vStations)-[ *.. {name:$routename}]->(e:vStations) WHERE s.myId=$id1 AND e.myId=$id2 " +
            "with *,relationships(p) as r " +
            "unwind r as x return SUM(x.time)")
    Integer findTimebetweenTwoStations(String id1,String id2,String routename);
}
