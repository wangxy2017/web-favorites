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
@Table(name = "t_favorites", indexes = {
        @Index(columnList = "user_id"),
        @Index(columnList = "category_id")})
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Favorites {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue
    private Integer id;

    @Column(name = "name", nullable = false, columnDefinition = "varchar(500) comment '名称'")
    private String name;

    @Column(name = "icon", columnDefinition = "varchar(500) comment '图标'")
    private String icon;

    @Column(name = "url", nullable = false, columnDefinition = "varchar(500) comment '地址'")
    private String url;

    @Column(name = "category_id", nullable = false, columnDefinition = "int(10) comment '分类ID'")
    private Integer categoryId;

    @Column(name = "user_id", nullable = false, columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

    @Column(name = "pinyin", columnDefinition = "varchar(500) comment '拼音'")
    private String pinyin;

    @Column(name = "pinyin_s", columnDefinition = "varchar(500) comment '拼音首字母'")
    private String pinyinS;

    @Column(name = "shortcut", columnDefinition = "varchar(100) comment '快捷指令'")
    private String shortcut;

    @Column(name = "schema_name", columnDefinition = "varchar(100) comment '快捷指令'")
    private String schemaName;

    @Column(name = "sort", columnDefinition = "int(4) comment '排序'")
    private Integer sort;

    @Column(name = "star", columnDefinition = "int(1) comment '是否标记：1-是 0-否'")
    private Integer star;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "visit_time", columnDefinition = "datetime comment '最近访问时间'")
    private Date visitTime;

    @Column(name = "delete_flag", columnDefinition = "int(1) comment '是否删除：0-否 1-是'")
    private Integer deleteFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "delete_time", columnDefinition = "datetime comment '逻辑删除时间'")
    private Date deleteTime;

    @Transient
    private Password password;
}
