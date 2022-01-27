package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_password",indexes = {@Index(columnList = "favorites_id")})
@org.hibernate.annotations.Table(appliesTo = "t_password",comment="密码表")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Password {

    @Id
    @Column(name = "id", columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "account", columnDefinition = "varchar(100) comment '账号'")
    private String account;

    @Column(name = "password", columnDefinition = "varchar(100) comment '密码'")
    private String password;

    @Column(name = "favorites_id", unique = true, columnDefinition = "int(10) comment '收藏ID'")
    private Integer favoritesId;

    @Column(name = "user_id", columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

}
