package com.ecnu.bussystem.respository;

import com.ecnu.bussystem.entity.Station;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRespository extends Neo4jRepository<Station,Long> {
    @Query("MATCH (c:Station) WHERE c.stationid CONTAINS $id RETURN c")
    Station findStationById(String id);

    @Query("MATCH (c:Station)-[r]->() WHERE r.name = $routeName RETURN c")
    List<Station> findRouteByPerciseName(String routeName);
}
