package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.Index;
import java.util.Date;
import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findAllByUserIdAndTaskDateBetween( Integer userId,Date startDate, Date endDate);

    List<Task> findByAlarmTime(Date alarmTime);

    List<Task> findByTaskDateAndLevelIn(Date taskDate, List<Integer> levels);
}
