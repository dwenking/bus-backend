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

    @Query("MATCH (n:vStations) " +
            "WHERE NOT EXISTS((n)-[]->(:vLines)) " +
            "REMOVE n:vStations " +
            "SET n:deleteStation " +
            "RETURN n.name")
    List<String> deleteStationWithNoLine();

    @Query("MATCH (n:deleteStation) " +
            "WHERE EXISTS((n)-[]->({name: $lineName})) " +
            "REMOVE n:deleteStation " +
            "SET n:vStations " +
            "RETURN n.name")
    List<String> restoreStationInLine(String lineName);

    @Query("MATCH (n:vStations {myId: $id1})-[r:vNEAR {name: $line}]->(m:vStations {myId: $id2}) DELETE r")
    void deleteLineBetweenStation(String id1, String id2, String line);

    @Query("MATCH (n:vStations {myId: $before})-[r:vNEAR {name: $line}]->(s:vStations {myId: $oldId}),  (m:vStations {myId: $newId}) " +
            "WHERE EXISTS(m.name) " +
            "CREATE (n)-[r1:vNEAR {name: r.name, lineNumber: r.lineNumber, time: r.time, weight:r.weight}]->(m) " +
            "RETURN r1.name")
    List<String> addLineBeforeStation(String before, String newId, String oldId, String line);

    @Query("MATCH (s:vStations {myId: $oldId})-[r:vNEAR {name: $line}]->(m:vStations {myId: $after}),  (n:vStations {myId: $newId}) " +
            "WHERE EXISTS(n.name) " +
            "CREATE (n)-[r1:vNEAR {name: r.name, lineNumber: r.lineNumber, time: r.time, weight:r.weight}]->(m) " +
            "RETURN r1.name")
    List<String> addLineAfterStation(String after, String newId, String oldId, String line);

    @Query("match (c:vStations)-[]->(n:vLines) where $lineName = n.name return c")
    List<Station> findStationByLine(String lineName);

    @Query("MATCH (n:vStations)-[]->(l:vLines) where n.myId = $id  RETURN l.name")
    List<String> findLineByStationId(String id);
}
