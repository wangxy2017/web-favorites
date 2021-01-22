package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("select t from Task t where t.userId = :userId and t.taskDate between :startDate and :endDate")
    List<Task> findAllByUserId(Date startDate, Date endDate, Integer userId);
}
