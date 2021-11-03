package com.ecnu.bussystem.service;

import com.ecnu.bussystem.entity.Station;
import com.ecnu.bussystem.entity.StationTimetable;
import com.ecnu.bussystem.entity.Timetable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TimetableServiceImpl implements TimetableService{
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    StationServiceImpl stationService;

    @Override
    public StationTimetable findTimetableByIdAndTime(String time, String stationId, String lineName, String count) {
        // 检查输入是否合法
        String pattern1 = "[0-9]{2}:[0-9]{2}";
        String pattern2 = "[0-9]*";
        if (time == null || stationId == null || count == null || !time.matches(pattern1) || !count.matches(pattern2)) {
            return null;
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("stationID").is(stationId));
        query.addCriteria(Criteria.where("routeName").in(lineName, lineName + "上行", lineName + "下行", lineName + "路上行", lineName + "路下行"));
        query.addCriteria(Criteria.where("passTime").gte(time));
        query.with(Sort.by(Sort.Direction.ASC,"passTime"));
        query.limit(Integer.parseInt(count));

        List<Timetable> find = mongoTemplate.find(query, Timetable.class, "timetable");
        if (find == null || find.size() == 0) {
            return null;
        }

        try {
            SimpleDateFormat formatter=new SimpleDateFormat("HH:mm");
            Date date=formatter.parse(time);

            // Timetable里需要增加一个字段minutes，显示几分钟后到站
            for (Timetable timetable : find) {
                String passTime = timetable.getPassTime();
                Date tmp =formatter.parse(passTime);
                int minutes = (int) (tmp.getTime() - date.getTime()) / 60 / 1000;
                timetable.setMinutes(minutes);
            }
        } catch (Exception e) {
            return null;
        }

        StationTimetable stationTimetable = new StationTimetable(find.get(0).getStationName(), find);

        return stationTimetable;
    }

    @Override
    public List<StationTimetable> findTimetableByNameAndTime(String time, String stationName, String lineName, String count) {
        List<StationTimetable> stationTimetables = new ArrayList<>();

        // 先在neo4j中查找所有符合name的Station
        List<Station> stationList= stationService.findStationByVagueName(stationName);

        // 找到每个Station的Timetable
        for (Station station : stationList) {
            StationTimetable stationTimetable = findTimetableByIdAndTime(time, station.getMyId(), lineName, count);
            if (stationTimetable != null && stationTimetable.isValid()) {
                stationTimetables.add(stationTimetable);
            }
        }
        return stationTimetables;
    }

    @Override
    public StationTimetable findTimetableByIdAndTimeRange(String time, int range, String stationId) {
        // 检查输入是否合法
        String pattern1 = "[0-9]{2}:[0-9]{2}";
        if (time == null || stationId == null || !time.matches(pattern1)) {
            return null;
        }

        SimpleDateFormat formatter=new SimpleDateFormat("HH:mm");
        StationTimetable stationTimetable;
        try {
            Date date=formatter.parse(time);
            String time1 = formatter.format(new Date(date.getTime() + (long)range * 60 * 1000));

            Query query = new Query();
            query.addCriteria(Criteria.where("stationID").is(stationId));
            query.addCriteria(Criteria.where("passTime").gte(time).lte(time1));
            query.with(Sort.by(Sort.Direction.ASC,"passTime"));

            List<Timetable> find = mongoTemplate.find(query, Timetable.class, "timetable");
            if (find == null || find.size() == 0) {
                return null;
            }

            // Timetable里需要增加一个字段minutes，显示几分钟后到站
            for (Timetable timetable : find) {
                String passTime = timetable.getPassTime();
                Date tmp =formatter.parse(passTime);
                int minutes = (int) (tmp.getTime() - date.getTime()) / 60 / 1000;
                timetable.setMinutes(minutes);
            }

            stationTimetable = new StationTimetable(find.get(0).getStationName(), find);
        } catch (Exception e) {
            return null;
        }

        return stationTimetable;
    }

    @Override
    public List<StationTimetable> findTimetableByNameAndTimeRange(String time, int range, String stationName) {
        List<StationTimetable> stationTimetables = new ArrayList<>();

        // 先在neo4j中查找所有符合name的Station
        List<Station> stationList= stationService.findStationByVagueName(stationName);

        // 找到每个Station的Timetable
        for (Station station : stationList) {
            StationTimetable stationTimetable = findTimetableByIdAndTimeRange(time, range, station.getMyId());
            if (stationTimetable != null && stationTimetable.isValid()) {
                stationTimetables.add(stationTimetable);
            }
        }
        return stationTimetables;
    }
}
