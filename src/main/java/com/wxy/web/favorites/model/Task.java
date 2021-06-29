package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "t_task", indexes = {@Index(columnList = "user_id")})
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Task {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "content", nullable = false, columnDefinition = "varchar(500) comment '内容'")
    private String content;

    @JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")
    @Column(name = "task_date", nullable = false, columnDefinition = "date comment '任务日期'")
    private Date taskDate;

    @Column(name = "is_alarm", columnDefinition = "int(1) comment '是否告警: 0-否 1-是'")
    private Integer isAlarm;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @Column(name = "alarm_time", columnDefinition = "datetime comment '告警时间'")
    private Date alarmTime;

    @Column(name = "user_id", nullable = false, columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @Column(name = "create_time", columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    @Column(name = "level", nullable = false, columnDefinition = "int(1) comment '级别: 0-紧急 1-优先 2-重要 3-待办 4-完成 5-取消'")
    private Integer level;
}
