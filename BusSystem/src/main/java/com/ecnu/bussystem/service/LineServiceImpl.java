package com.ecnu.bussystem.service;

import com.alibaba.fastjson.JSONObject;
import com.ecnu.bussystem.common.Neo4jUtil;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.respository.LineRepository;
import com.ecnu.bussystem.respository.StationRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
        List<Line> lines = new ArrayList<>();

        if (line1 != null) {
            lines.add(line1);
        }
        if (line2 != null) {
            lines.add(line2);
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

    /////////////////
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
        StationLine lineStation1 = this.findStationOfLineByPreciseName(lineName1);
        StationLine lineStation2 = this.findStationOfLineByPreciseName(lineName2);
        if (lineStation1 == null || lineStation2 == null) {
            JSONObject resLine = new JSONObject();
            resLine.put("stationName", null);
            resLine.put("stationID", null);
            resLine.put("english", null);
            res.add(resLine);
            return res;
        }
        //修改为：重复站点名由ID区分
        Set<String> stationID1 = new HashSet<>(lineStation1.returnAllStationMyId());
        Set<String> stationID2 = new HashSet<>(lineStation2.returnAllStationMyId());
        Collection<String> dup_ID = CollectionUtils.intersection(stationID1, stationID2);
        Station tmp;
        for (String id : dup_ID) {
            JSONObject resLine = new JSONObject();
            tmp = stationService.findStationById(id);
            resLine.put("stationName", tmp.getName());
            resLine.put("stationID", tmp.getMyId());
            resLine.put("english", tmp.getEnglishname());
            res.add(resLine);
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

    //(具体的直达线路）返回两个站之间是否存在直达线路，返回从name1到name2的线路（！注意：如果是name1到name2则线路的directional设为true，如果是相反，则为false）
    @Override
    public List<StationLine> findDirectPathBetweenTwoStations(String name1, String name2) {
        //存储直达线路的答案
        List<StationLine> DirectPathList = new ArrayList<>();
        //分别找出两个站name的站的集合，并已找到相关的线路，并返回在station中的lines数组中
        List<Station> stationList1 = stationService.findLineOfStationByVagueName(name1);
        List<Station> stationList2 = stationService.findLineOfStationByVagueName(name2);
        if (stationList1 == null || stationList1.size() == 0 || stationList2 == null || stationList2.size() == 0) {
            return null;
        }
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
                    //找出该线路下两个站的下标位置
                    Integer indx1 = -1;
                    Integer indx2 = -1;
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
                    StationLine thisDirectpath = null;
                    if (indx1 < indx2) {
                        thisDirectpath = new StationLine(stationLine.getName(), true, new ArrayList<>(stationList.subList(indx1, indx2 + 1)), 0);
                    } else {
                        thisDirectpath = new StationLine(stationLine.getName(), false, new ArrayList<>(stationList.subList(indx2, indx1 + 1)), 0);
                    }
                    DirectPathList.add(thisDirectpath);
                }
            }
        }
        return DirectPathList;
    }

    @Override
    public List<JSONObject> findDirectPathNameBetweenTwoStations(String name1, String name2) {
        List<StationLine> stationLineList = this.findDirectPathBetweenTwoStations(name1, name2);
        List<JSONObject> objects = new ArrayList<>();
        if (stationLineList == null || stationLineList.size() == 0) {
            return null;
        }
        Set<String> stationtruelist = new HashSet<>();
        Set<String> stationfalselist = new HashSet<>();
        for (StationLine stationLine : stationLineList) {
            //只有从name1->name2的线路名称需要
            if (stationLine.getDirectional()) {
                stationtruelist.add(stationLine.getName());
            } else {
                stationfalselist.add(stationLine.getName());
            }
        }
        //对所有符合要求的线路名称进行去重，封装为JSONObject
        for (String name : stationtruelist) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("directional", name1 + "->" + name2);
            objects.add(jsonObject);
        }
        for (String name : stationfalselist) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("directional", name2 + "->" + name1);
            objects.add(jsonObject);
        }
        return objects;
    }

    @Override
    public List<JSONObject> findOneWayStationsByRouteName(String name) {
        //存储这条线路上的单行站的名称
        List<JSONObject> objectList=new ArrayList<>();
        List<Line> linelist = this.findLineByVagueName(name);
        if (linelist == null || linelist.size() <= 1) {
            return null;
        }
        Set<String> nameSet1=new HashSet<>();
        Set<String> nameSet2=new HashSet<>();
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
            Set<String> stationNameSet=new HashSet<>();
            for (int j = 0; j < stations.size(); j++) {
                stationNameSet.add(stations.get(j).getName());
            }
            if(i==0){
                nameSet1.addAll(stationNameSet);
            }
            else {
                nameSet2.addAll(stationNameSet);
            }
        }
        Collection<String> allStationName = CollectionUtils.union(nameSet1, nameSet2);
        Collection<String> intersectionStationName = CollectionUtils.intersection(nameSet1, nameSet2);
        List<String> thisRouteOneWayStations = (List<String>) CollectionUtils.subtract(allStationName, intersectionStationName);
        if(thisRouteOneWayStations==null||thisRouteOneWayStations.size()==0){
            return null;
        }
        for(int i=0;i<thisRouteOneWayStations.size();i++){
            JSONObject objects = new JSONObject();
            objects.put("name",thisRouteOneWayStations.get(i));
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
        try (Session session = neo4jDriver.session()) {
            // 先找出该routeName对应线路的所有途径站点的id，放在stationID中
            String cypher = String.format("match (n:vStations)-[]->(l:vLines{name:'%s'}) return n", routeName);
            Result result = session.run(cypher);
            List<Record> records = result.list();
            List<String> stationID = new ArrayList<>();
            for (Record record : records) {
                Value value = record.get("n");
                Map<String, Object> map = value.asNode().asMap();
                stationID.add(map.get("myId").toString());
            }
            // 遍历所有站点ID，找出和每个ID相连的线路即为可换乘线路，记得去掉一开始查询的路线routeName
            for (String stationid : stationID) {
                List<String> transferLineList = new ArrayList<>();
                String cypher2 = String.format("match (n:vStations{myId:'%s'})-[]->(l:vLines) " +
                        "with l.name as transferLine return distinct transferLine", stationid);
                Result result2 = session.run(cypher2);
                List<Record> records2 = result2.list();
                for (Record record : records2) {
                    Map<String, Object> map = record.asMap();
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
        JSONObject resLine2 = new JSONObject();
        resLine2.put("TotalTransLineNumber", totalTransferLine.size());
        res.add(resLine2);
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
            System.out.println(type);
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
        int cnt = stations.size();
        //用nums存储每两个站点之间的非重复系数，根据id查找站点间的线路，并且区分方向
        List<Double> nums = new ArrayList<>();
        for (int i = 0; i < cnt - 1; i++) {
            String id1 = stations.get(i).getMyId();
            String id2 = stations.get(i + 1).getMyId();
            int routes = this.findDirectPathWithDirection(id1, id2);
//            System.out.println(routes);
            nums.add(1.0 / routes);
        }
        //用reduce函数求非重复系数和
        Double average = nums.stream().reduce(Double::sum).orElse(0.0);
        //求平均非重复系数并保留两位小数
        average = average / (cnt - 1);
        BigDecimal b = new BigDecimal(average);
        double ave = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        res.put("lineName", routeName);
        res.put("number", ave);
        return res;
    }


    public int findDirectPathWithDirection(String id1, String id2) {
        int cnt = 0;
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
                    "return n",line.getName(),line.getDirectional(),line.getKilometer(),line.getLineNumber(),
                                line.getOnewayTime(),line.getRoute(),line.getRuntime(),line.getType(), line.getInterval());
            Result result = session.run(cypher);
        }
        res.put("line",line);

        return res;
    }


}
