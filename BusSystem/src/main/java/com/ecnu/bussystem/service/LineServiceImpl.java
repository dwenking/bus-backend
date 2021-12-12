package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.Neo4jUtil;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.entity.StationPath;
import com.ecnu.bussystem.respository.LineRepository;
import com.ecnu.bussystem.respository.StationRepository;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.*;

@Service
public class LineServiceImpl implements LineService {
    @Autowired
    StationServiceImpl stationService;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    LineRepository lineRepository;

    @Resource
    Driver neo4jDriver;

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Line findLineByPerciseName(String routeName) {
        return lineRepository.findLineByPerciseName(routeName);
    }

    @Override
    public List<Line> findLineByVagueName(String routeName) {
        if (routeName == null || routeName.equals("")) {
            return null;
        }

        String regex = "^[a-z0-9A-Z]+$";
        if (routeName.endsWith("上行") || routeName.endsWith("下行")) {
            routeName = routeName.substring(0, routeName.length() - 2);
        } else if (routeName.matches(regex)) {
            routeName += "路";
        }

        String routename1 = routeName + "上行";
        String routename2 = routeName + "下行";

        Line line1 = lineRepository.findLineByPerciseName(routename1);
        Line line2 = lineRepository.findLineByPerciseName(routename2);
        Line line = lineRepository.findLineByPerciseName(routeName);
        List<Line> lines = new ArrayList<>();

        if (line1 != null) {
            lines.add(line1);
        }
        if (line2 != null) {
            lines.add(line2);
        }
        if (line != null) {
            lines.add(line);
        }

        return lines;
    }

    @Override
    public StationLine findStationOfLineByPreciseName(String routeName) {
        if (routeName == null || routeName.equals("")) {
            return null;
        }

        StationLine stationLine = new StationLine();
        stationLine.setName(routeName);

        List<Station> stations = new ArrayList<>();
        Station beginStation;
        Station endStation = null;
        Line lineNode = null;

        try (Session session = neo4jDriver.session()) {
            // 找到begin的node
            String cypher = String.format("MATCH (n:vStations)-[r]-(m:vLines) WHERE type(r)='begin' AND m.name='%s' RETURN n", routeName);
            Result result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                beginStation = JSONObject.parseObject(mapStrings.get(0), Station.class);
            } catch (Exception e) {
                System.out.println("没有找到BeginNode, name:" + routeName);
                return stationLine;
            }

            // 找到end的node
            cypher = String.format("MATCH (n:vStations)-[r]-(m:vLines) WHERE type(r)='end' AND m.name='%s' RETURN n", routeName);
            result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                endStation = JSONObject.parseObject(mapStrings.get(0), Station.class);
            } catch (Exception e) {
                System.out.println("没有找到endNode, name:" + routeName);
                return stationLine;
            }

            // 确定线路
            cypher = String.format("MATCH (n:vLines) WHERE n.name='%s' RETURN n", routeName);
            result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                lineNode = JSONObject.parseObject(mapStrings.get(0), Line.class);
            } catch (Exception e) {
                System.out.println("没有找到lineNode, name:" + routeName);
                return stationLine;
            }

            //根据begin和end找路线
            cypher = String.format("MATCH p=(s:vStations)-[r *.. {name:'%s'}]->(e:vStations) WHERE '%s' = s.myId AND '%s'= e.myId RETURN p ORDER BY length(p) DESC", routeName, beginStation.getMyId(), endStation.getMyId());
            result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromPathResult(result);
                for (String string : mapStrings) {
                    //json字符串直接转给java对象
                    Station station = JSONObject.parseObject(string, Station.class);
                    stations.add(station);
                }
            } catch (Exception e) {
                System.out.println("没有找到Record, name:" + routeName);
                return stationLine;
            }
        }

        stationLine.setStations(stations);
        stationLine.setDirectional(lineNode.getDirectional());
        return stationLine;
    }

    @Override
    public List<StationLine> findStationOfLineByVagueName(String routeName) {
        if (routeName == null || routeName.length() == 0) {
            return null;
        }

        List<StationLine> stationLines = new ArrayList<>();
        String regex = "^[a-z0-9A-Z]+$";
        if (routeName.matches(regex)) {
            routeName += "路";
        } else if (routeName.endsWith("上行") || routeName.endsWith("下行")) {
            routeName = routeName.substring(0, routeName.length() - 2);
        }

        System.out.println(routeName);

        String routename1 = routeName + "上行";
        String routename2 = routeName + "下行";

        StationLine stationLine = this.findStationOfLineByPreciseName(routeName);
        StationLine stationLine1 = this.findStationOfLineByPreciseName(routename1);
        StationLine stationLine2 = this.findStationOfLineByPreciseName(routename2);
        if (stationLine1 != null && stationLine1.isValid()) {
            stationLines.add(stationLine1);
        }
        if (stationLine2 != null && stationLine2.isValid()) {
            stationLines.add(stationLine2);
        }
        if (stationLine != null && stationLine.isValid()) {
            stationLines.add(stationLine);
        }

        return stationLines;
    }

    @Override
    public List<Map<String, String>> findTop15MostStationsRoutes() {
        List<Map<String, String>> mapList = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            // key为routename-number
            String cypher = String.format("MATCH (n:vStations)-[]->(l:vLines) " +
                    "with l.name as routename, count(n.myId) as number " +
                    "order by number DESC limit 15 return routename,number");
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                //将records映射为map对象
                Map<String, Object> objectMap = record.asMap();
                Map<String, String> map = new HashMap<>();
                for (String cur : objectMap.keySet()) {
                    map.put(cur, objectMap.get(cur).toString());
                }
                mapList.add(map);
            }
        }
        return mapList;
    }

    @Override
    public List<JSONObject> findDuplicateStations(String lineName1, String lineName2) {
        List<JSONObject> res = new ArrayList<>();
        // 直接利用cypher返回所需数据
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("MATCH (m:vStations)-[]->(n:vLines{name:'%s'}) " +
                    "where (m)-[]->(:vLines{name:'%s'}) " +
                    "with m.name as stationName, m.myId as stationID, m.englishname as english " +
                    "return stationName,stationID,english", lineName1, lineName2);
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                Map<String, Object> objectMap = record.asMap();
                JSONObject map = new JSONObject();
                for (String cur : objectMap.keySet()) {
                    map.put(cur, objectMap.get(cur).toString());
                }
                res.add(map);
            }
        }

        return res;
    }

    @Override
    public List<StationLine> findAlongStationLineByStartAndEndName(String name1, String name2, String routename) {
        List<Line> lines = this.findLineByVagueName(routename);
        List<StationLine> stationLineList = new ArrayList<>();
        for (Line line : lines) {
            stationLineList.addAll(this.findAlongStationLineByStartAndEndNameandPreciseRoutename(name1, name2, line.getName()));
        }
        return stationLineList;
    }

    @Override
    public List<StationLine> findAlongStationLineByStartAndEndNameandPreciseRoutename(String name1, String name2, String routename) {
        List<StationLine> stationLineList = new ArrayList<>();

        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match (k:vNames{name:'%s'})-[]-(n1:vStations),(m:vNames{name:'%s'})-[]-(n2:vStations)\n" +
                    "match p= (n1)-[:vNEAR*..{name:'%s'}]->(n2)\n" +
                    "with p unwind(relationships(p)) as r\n" +
                    "return p,sum(r.time) as runtime", name1, name2, routename);
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                //每一组值代表一个路线
                StationLine stationLine = new StationLine();
                stationLine.setName(routename);
                List<Station> stations = new ArrayList<>();
                Map<String, Object> map = record.asMap();
                //返回的是path和time
                for (String cur : map.keySet()) {
                    if (cur.equals("p")) {
                        Path path = (Path) map.get(cur);
                        for (Node node : path.nodes()) {
                            Station station = Neo4jUtil.getStationFromNode(node);
                            stations.add(station);
                        }
                        stationLine.setStations(stations);
                    } else {
                        Number time = (Number) map.get(cur);
                        stationLine.setTime(time.intValue());
                    }
                }
                stationLineList.add(stationLine);
            }
        }
        return stationLineList;
    }

    @Override
    public List<JSONObject> findDirectPathNameBetweenTwoStations(String name1, String name2) {
        List<JSONObject> objects = new ArrayList<>();
        //考虑方向和去重
        objects.addAll(this.findDirectPathNameBetweenTwoStationsByDirection(name1, name2));
        objects.addAll(this.findDirectPathNameBetweenTwoStationsByDirection(name2, name1));
        List<JSONObject> ans = new ArrayList<>();
        Set<String> pathset = new HashSet<>();
        for (JSONObject o : objects) {
            JSONObject thisPath=new JSONObject();
            String p=(String) o.get("routename") + o.get("name1")+o.get("name2");
            if((boolean) o.get("d")==false){
                String rp=(String) o.get("routename") + o.get("name2")+o.get("name1");
                if(pathset.contains(p)==false && pathset.contains(rp)==false){
                    pathset.add(p);
                    pathset.add(rp);
                    thisPath.put("name", (String) o.get("routename"));
                    thisPath.put("directional", (String)o.get("name1")+"<->"+o.get("name2"));
                    ans.add(thisPath);
                }
            }
            else if(pathset.contains(p)==false){
                pathset.add(p);
                thisPath.put("name", (String) o.get("routename"));
                thisPath.put("directional", (String)o.get("name1")+"->"+o.get("name2"));
                ans.add(thisPath);
            }
        }
        return ans;
    }

    @Override
    public List<JSONObject> findDirectPathNameBetweenTwoStationsByDirection(String name1, String name2) {
        List<JSONObject> objects = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match (s1:vStations{name:'%s'})-[r]->()\n" +
                    "match ()-[r2]->(s2:vStations{name:'%s'})\n" +
                    "where r.name=r2.name\n" +
                    "with r.name as line,s1.name as name1, s2.name as name2\n" +
                    "where exists((s1)-[:vNEAR*..{name:line}]->(s2)) \n" +
                    "return name1,line,name2", name1, name2);
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                JSONObject thisPath = new JSONObject();
                Map<String, Object> map = record.asMap();
                //返回的是path和time
                String startName = null;
                String endName = null;
                String thisRoutename = null;
                for (String cur : map.keySet()) {
                    if (cur.equals("name1")) {
                        startName = (String) map.get(cur);
                    } else if (cur.equals("name2")) {
                        endName = (String) map.get(cur);
                    } else {
                        thisRoutename = (String) map.get(cur);
                    }
                }
                //判断是否为环线
                Line line = this.findLineByPerciseName(thisRoutename);
                if (line.getDirectional() == false) {
                    thisPath.put("routename", thisRoutename);
                    thisPath.put("name1", startName);
                    thisPath.put("name2", endName);
                    thisPath.put("d",false);
                    objects.add(thisPath);
                } else {
                    thisPath.put("routename", thisRoutename);
                    thisPath.put("name1", startName);
                    thisPath.put("name2", endName);
                    thisPath.put("d",true);
                    objects.add(thisPath);
                }
            }
        }
        return objects;
    }

    @Override
    public List<JSONObject> findOneWayStationsByRouteName(String name) {
        name=name.replace("上行","");
        name=name.replace("下行","");
        Line line=this.findLineByPerciseName(name);
        if(line!=null && line.getDirectional()==false)
            return null;
        //存储这条线路上的单行站的名称
        List<JSONObject> objectList = new ArrayList<>();

        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match (s:vStations)-[:vNEAR{name:'%s'}]-()\n" +
                    "match (s1:vStations)-[:vNEAR{name:'%s'}]-() \n" +
                    "with collect (distinct s.name) as a, collect(distinct s1.name) as b \n" +
                    "return [x in a where not x in b] as up_single, [x in b where not x in a] as down_single\n", name+"上行", name+"下行");
            List<String> stringList=new ArrayList<>();
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                Map<String, Object> map = record.asMap();
                for (String cur : map.keySet()) {
                    if (cur.equals("up_single")) {
                        List<String> strings= (List<String>) map.get(cur);
                        stringList.addAll(strings);
                    } else if (cur.equals("down_single")) {
                        List<String> strings = (List<String>) map.get(cur);
                        stringList.addAll(strings);
                    }
                }
            }
            for (String s:stringList){
                JSONObject objects = new JSONObject();
                objects.put("name", s);
                objectList.add(objects);
            }

        }
        return  objectList;
    }

    @Override
    public List<JSONObject> findTypeAndNumberOfLines() {
        //返回的list包括type和number
        List<JSONObject> res = new ArrayList<>();
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("C", "干线");
        typeMap.put("S", "社区线");
        typeMap.put("N", "夜班线");
        typeMap.put("G", "高峰线");
        typeMap.put("K", "快速公交");
        typeMap.put("Z", "支线");
        typeMap.put("B", "斑驳线");
        typeMap.put("CC", "城乡线");
        try (Session session = neo4jDriver.session()) {
            // 找到单行的，路线lineNumber相同的路线对
            String cypher = String.format("MATCH (n:vLines) \n" +
                    "with n.type as type, count(distinct n.lineNumber) as number\n" +
                    "return type, number order by number desc");
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                Map<String, Object> map = record.asMap();
                JSONObject resline = new JSONObject();
                for (String cur : map.keySet()) {
                    if (cur.equals("type")) {
                        resline.put("type", typeMap.get(map.get(cur)));
                    } else {
                        resline.put("number", map.get(cur));
                    }
                }
                res.add(resline);
            }
        }
        return res;
    }

    @Override
    public List<JSONObject> findTransferLines(String routeName) {
        List<JSONObject> res = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match ()-[r:vNEAR]-(n:vStations)-[r2:vNEAR{name:'%s'}]-() " +
                    "where r.name<>r2.name return n.name as TransferStation, n.myId as TransferStationID," +
                    "collect(DISTINCT r.name) as TransferLines", routeName);
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                Map<String, Object> map = record.asMap();
                JSONObject resLine = new JSONObject();
                for (String cur : map.keySet()) {
                    resLine.put(cur, map.get(cur));
                }
                res.add(resLine);
            }
        }
        return res;
    }


    @Override
    public JSONObject deleteLineByPerciseName(String name) {
        JSONObject res = new JSONObject();

        String line = lineRepository.deleteLineByPerciseName(name);
        if (line == null || "".equals(line)) {
            return null;
        }

        // 删除只有这一条线路的站点
        List<String> stations = stationRepository.deleteStationWithNoLine();

        res.put("line", line);
        if (stations != null && stations.size() > 0) {
            res.put("stations", stations);
        }

        return res;
    }

    @Override
    public JSONObject restoreLineByPerciseName(String name) {
        JSONObject res = new JSONObject();

        String line = lineRepository.restoreLineByPerciseName(name);
        if (line == null || "".equals(line)) {
            return null;
        }

        // 恢复只有这一条线路的站点
        List<String> stations = stationRepository.restoreStationInLine(line);

        res.put("line", line);
        if (stations != null && stations.size() > 0) {
            res.put("stations", stations);
        }

        return res;
    }

    @Override
    public StationLine replaceStationInLine(String name, String oldId, String newId) {
        StationLine stationLine = null;
        Station before = null, after = null;
        String cypher;
        Result result;

        try (Session session = neo4jDriver.session()) {
            // 找到before的node
            cypher = String.format("MATCH (n:vStations)-[r:vNEAR]->(m:vStations) WHERE m.myId='%s' AND r.name='%s' RETURN n", oldId, name);
            result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                before = JSONObject.parseObject(mapStrings.get(0), Station.class);
            } catch (Exception e) {
                System.out.println("没有找到before node");
            }

            // 找到after的node
            cypher = String.format("MATCH (m:vStations)-[r:vNEAR]->(n:vStations) WHERE m.myId='%s' AND r.name='%s' RETURN n", oldId, name);
            result = session.run(cypher);

            try {
                List<String> mapStrings = Neo4jUtil.getJsonStringFromNodeResult(result);
                after = JSONObject.parseObject(mapStrings.get(0), Station.class);
            } catch (Exception e) {
                System.out.println("没有找到after node");
            }

            if (before == null && after == null) {
                return null;
            }

            // 更新与vLines关系
            String type = lineRepository.deleteStationOfLine(oldId, name);
            if ("begin".equals(type)) {
                lineRepository.addStationOfBeginLine(newId, name);
            } else if ("end".equals(type)) {
                lineRepository.addStationOfEndLine(newId, name);
            } else {
                lineRepository.addStationOfInLine(newId, name);
            }

            // 更新与before、after关系
            if (before != null) {
                stationRepository.addLineBeforeStation(before.getMyId(), newId, oldId, name);
                stationRepository.deleteLineBetweenStation(before.getMyId(), oldId, name);
            }
            if (after != null) {
                stationRepository.addLineAfterStation(after.getMyId(), newId, oldId, name);
                stationRepository.deleteLineBetweenStation(oldId, after.getMyId(), name);
            }
        }

        stationLine = findStationOfLineByPreciseName(name);
        return stationLine;
    }

    @Override
    public JSONObject findNotRepeating(String routeName) {
        JSONObject res = new JSONObject();
        StationLine stationLine = this.findStationOfLineByPreciseName(routeName);
        List<Station> stations = stationLine.getStations();
        if (stations == null || stations.size() < 1) {
            return null;
        }
        int cnt = stations.size();
        // 根据id查找站点间的线路，并且区分方向，把非重复系数的总和存储在average中
        Double average = 0.0;
        for (int i = 0; i < cnt - 1; i++) {
            String id1 = stations.get(i).getMyId();
            String id2 = stations.get(i + 1).getMyId();
            int routes = lineRepository.findDirectPathWithDirection(id1, id2);
            average += 1.0 / routes;
        }
        //求平均非重复系数并保留两位小数
        average = average / (cnt - 1);
        DecimalFormat df = new DecimalFormat("0.00");
        res.put("lineName", routeName);
        res.put("number", df.format(average));
        return res;
    }


    @Override
    public JSONObject createNewLine(Line line) {
        JSONObject res = new JSONObject();
        try (Session session = neo4jDriver.session()) {
            //根据线路基本信息创建vLine节点
            String cypher = String.format("CREATE (n:vLines \n" +
                            "{name:'%s', directional:%b,kilometer:%.1f," +
                            "lineNumber:'%s', onewayTime:%d, route:'%s'," +
                            "runTime:'%s', type:'C',interval:%d})\n" +
                            "return n", line.getName(), line.getDirectional(), line.getKilometer(), line.getLineNumber(),
                    line.getOnewayTime(), line.getRoute(), line.getRuntime(), line.getInterval());
            Result result = session.run(cypher);
        }
        res.put("line", line);

        return res;
    }


    @Override
    public List<JSONObject> findShortestPathById(String id1, String id2) {
        List<JSONObject> ansObjectList = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations{myId:'%s'})),((n2:vStations{myId:'%s'}))\n" +
                    "CALL apoc.algo.dijkstra(n1,n2,\"vNEAR>\",\"weight\")yield path as path,weight as weight\n" +
                    "with path as p\n" +
                    "return p,length(p)", id1, id2);
            Result result = session.run(cypher);
            List<StationPath> stationPaths = Neo4jUtil.getStationPathFromResult(result);
            //将每一条路线封装为一个JSONObject
            for (StationPath stationPath : stationPaths) {
                JSONObject object = new JSONObject();
                object.put("stations", new ArrayList<>(stationPath.getStationList()));
                object.put("length", stationPath.getLength());
                ansObjectList.add(object);
            }
        } catch (Exception e) {
            System.out.println("没有找到Record, id1:" + id1 + "->" + "id2:" + id2);
            return null;
        }
        return ansObjectList;
    }


    @Override
    public List<JSONObject> findShortestPathByName(String name1, String name2) {
        List<JSONObject> ansObjectList = new ArrayList<>();
        List<StationPath> stationPaths = findAllShortestPathByName(name1, name2);
        if (stationPaths == null || stationPaths.size() == 0) {
            return null;
        }
        //使用comparable接口将newList排序，将其中的len属性从小到大排序
        Comparator<StationPath> compareLen = Comparator.comparing(StationPath::getLength);
        Collections.sort(stationPaths, compareLen);
        int minLength = -1;//存储最短的路线长度，只取最短的长度
        //将每一条路线封装为一个JSONObject
        for (StationPath stationPath : stationPaths) {
            if (minLength == -1) {
                minLength = stationPath.getLength();
            } else if (minLength != stationPath.getLength()) {
                break;
            }
            JSONObject object = new JSONObject();
            object.put("stations", new ArrayList<>(stationPath.getStationList()));
            object.put("length", stationPath.getLength());
            ansObjectList.add(object);
        }
        return ansObjectList;
    }

    @Override
    public List<StationPath> findAllShortestPathByName(String name1, String name2) {
        List<StationPath> ansStationPaths;
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations)-[]->(m:vNames{name:'%s'})),((n2:vStations)-[]->(k:vNames{name:'%s'}))\n" +
                    "CALL apoc.algo.dijkstra(n1,n2,\"vNEAR>\",\"weight\")yield path as path,weight as weight\n" +
                    "with path as p\n" +
                    "return p,length(p)", name1, name2);
            Result result = session.run(cypher);
            ansStationPaths = Neo4jUtil.getStationPathFromResult(result);
        } catch (Exception e) {
            System.out.println("没有找到Record, name1:" + name1 + "->" + "name2:" + name2);
            return null;
        }
        return ansStationPaths;
    }

    @Override
    public List<JSONObject> findMinTimePathByName_REDUCE(String name1, String name2) {
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        List<StationPath> stationPaths = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations)-[]->(m:vNames{name:'%s'})),((n2:vStations)-[]->(k:vNames{name:'%s'})),\n" +
                    "p=((n1)-[r:vNEAR*..10]->(n2))\n" +
                    "with p,REDUCE(x=0,r in relationships(p)|x+r.time) as sumtime\n" +
                    "order by sumtime\n" +
                    "return p,sumtime", name1, name2);
            Result result = session.run(cypher);
            stationPaths = Neo4jUtil.getStationPathFromResult(result);
            if (stationPaths == null || stationPaths.size() == 0) {
                return null;
            }
            int minTime = -1;//存储最短时间的路线，存储最短时间
            for (StationPath stationPath : stationPaths) {
                if (minTime == -1) {
                    minTime = stationPath.getTime();
                } else if (minTime != stationPath.getTime()) {
                    break;
                }
                //将每一条路线封装为一个JSONObject
                JSONObject object = new JSONObject();
                object.put("routename", new ArrayList<>(stationPath.getStationRelationships()));
                object.put("stations", new ArrayList<>(stationPath.getStationList()));
                object.put("time", stationPath.getTime());
                object.put("length", stationPath.getLength());
                ansJsonObjects.add(object);
            }
            return ansJsonObjects;

        } catch (Exception e) {
            System.out.println("没有找到Record, name1:" + name1 + "->" + "name2:" + name2);
            return null;
        }
    }

    @Override
    public List<JSONObject> findMinTimePathByName_APOC(String name1, String name2) {
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        List<StationPath> stationPaths = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations)-[]->(m:vNames{name:'%s'})),((n2:vStations)-[]->(k:vNames{name:'%s'}))\n" +
                    "CALL apoc.algo.dijkstra(n1,n2,\"vNEAR>\",\"time\")yield path as path,weight as weight\n" +
                    "with path as p, weight as sumtime\n" +
                    "order by sumtime\n" +
                    "return p,sumtime\n", name1, name2);
            Result result = session.run(cypher);
            stationPaths = Neo4jUtil.getStationPathFromResult(result);
//            System.out.println(stationPaths);
            if (stationPaths == null || stationPaths.size() == 0) {
                return null;
            }
            int minTime = -1;//存储最短时间的路线，存储最短时间
            for (StationPath stationPath : stationPaths) {
                if (minTime == -1) {
                    minTime = stationPath.getTime();
                } else if (minTime != stationPath.getTime()) {
                    break;
                }
                //将每一条路线封装为一个JSONObject
                JSONObject object = new JSONObject();
                object.put("routename", new ArrayList<>(stationPath.getStationRelationships()));
                object.put("stations", new ArrayList<>(stationPath.getStationList()));
                object.put("time", stationPath.getTime());
                object.put("length", stationPath.getLength());
                ansJsonObjects.add(object);
            }
            return ansJsonObjects;

        } catch (Exception e) {
            System.out.println("没有找到Record, name1:" + name1 + "->" + "name2:" + name2);
            return null;
        }
    }

    @Override
    public List<JSONObject> findMinTimePathById_APOC(String id1, String id2) {
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        List<StationPath> stationPaths = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations{myId:'%s'})),((n2:vStations{myId:'%s'}))\n" +
                    "CALL apoc.algo.dijkstra(n1,n2,\"vNEAR>\",\"time\")yield path as path,weight as weight\n" +
                    "with path as p ,weight as weight\n" +
                    "order by weight\n" +
                    "return p,weight\n", id1, id2);
            Result result = session.run(cypher);
            stationPaths = Neo4jUtil.getStationPathFromResult(result);
//            System.out.println(stationPaths);
            if (stationPaths == null || stationPaths.size() == 0) {
                return null;
            }
            int minTime = -1;//存储最短时间的路线，存储最短时间
            for (StationPath stationPath : stationPaths) {
                if (minTime == -1) {
                    minTime = stationPath.getTime();
                } else if (minTime != stationPath.getTime()) {
                    break;
                }
                //将每一条路线封装为一个JSONObject
                JSONObject object = new JSONObject();
                object.put("routename", new ArrayList<>(stationPath.getStationRelationships()));
                object.put("stations", new ArrayList<>(stationPath.getStationList()));
                object.put("time", stationPath.getTime());
                object.put("length", stationPath.getLength());
                ansJsonObjects.add(object);
            }
            return ansJsonObjects;

        } catch (Exception e) {
            System.out.println("没有找到Record, id1:" + id1 + "->" + "id2:" + id2);
            return null;
        }
    }

    @Override
    public List<JSONObject> findMinTimePathByName_ALL(String name1, String name2) {
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        List<StationPath> stationPaths = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations)-[]->(m:vNames{name:'%s'})),((n2:vStations)-[]->(k:vNames{name:'%s'}))\n" +
                    "CALL apoc.algo.allSimplePaths(n1,n2,\"vNEAR>\",20)yield path\n" +
                    "with reduce(x=0,r in relationships(path)|x+r.time) as sumtime,path as p\n" +
                    "order by sumtime\n" +
                    "return p,sumtime", name1, name2);
            Result result = session.run(cypher);
            stationPaths = Neo4jUtil.getStationPathFromResult(result);
            if (stationPaths == null || stationPaths.size() == 0) {
                return null;
            }
            //因为cyper返回的路线已经根据time从小到大排序，因此直接取list前面时间最少的路线
            int minTime = -1;//存储最短时间的路线，存储最短时间
            for (StationPath stationPath : stationPaths) {
                if (minTime == -1) {
                    minTime = stationPath.getTime();
                } else if (minTime != stationPath.getTime()) {
                    break;
                }
                //将每一条路线封装为一个JSONObject
                JSONObject object = new JSONObject();
                object.put("routename", new ArrayList<>(stationPath.getStationRelationships()));
                object.put("stations", new ArrayList<>(stationPath.getStationList()));
                object.put("time", stationPath.getTime());
                object.put("length", stationPath.getLength());
                ansJsonObjects.add(object);
            }
            return ansJsonObjects;

        } catch (Exception e) {
            System.out.println("没有找到Record, name1:" + name1 + "->" + "name2:" + name2);
            return null;
        }
    }

    @Override
    public List<JSONObject> findMinTransferPathByName(String name1, String name2) {
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        List<StationPath> stationPaths = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations)-[]->(m:vNames{name:'%s'})),((n2:vStations)-[]->(k:vNames{name:'%s'}))\n" +
                    "CALL apoc.algo.allSimplePaths(n1,n2,\"vNEAR>\",10)yield path\n" +
                    "with path as p\n" +
                    "return p", name1, name2);
            Result result = session.run(cypher);
            stationPaths = Neo4jUtil.getStationPathFromResult(result);
            if (stationPaths == null || stationPaths.size() == 0) {
                return null;
            }
            //根据路径中的transferCnt排序
            Comparator<StationPath> compareLen = Comparator.comparing(StationPath::getTransferCnt);
            Collections.sort(stationPaths, compareLen);
            int minTransferCnt = -1;//存储最短的换乘次数
            //将每一条路线封装为一个JSONObject
            for (StationPath stationPath : stationPaths) {
                if (minTransferCnt == -1) {
                    minTransferCnt = stationPath.getTransferCnt();
                } else if (minTransferCnt != stationPath.getTransferCnt()) {
                    break;
                }
                //将每一条路线封装为一个JSONObject
                JSONObject object = new JSONObject();
                object.put("routename", new ArrayList<>(stationPath.getStationRelationships()));
                object.put("stations", new ArrayList<>(stationPath.getStationList()));
                object.put("transferCount", stationPath.getTransferCnt());
                object.put("length", stationPath.getLength());
                ansJsonObjects.add(object);
            }
            return ansJsonObjects;

        } catch (Exception e) {
            System.out.println("没有找到Record, name1:" + name1 + "->" + "name2:" + name2);
            return null;
        }
    }

    //返回所有线路
    @Override
    public List<JSONObject> findAllLines(){
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("MATCH (n:vLines) with n.name as name RETURN name");
            Result result = session.run(cypher);
            List<Record> records = result.list();
            for (Record record : records) {
                Map<String, Object> map = record.asMap();
                JSONObject resline = new JSONObject();
                for (String cur : map.keySet()) {
                    if (cur.equals("name")) {
                        resline.put("name", map.get(cur));
                    }
                }
                ansJsonObjects.add(resline);
            }
            return ansJsonObjects;
        }catch (Exception e) {
            System.out.println("没有找到线路");
            return null;
        }
    }
}

