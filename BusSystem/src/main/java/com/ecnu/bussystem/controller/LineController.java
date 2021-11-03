package com.ecnu.bussystem.controller;


import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.service.LineService;
import com.ecnu.bussystem.service.LineServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/line")
public class LineController {
    @Autowired
    private LineServiceImpl lineService;

    // 根据线路精确名称查找线路（指明上行和下行）
    @GetMapping(path = "/perciselinename/{name}")
    public JSONResult<?> findLineByPerciseName(
            @PathVariable String name
    ) {
        Line line= lineService.findLineByPerciseName(name);
        if (line == null) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到线路数据,name:" + name);
        }
        return JSONResult.success(line);
    }

    //根据线路模糊名称查找线路（不指明上行还是下行）
    @GetMapping(path = "/vaguelinename/{name}")
    public JSONResult<?> findLineByVagueName(
            @PathVariable String name
    ) {
        List<Line> lines = lineService.findLineByVagueName(name);
        if (lines == null || lines.size() == 0) {
            return JSONResult.error(JSONResult.NO_DATA_ERROR,"未找到线路数据,name:" + name);
        }
        return JSONResult.success(lines);
    }
}
