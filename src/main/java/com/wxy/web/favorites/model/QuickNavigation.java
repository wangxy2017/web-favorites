package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_quick_navigation", indexes = {@Index(columnList = "user_id")})
@org.hibernate.annotations.Table(appliesTo = "t_quick_navigation",comment="快捷导航")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class QuickNavigation {

    @Id
    @Column(name = "id", columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "name", columnDefinition = "varchar(500) comment '名称'")
    private String name;

    @Column(name = "icon", columnDefinition = "varchar(500) comment '图标'")
    private String icon;

    @Column(name = "url", columnDefinition = "varchar(1000) comment '地址'")
    private String url;

    @Column(name = "user_id", columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

}