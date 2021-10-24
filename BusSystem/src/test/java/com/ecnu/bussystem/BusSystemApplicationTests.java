package com.ecnu.bussystem;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationLine;
import com.ecnu.bussystem.service.BusInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class BusSystemApplicationTests {
    @Autowired
    BusInfoServiceImpl busInfoService;

    @Test
    void testFindStationById() {
        Station station = busInfoService.findStationById("5425");

        System.out.println(station.getName() + " " + station.getEnglishname() + " " + station.getType());
    }

    @Test
    void testFindRouteByPerciseName() {
        StationLine stationLine = busInfoService.findRouteByPerciseName("102路");

        System.out.println("line: " + stationLine.getName());
        List<Station> stations = stationLine.getStations();

        // 注意空指针（有一些station的begins和ends是空）
        for (Station cur : stations) {
            System.out.println((cur.getName()) + " " + cur.getIdlist().size());
        }
    }

    @Test
    void testFindRouteByVagueName() {
        List<StationLine> stationLines = busInfoService.findRouteByVagueName("30路");
        System.out.println("size: " + stationLines.size());

        for (StationLine cur : stationLines) {
            System.out.println((cur.getName()) + ":");
            List<Station> stations = cur.getStations();
            for (Station station : stations) {
                System.out.println(station.getName());
            }
        }
    }

    @Test
    void testFindRelatedRoutesByStationName() {
        List<StationLine> stationLines = busInfoService.findRelatedRoutesByStationName("灵龙大道北");
        System.out.println("size: " + stationLines.size());

        for (StationLine cur : stationLines) {
            System.out.println((cur.getName()) + ":");
            List<Station> stations = cur.getStations();
            for (Station station : stations) {
                System.out.println(station.getName());
            }
        }
    }

    //判断两个站点之间是否存在直达路线并显示“路线名称-路径”
    @Test
    void testFindTwoStationDirectPathByName() {
        String station1name = "燎原";
        String station2name = "北门立交西";
        List<StationLine> stationLines = busInfoService.findTwoStationDirectPathByName(station1name, station2name);
        if (stationLines.size() == 0) {
            System.out.println("不存在直达线路");
            return;
        }
        for (int i = 0; i < stationLines.size(); i++) {
            System.out.print(stationLines.get(i).getName() + ":");
            List<Station> directPath = stationLines.get(i).getStations();
            for (int j = 0; j < directPath.size(); j++) {
                if (j != directPath.size() - 1)
                    System.out.printf(directPath.get(j).getName() + "->");
                else System.out.println(directPath.get(j).getName());
            }
        }
    }

    //判断两个站点之间是否存在直达路线，并输出直达线路的名称
    @Test
    void testFindTwoStationDirectRoutenameByName() {
        String name1 = "燎原";
        String name2 = "北门立交西";
        List<String> routenameList = busInfoService.findTwoStationDirectRoutenameByName(name1, name2);
        if (routenameList.size() <= 0) {
            System.out.println("不存在线路");
        }
        for (int i = 0; i < routenameList.size(); i++) {
            System.out.println(routenameList.get(i));
        }
    }

    //判断指定一条路线上两个站点之间的路线，（直达线路的路径）
    @Test
    void testFindTwoStationOnThisPathDirectPathByName() {
        String name1 = "何家巷";
        String name2 = "燎原";
        String routename = "102路";
        List<StationLine> directPath = busInfoService.findTwoStationOnThisPathDirectPathByName(routename, name1, name2);
        if(directPath.size()==0){
            System.out.println("线路不存在");
        }
        for (int i = 0; i < directPath.size(); i++) {
            String thisRouteName = directPath.get(i).getName();
            System.out.printf(thisRouteName + ":");
            List<Station> stationList=directPath.get(i).getStations();
            for(int j=0;j<stationList.size();j++){
                if(j!=stationList.size()-1)
                    System.out.printf(stationList.get(j).getName()+"->");
                else System.out.println(stationList.get(j).getName());
            }
        }

    }
}
