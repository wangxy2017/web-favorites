package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_user")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class User {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue
    private Integer id;

    @Column(name = "username", unique = true, nullable = false, columnDefinition = "varchar(100) comment '用户名'")
    private String username;

    @Column(name = "password", nullable = false, columnDefinition = "varchar(100) comment '密码'")
    private String password;

    @Column(name = "email", unique = true, nullable = false, columnDefinition = "varchar(100) comment '邮箱'")
    private String email;

    @Column(name = "view_style", columnDefinition = "int(1) comment '模式：0-常规模式 1-书签模式'")
    private Integer viewStyle;

    @Column(name = "capacity", columnDefinition = "bigint(20) comment '容量'")
    private Long capacity;

    @Column(name = "used_size", columnDefinition = "bigint(20) comment '已使用大小'")
    private Long usedSize;

    @Transient
    private String code;
}
