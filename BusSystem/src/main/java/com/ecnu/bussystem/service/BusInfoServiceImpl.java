package com.ecnu.bussystem.service;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.respository.StationRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BusInfoServiceImpl implements BusInfoService{
    @Autowired
    StationRespository stationRespository;

    @Override
    public Station findStationById(String Id) {
        return stationRespository.findStationById(Id);
    }

    @Override
    public StationLine findRouteByPerciseName(String routeName) {
        StationLine stationLine = new StationLine();
        stationLine.setName(routeName);
        stationLine.setStations(stationRespository.findRouteByPerciseName(routeName));
        return stationLine;
    }

    @Override
    public List<StationLine> findRouteByName(String routeVogueName) {
        StationLine stationLine = new StationLine();
        List<Station> tmp;
        List<StationLine> stationLines = new ArrayList<>();

        tmp = stationRespository.findRouteByPerciseName(routeVogueName + "上行");
        if (tmp.size() > 0) {
            stationLine.setName(routeVogueName + "上行");
            stationLine.setStations(tmp);
            stationLines.add(stationLine);
        }

        tmp = stationRespository.findRouteByPerciseName(routeVogueName + "下行");
        if (tmp.size() > 0) {
            stationLine.setName(routeVogueName + "下行");
            stationLine.setStations(tmp);
            stationLines.add(stationLine);
        }

        tmp = stationRespository.findRouteByPerciseName(routeVogueName);
        if (tmp.size() > 0) {
            stationLine.setName(routeVogueName);
            stationLine.setStations(tmp);
            stationLines.add(stationLine);
        }

        return stationLines;
    }
}
