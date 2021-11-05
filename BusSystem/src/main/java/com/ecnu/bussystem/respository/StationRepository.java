package com.ecnu.bussystem.respository;

import com.ecnu.bussystem.entity.Station;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends Neo4jRepository<Station, Long> {
    @Query("MATCH (c:vStations) WHERE c.myId=$id RETURN c")
    Station findStationById(String id);

    @Query("MATCH (c:vStations) WHERE $stationName = c.name RETURN c")
    List<Station> findStationByName(String stationName);

    @Query("match(n:vStations) where n.type=\"metro\" return distinct n.name")
    List<String> findMetroStations();

    @Query("match (n:vStations) where n.name contains \"始发站\" return distinct n.name")
    List<String> findBeginStations();

    @Query("match (n:vStations) where n.name contains \"终点站\" return distinct n.name")
    List<String> findEndStations();

}
