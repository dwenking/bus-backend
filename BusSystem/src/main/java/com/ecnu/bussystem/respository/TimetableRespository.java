package com.ecnu.bussystem.respository;

import com.ecnu.bussystem.entity.Timetable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimetableRespository extends MongoRepository<Timetable, String> {
}
