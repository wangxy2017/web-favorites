package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findAllByUserIdAndTaskDateBetween( Integer userId,Date startDate, Date endDate);

    Page<Task> findAllByUserIdAndTaskDate(Integer userId, Date taskDate, Pageable pageable);

    List<Task> findByAlarmTime(Date alarmTime);

    void deleteByUserIdAndTaskDate(Integer userId,Date taskDate);

    List<Task> findByTaskDateAndLevelIn(Date taskDate, List<Integer> levels);

    @Query(value = "select task_date taskDate,count(id) count,level from t_task where user_id = :id and task_date between :startDate and :endDate group by level,task_date order by task_date,level",nativeQuery = true)
    List<Map<String,Object>> taskCountByDayBetween(Integer id,Date startDate,Date endDate);

    int countByUserId(Integer userId);

    List<Task> findByUserIdAndLevelIn(Integer userId, List<Integer> levels);

    void deleteAllByUserId(Integer userId);
}
