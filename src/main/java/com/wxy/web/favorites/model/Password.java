package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_password",indexes = {@Index(columnList = "favorites_id")})
@org.hibernate.annotations.Table(appliesTo = "t_password",comment="密码表")
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
@DynamicUpdate
@DynamicInsert
public class Password {

    @Id
    @Column(name = "id", columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
