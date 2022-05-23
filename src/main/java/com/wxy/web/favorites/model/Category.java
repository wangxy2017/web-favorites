package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "t_category", indexes = {@Index(columnList = "user_id")})
@org.hibernate.annotations.Table(appliesTo = "t_category",comment="分类表")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Category {

    @Id
    @Column(name = "id", columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "name", columnDefinition = "varchar(100) comment '名称'")
    private String name;

    @Column(name = "user_id", columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

    @Column(name = "is_system", columnDefinition = "int(1) comment '是否系统分类'")
    private Integer isSystem;

    @Column(name = "sort", columnDefinition = "int(4) comment '排序'")
    private Integer sort;

    @Column(name = "bookmark", columnDefinition = "int(1) comment '强制书签模式'")
    private Integer bookmark;

    @Column(name = "pinyin", columnDefinition = "varchar(3000) comment '拼音'")
    private String pinyin;

    @Column(name = "pinyin_s", columnDefinition = "varchar(500) comment '拼音首字母'")
    private String pinyinS;

    @Transient
    private List<Favorites> favorites;

    @Transient
    private Integer page;
}
