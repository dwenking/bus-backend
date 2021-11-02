package com.ecnu.bussystem.respository;


import com.ecnu.bussystem.entity.Line;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LineRepository extends Neo4jRepository<Line,Long> {
    @Query("MATCH (c:vLines) WHERE $routeName = c.name RETURN c")
    Line findRouteByPerciseName(String routeName);
}
