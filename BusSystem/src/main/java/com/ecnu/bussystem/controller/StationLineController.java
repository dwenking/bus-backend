package com.ecnu.bussystem.controller;

import com.ecnu.bussystem.common.JSONResult;
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
@RequestMapping("/StationLine")
public class StationLineController {
    @Autowired
    private LineServiceImpl lineService;

    //根据路线的名称返回路线上的站
    @GetMapping(path = "/GET/preciseName")
    public JSONResult<?> findStationlineByPreciseRouteName(
            @RequestParam(name = "name") String routeName
    ) {
        StationLine lines = lineService.findStationlineByPreciseRouteName(routeName);
        return JSONResult.success(lines);
    }

    //根据路线的名称模糊搜索返回路线上的站
    @GetMapping(path = "/GET/vagueName")
    public JSONResult<?>findStationlineByVagueRouteName(
            @RequestParam(name = "name") String routeName
    ) {
        List<StationLine> lines = lineService.findStationlineByVagueRouteName(routeName);
        return JSONResult.success(lines);
    }

}
