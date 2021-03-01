package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("select t from Task t where t.userId = :userId and to_char(t.taskDate,'yyyy-MM-dd') between :startDate and :endDate")
    List<Task> findAllByUserId(String startDate, String endDate, Integer userId);

    @Query("select t from Task t where to_char(t.alarmTime,'yyyy-MM-dd HH24:MI:SS') = :alarmTime")
    List<Task> findByAlarmTime(String alarmTime);

    @Query("select t from Task t where to_char(t.taskDate,'yyyy-MM-dd') = :taskDate and t.level in (0,1,2,3)")
    List<Task> findUndoTask(String taskDate);
}
