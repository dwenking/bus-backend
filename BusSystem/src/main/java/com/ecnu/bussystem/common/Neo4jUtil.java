package com.ecnu.bussystem.common;

import com.alibaba.fastjson.JSONObject;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Neo4jUtil {
    /**
     * 确保是return n的情况
     * @param result 结果
     * @return {@code List<String>}
     */
    public static List<String> getJsonStringFromNodeResult(Result result) {
        List<Record> records = result.list();
        List<String> mapStrings = new ArrayList<>();

        for (Record record : records) {
            Value value = record.get("n");
            Map<String, Object> map = value.asNode().asMap();

            String mapString = JSONObject.toJSONString(map);
            if (mapString != null && !mapString.equals("")) {
                mapStrings.add(mapString);
            }
        }
        return mapStrings;
    }

    /**
     * 确保是return p的情况
     *
     * @param result 结果
     * @return {@code List<String>}
     */
    public static List<String> getJsonStringFromPathResult(Result result) {
        List<Record> records = result.list();
        List<String> mapStrings = new ArrayList<>();
        Record record = null;

        if (records != null) {
            record = records.get(0);
        }
        else {
            return mapStrings;
        }

        // 因为是return p
        Value value = record.get("p");
        Path path = value.asPath();

        // 得到node结果后，类型转换并加入line的station list
        for (Node node : path.nodes()) {
            Map<String, Object> map = node.asMap();
            String mapString = JSONObject.toJSONString(map);
            if (mapString != null && !mapString.equals("")) {
                mapStrings.add(mapString);
            }
        }
        return mapStrings;
    }
}
