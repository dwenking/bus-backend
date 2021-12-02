package com.ecnu.bussystem.respository;


import com.ecnu.bussystem.entity.Line;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LineRepository extends Neo4jRepository<Line, Long> {
    @Query("MATCH (c:vLines) WHERE $name = c.name RETURN c")
    Line findLineByPerciseName(String name);

    @Query("MATCH p=(s:vStations)-[ *.. {name:$routename}]->(e:vStations) WHERE s.myId=$id1 AND e.myId=$id2 " +
            "with *,relationships(p) as r " +
            "unwind r as x return SUM(x.time)")
    Integer findTimebetweenTwoStations(String id1, String id2, String routename);

    @Query("MATCH (n:vLines) " +
            "WHERE n.name = $name " +
            "REMOVE n:vLines " +
            "SET n:deleteLine " +
            "RETURN n.name")
    String deleteLineByPerciseName(String name);

    @Query("MATCH (n:deleteLine) " +
            "WHERE n.name = $name " +
            "REMOVE n:deleteLine " +
            "SET n:vLines " +
            "RETURN n.name")
    String restoreLineByPerciseName(String name);

    // 删除与vLines节点的关系
    @Query("MATCH (n:vStations {myId: $stationId})-[r]->(m:vLines {name: $lineName}) WHERE EXISTS(m.directional) AND EXISTS(n.name) DELETE r RETURN type(r)")
    String deleteStationOfLine(String stationId, String lineName);

    // 添加与vLines节点的关系
    @Query("MATCH (n:vStations {myId: $stationId}), (m:vLines {name: $lineName}) " +
            "WHERE EXISTS(m.directional) " +
            "CREATE (n)-[r:in]->(m) RETURN type(r)")
    String addStationOfInLine(String stationId, String lineName);

    @Query("MATCH (n:vStations {myId: $stationId}), (m:vLines {name: $lineName}) " +
            "WHERE EXISTS(m.directional) " +
            "CREATE (n)-[r:begin]->(m) RETURN type(r)")
    String addStationOfBeginLine(String stationId, String lineName);

    @Query("MATCH (n:vStations {myId: $stationId}), (m:vLines {name: $lineName}) " +
            "WHERE EXISTS(m.directional) " +
            "CREATE (n)-[r:end]->(m) RETURN type(r)")
    String addStationOfEndLine(String stationId, String lineName);

    @Query("MATCH (n:vStations {myId: $stationId1})-[r]->(m:vStations {myId: $stationId2}) " +
            "WITH count(r) as number RETURN number")
    Integer findDirectPathWithDirection(String stationId1, String stationId2);

}
