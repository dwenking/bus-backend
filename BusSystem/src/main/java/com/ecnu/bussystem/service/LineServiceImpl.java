package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.Neo4jUtil;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.entity.StationPath;
import com.ecnu.bussystem.respository.LineRepository;
import com.ecnu.bussystem.respository.StationRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
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
        //首先根据线路模糊查询到每条线路沿线的站
        List<StationLine> stationLineList = new ArrayList<>();
        List<StationLine> stationLines = this.findStationOfLineByVagueName(routename);
        //没有找到相关的路线，返回null
        if (stationLines == null || stationLines.size() == 0) {
            return null;
        }
        //遍历每条线路上的站，判断是否存在name1->name2的路线，如果有则存入stationlines中
        for (int i = 0; i < stationLines.size(); i++) {
            List<Station> stationList = stationLines.get(i).getStations();
            List<Integer> indx1List = new ArrayList<>();
            List<Integer> indx2List = new ArrayList<>();
            //查找一条路线中从name1到name2的路线（防止有多条），直接记录下标计算
            for (int j = 0; j < stationList.size(); j++) {
                String thisStationName = stationList.get(j).getName();
                if (thisStationName.equals(name1) || thisStationName.equals(name1 + "(始发站)") || thisStationName.equals(name1 + "(终点站)")) {
                    indx1List.add(j);
                }
                if (thisStationName.equals(name2) || thisStationName.equals(name2 + "(始发站)") || thisStationName.equals(name2 + "(终点站)")) {
                    indx2List.add(j);
                }
            }
            //没有找到相关的站，之间continue
            if (indx1List.size() == 0 || indx2List.size() == 0) {
                continue;
            }
            //如果存在路线，则生成路线并计算时间
            for (Integer index1 : indx1List) {
                for (Integer index2 : indx2List) {
                    if (index1 < index2) {
                        String id1 = stationList.get(index1).getMyId();
                        String id2 = stationList.get(index2).getMyId();
                        Integer time = lineRepository.findTimebetweenTwoStations(id1, id2, stationLines.get(i).getName());
                        List<Station> alongStations = new ArrayList<>(stationList.subList(index1, index2 + 1));
                        stationLineList.add(new StationLine(stationLines.get(i).getName(), true, alongStations, time));
                    }
                }
            }
        }
        return stationLineList;
    }


    @Override
    public List<JSONObject> findDirectPathNameBetweenTwoStations(String name1, String name2) {
        //分别找出两个站name的站的集合，并已找到相关的线路，并返回在station中的lines数组中
        List<Station> stationList1 = stationService.findLineOfStationByVagueName(name1);
        List<Station> stationList2 = stationService.findLineOfStationByVagueName(name2);
        if (stationList1 == null || stationList1.size() == 0 || stationList2 == null || stationList2.size() == 0) {
            return null;
        }
        List<JSONObject> objects = new ArrayList<>();
        Set<String> directPathStationSet = new HashSet<>();//用来判断是否重复记录线路的集合
        for (Station station1 : stationList1) {
            for (Station station2 : stationList2) {
                List<String> routename1list = station1.getLines();
                List<String> routename2list = station2.getLines();
                if (routename1list == null || routename1list.size() == 0 || routename2list == null || routename2list.size() == 0) {
                    continue;
                }
                List<String> routenameList = new ArrayList<>(CollectionUtils.intersection(routename1list, routename2list));
                //找到两个站的线路重合的线路，并遍历这些线路，找出线路中的两个站，截取直达的线路部分（注意方向）
                for (String routename : routenameList) {
                    StationLine stationLine = this.findStationOfLineByPreciseName(routename);
                    if (stationLine == null) {
                        break;
                    }
                    List<Station> stationList = stationLine.getStations();
                    if (stationList == null || stationList.size() == 0) {
                        break;
                    }
                    if (!stationLine.getDirectional()) {

                        if (!directPathStationSet.contains(routename + name1 + "<->" + name2 + "（环线）") && !directPathStationSet.contains(routename + name2 + "<->" + name1 + "（环线）")) {
                            JSONObject thisPath = new JSONObject();
                            thisPath.put("name", routename);
                            thisPath.put("directional", name1 + "<->" + name2 + "（环线）");
                            objects.add(thisPath);
                            directPathStationSet.add(routename + name1 + "<->" + name2 + "（环线）");
                            directPathStationSet.add(routename + name2 + "<->" + name1 + "（环线）");
                        }
                        continue;
                    }
                    //找出该线路下两个站的下标位置
                    int indx1 = -1;
                    int indx2 = -1;
                    for (int i = 0; i < stationList.size(); i++) {
                        if (stationList.get(i).getMyId().equals(station1.getMyId())) {
                            indx1 = i;
                        }
                        if (stationList.get(i).getMyId().equals(station2.getMyId())) {
                            indx2 = i;
                        }
                    }
                    if (indx1 == -1 || indx2 == -1) {
                        break;
                    }

                    if (indx1 < indx2 && !directPathStationSet.contains(routename + name1 + "->" + name2)) {
                        JSONObject thisPath = new JSONObject();
                        thisPath.put("name", routename);
                        thisPath.put("directional", name1 + "->" + name2);
                        directPathStationSet.add(routename + name1 + "->" + name2);
                        objects.add(thisPath);
                    } else if (indx1 > indx2 && !directPathStationSet.contains(routename + name2 + "->" + name1)) {
                        JSONObject thisPath = new JSONObject();
                        thisPath.put("name", routename);
                        thisPath.put("directional", name2 + "->" + name1);
                        directPathStationSet.add(routename + name2 + "->" + name1);
                        objects.add(thisPath);
                    }

                }
            }
        }
        return objects;
    }

    @Override
    public List<JSONObject> findOneWayStationsByRouteName(String name) {
        //存储这条线路上的单行站的名称
        List<JSONObject> objectList = new ArrayList<>();
        List<Line> linelist = this.findLineByVagueName(name);
        if (linelist == null || linelist.size() <= 1) {
            return null;
        }
        Set<String> nameSet1 = new HashSet<>();
        Set<String> nameSet2 = new HashSet<>();
        for (int i = 0; i < linelist.size(); i++) {
            String routeName = linelist.get(i).getName();
            StationLine stationLine = this.findStationOfLineByPreciseName(routeName);
            if (stationLine == null) {
                break;
            }
            List<Station> stations = stationLine.getStations();
            if (stations == null || stations.size() == 0) {
                break;
            }
            Set<String> stationNameSet = new HashSet<>();
            for (int j = 0; j < stations.size(); j++) {
                stationNameSet.add(stations.get(j).getName());
            }
            if (i == 0) {
                nameSet1.addAll(stationNameSet);
            } else {
                nameSet2.addAll(stationNameSet);
            }
        }
        Collection<String> allStationName = CollectionUtils.union(nameSet1, nameSet2);
        Collection<String> intersectionStationName = CollectionUtils.intersection(nameSet1, nameSet2);
        List<String> thisRouteOneWayStations = (List<String>) CollectionUtils.subtract(allStationName, intersectionStationName);
        if (thisRouteOneWayStations == null || thisRouteOneWayStations.size() == 0) {
            return null;
        }
        for (int i = 0; i < thisRouteOneWayStations.size(); i++) {
            JSONObject objects = new JSONObject();
            objects.put("name", thisRouteOneWayStations.get(i));
            objectList.add(objects);
        }
        return objectList;
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
        Map<String, String> totalTransferLine = new HashMap<>();
        // 先找出该routeName对应线路的所有途径站点
        StationLine lineStation = this.findStationOfLineByPreciseName(routeName);
        List<Station> stations = lineStation.getStations();
        if (stations == null || stations.size() < 1) {
            return null;
        }
        try (Session session = neo4jDriver.session()) {
            // 遍历所有站点的ID，和每个ID相连的线路即为可换乘线路
            for (Station station : stations) {
                String stationid = station.getMyId();
                List<String> transferLineList = new ArrayList<>();
                String cypher = String.format("match (n:vStations{myId:'%s'})-[]->(l:vLines) " +
                        "with l.name as transferLine return distinct transferLine", stationid);
                Result result = session.run(cypher);
                List<Record> records = result.list();
                for (Record record : records) {
                    Map<String, Object> map = record.asMap();
                    // 要去掉routeName自身
                    if (!map.get("transferLine").equals(routeName)) {
                        transferLineList.add(map.get("transferLine").toString());
                        totalTransferLine.put(map.get("transferLine").toString(), "0");
                    }
                }
                if (transferLineList.size() != 0) {
                    JSONObject resLine = new JSONObject();
                    resLine.put("TransferStation", stationRepository.findStationById(stationid).getName());
                    resLine.put("TransferStationID", stationid);
                    resLine.put("TransferLines", transferLineList);
                    res.add(resLine);
                }
            }
        }
        JSONObject resLine = new JSONObject();
        resLine.put("TotalTransLineNumber", totalTransferLine.size());
        res.add(resLine);
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
            int routes = this.findDirectPathWithDirection(id1, id2);
            average += 1.0 / routes;
        }
        //求平均非重复系数并保留两位小数
        average = average / (cnt - 1);
        DecimalFormat df = new DecimalFormat("0.00");
        res.put("lineName", routeName);
        res.put("number", df.format(average));
        return res;
    }


    public int findDirectPathWithDirection(String id1, String id2) {
        int cnt = 0;
        // 根据id查找途径该站点的线路
        List<String> routesName1 = stationRepository.findLineByStationId(id1);
        List<String> routesName2 = stationRepository.findLineByStationId(id2);
        List<String> commonRoutes = new ArrayList<>(CollectionUtils.intersection(routesName1, routesName2));
        for (String route : commonRoutes) {
            StationLine stationLine = this.findStationOfLineByPreciseName(route);
            List<Station> stationList = stationLine.getStations();
            Integer indx1 = -1;
            Integer indx2 = -1;
            for (int i = 0; i < stationList.size(); i++) {
                if (stationList.get(i).getMyId().equals(id1)) {
                    indx1 = i;
                }
                if (stationList.get(i).getMyId().equals(id2)) {
                    indx2 = i;
                }
            }
            //从id1到id2，线路数加1
            if (indx1 < indx2) {
                cnt++;
            }
        }
        return cnt;
    }


    @Override
    public JSONObject createNewLine(Line line) {
        JSONObject res = new JSONObject();
        try (Session session = neo4jDriver.session()) {
            //根据线路基本信息创建vLine节点
            String cypher = String.format("CREATE (n:vLines \n" +
                            "{name:'%s', directional:'%s',kilometer:'%s'," +
                            "lineNumber:'%s', onewayTime:'%s', route:'%s'," +
                            "runTime:'%s', type:'%s',interval:'%s'})\n" +
                            "return n", line.getName(), line.getDirectional(), line.getKilometer(), line.getLineNumber(),
                    line.getOnewayTime(), line.getRoute(), line.getRuntime(), line.getType(), line.getInterval());
            Result result = session.run(cypher);
        }
        res.put("line", line);

        return res;
    }


    @Override
    public List<JSONObject> findShortestPathById(String id1, String id2) {
        List<JSONObject> ansObjectList = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match(n1:vStations{myId:'%s'}),(n2:vStations{myId:'%s'}),\n" +
                    "p=allShortestPaths((n1)-[:vNEAR *..10]-(n2))\n" +
                    "return p", id1, id2);
            Result result = session.run(cypher);
            List<StationPath> stationPaths = Neo4jUtil.getStationPathFromResult(result);
            //使用treeset去重
            Set<StationPath> newSet = new TreeSet<>(Comparator.comparing(StationPath::getPathLabel));
            newSet.addAll(stationPaths);
            List<StationPath> newList = new ArrayList<>(newSet);
            //将每一条路线封装为一个JSONObject
            for (StationPath stationPath : newList) {
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
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        List<StationPath> stationPaths = findAllShortestPathByName(name1, name2);
        if (stationPaths == null || stationPaths.size() == 0) {
            return null;
        }
        //使用treeset去重（将相同经过相同站点的路线去重）
        Set<StationPath> newSet = new TreeSet<>(Comparator.comparing(StationPath::getPathLabel));
        newSet.addAll(stationPaths);
        List<StationPath> newList = new ArrayList<>(newSet);
        //使用comparable接口将newList排序，将其中的len属性从小到大排序
        Comparator<StationPath> compareLen = Comparator.comparing(StationPath::getLength);
        Collections.sort(newList, compareLen);
        int minLength = -1;//存储最短的路线长度，只取最短的长度
        //将每一条路线封装为一个JSONObject
        for (StationPath stationPath : newList) {
            if (minLength == -1) {
                minLength = stationPath.getLength();
            } else if (minLength != stationPath.getLength()) {
                break;
            }
            JSONObject object = new JSONObject();
            object.put("stations", new ArrayList<>(stationPath.getStationList()));
            object.put("length", stationPath.getLength());
            ansJsonObjects.add(object);
        }
        return ansJsonObjects;
    }

    @Override
    public List<StationPath> findAllShortestPathByName(String name1, String name2) {
        List<StationPath> ansStationPaths;
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations)-[]->(m:vNames{name:'%s'})),((n2:vStations)-[]->(k:vNames{name:'%s'})),\n" +
                    "p=allShortestPaths((n1)-[:vNEAR *..10]-(n2))\n" +
                    "return p", name1, name2);
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
            System.out.println("run cyper");
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
    public List<JSONObject> findMinTimePathByName_APOC(String name1, String name2) {
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        List<StationPath> stationPaths = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations)-[]->(m:vNames{name:'%s'})),((n2:vStations)-[]->(k:vNames{name:'%s'}))\n" +
                    "CALL apoc.algo.dijkstra(n1,n2,\"vNEAR\",\"time\")yield path as path,weight as weight\n" +
                    "with path as p, weight as sumtime\n" +
                    "order by sumtime\n" +
                    "return p,sumtime\n", name1, name2);
            System.out.println("run cyper");
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
    public List<JSONObject> findMinTimePathByName_ALL(String name1, String name2) {
        List<JSONObject> ansJsonObjects = new ArrayList<>();
        List<StationPath> stationPaths = new ArrayList<>();
        try (Session session = neo4jDriver.session()) {
            String cypher = String.format("match((n1:vStations)-[]->(m:vNames{name:'%s'})),((n2:vStations)-[]->(k:vNames{name:'%s'}))\n" +
                    "CALL apoc.algo.allSimplePaths(n1,n2,\"vNEAR>\",10)yield path\n" +
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
                if (minTransferCnt ==-1) {
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
}

