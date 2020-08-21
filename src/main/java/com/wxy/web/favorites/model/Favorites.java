package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_favorites")
@org.hibernate.annotations.Table(appliesTo = "t_favorites", comment = "收藏表")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = { "hibernateLazyInitializer"})
public class Favorites {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue
    private Integer id;

    @Column(name = "name", nullable = false, columnDefinition = "varchar(100) comment '名称'")
    private String name;

    @Column(name = "icon", columnDefinition = "varchar(500) comment '图标'")
    private String icon;

    @Column(name = "url", nullable = false, columnDefinition = "varchar(500) comment '地址'")
    private String url;

    @Column(name = "category_id", nullable = false, columnDefinition = "int(10) comment '分类ID'")
    private Integer categoryId;

    @Column(name = "user_id", nullable = false, columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

    @Column(name = "pinyin", nullable = false, columnDefinition = "varchar(500) comment '拼音'")
    private String pinyin;

    @Column(name = "sort", columnDefinition = "int(4) comment '排序'")
    private Integer sort;

    @Column(name = "star", columnDefinition = "int(1) comment '是否标记：1-是 0-否'")
    private Integer star;

    @Transient
    private Password password;
}
