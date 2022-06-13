package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "t_favorites", indexes = {
        @Index(name = "idx_user_id_shortcut", columnList = "user_id"),
        @Index(name = "idx_user_id_shortcut", columnList = "shortcut"),
        @Index(columnList = "category_id")})
@org.hibernate.annotations.Table(appliesTo = "t_favorites", comment = "收藏表")
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Favorites {

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

    @Column(name = "category_id", columnDefinition = "int(10) comment '分类ID'")
    private Integer categoryId;

    @Column(name = "user_id", columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

    @Column(name = "pinyin", columnDefinition = "varchar(3000) comment '拼音'")
    private String pinyin;

    @Column(name = "pinyin_s", columnDefinition = "varchar(500) comment '拼音首字母'")
    private String pinyinS;

    @Column(name = "shortcut", columnDefinition = "varchar(100) comment '快捷指令'")
    private String shortcut;

    @Column(name = "schema_name", columnDefinition = "varchar(100) comment '快捷指令'")
    private String schemaName;

    @Column(name = "sort", columnDefinition = "int(4) default 0 comment '排序'")
    private Integer sort;

    @Column(name = "star", columnDefinition = "int(1) default 0 comment '是否标记：1-是 0-否'")
    private Integer star;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "visit_time", columnDefinition = "datetime comment '最近访问时间'")
    private Date visitTime;

    @Column(name = "delete_flag", columnDefinition = "int(1) comment '是否删除：0-否 1-是'")
    private Integer deleteFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "delete_time", columnDefinition = "datetime comment '逻辑删除时间'")
    private Date deleteTime;

    @Column(name = "is_share", columnDefinition = "int(1) default 0 comment '是否分享：0-否 1-是'")
    private Integer isShare;

    @Column(name = "support", columnDefinition = "int(10) default 0 comment '关注量'")
    private Integer support;

    @Column(name = "click_count", columnDefinition = "int(10) default 0 comment '点击次数'")
    private Integer clickCount;

    @Transient
    private Password password;

    @Transient
    private String username;

    public Favorites(Integer id, String name, String icon, String url, Integer support, String username) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.url = url;
        this.support = support;
        this.username = username;
    }
}
