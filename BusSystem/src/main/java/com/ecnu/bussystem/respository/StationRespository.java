package com.ecnu.bussystem.respository;

import com.ecnu.bussystem.entity.Station;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRespository extends Neo4jRepository<Station,Long> {
    @Query("MATCH (c:Station) WHERE $id IN c.idlist RETURN c")
    Station findStationById(String id);

    @Query("MATCH (c:Station) WHERE $stationName = c.name RETURN c")
    Station findStationByName(String stationName);
}
