package com.ecnu.bussystem.controller;


import com.ecnu.bussystem.common.JSONResult;
import com.ecnu.bussystem.entity.Line;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.service.LineService;
import com.ecnu.bussystem.service.LineServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Line")
public class LineController {
    @Autowired
    private LineServiceImpl lineService;
    // 根据线路精确名称查找线路（指明上行和下行）
    @GetMapping(path = "/GET/precisename")
    public JSONResult<?> findRouteByPerciseName(
            @RequestParam(name = "name") String routeName
    ) {
        Line line= lineService.findRouteByPerciseName(routeName);
        return JSONResult.success(line);
    }

    //根据线路模糊名称查找线路（不指明上行还是下行）
    @GetMapping(path = "/GET/vaguename")
    public JSONResult<?> findRouteByVagueName(
            @RequestParam(name = "name") String routeName
    ) {
        List<Line> lines = lineService.findRouteByVagueName(routeName);
        return JSONResult.success(lines);
    }



}
