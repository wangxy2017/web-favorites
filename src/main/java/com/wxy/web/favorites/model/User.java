package com.wxy.web.favorites.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_user")
@org.hibernate.annotations.Table(appliesTo = "t_user", comment = "用户表")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue
    private Integer id;

    @Column(name = "username", nullable = false, columnDefinition = "varchar(100) comment '用户名'")
    private String username;

    @Column(name = "password", nullable = false, columnDefinition = "varchar(100) comment '密码'")
    private String password;

    @Column(name = "email", nullable = false, columnDefinition = "varchar(100) comment '邮箱'")
    private String email;
}
