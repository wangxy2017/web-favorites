package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "t_user")
@org.hibernate.annotations.Table(appliesTo = "t_user", comment = "用户表")
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class User {

    @Id
    @Column(name = "id", columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "username", unique = true, columnDefinition = "varchar(100) comment '用户名'")
    private String username;

    @Column(name = "nick_name", unique = true, columnDefinition = "varchar(100) comment '昵称'")
    private String nickName;

    @Column(name = "password", columnDefinition = "varchar(100) comment '密码'")
    private String password;

    @Column(name = "email", unique = true, columnDefinition = "varchar(100) comment '邮箱'")
    private String email;

    @Column(name = "view_style", columnDefinition = "int(1) default 0 comment '模式：0-常规模式 1-书签模式'")
    private Integer viewStyle;

    @Column(name = "admin", columnDefinition = "int(1) default 0 comment '管理员：0-否 1-是'")
    private Integer admin;

    @Column(name = "capacity", columnDefinition = "bigint(20) default 0 comment '容量'")
    private Long capacity;

    @Column(name = "used_size", columnDefinition = "bigint(20) default 0 comment '已使用大小'")
    private Long usedSize;

    @Column(name = "error_count", columnDefinition = "int(10) default 0 comment '登录失败次数'")
    private Integer errorCount;

    @Transient
    private String code;

    @Transient
    private String sid;

    @Column(name = "status", columnDefinition = "int(1) default 1 comment '状态：1-正常 2-禁用'")
    private Integer status;

    @Column(name = "click_count", columnDefinition = "int(10) default 0 comment '点击次数'")
    private Integer clickCount;

    @Column(name = "search_count", columnDefinition = "int(10) default 0 comment '搜索次数'")
    private Integer searchCount;

    @Column(name = "online_hour", columnDefinition = "int(10) default 0 comment '在线时长'")
    private Integer onlineHour;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "register_time", columnDefinition = "datetime comment '注册时间'")
    private Date registerTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "last_online_time", columnDefinition = "datetime comment '上线时间'")
    private Date lastOnlineTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "feedback_time", columnDefinition = "datetime comment '反馈时间'")
    private Date feedbackTime;
}
