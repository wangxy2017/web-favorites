package com.wxy.web.favorites.model;

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
public class Favorites {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue
    private Integer id;

    @Column(name = "name", nullable = false, columnDefinition = "varchar(100) comment '名称'")
    private String name;

    @Column(name = "url", nullable = false, columnDefinition = "varchar(1000) comment '地址'")
    private String url;

    @Column(name = "category_id", columnDefinition = "int(10) comment '分类ID'")
    private Integer categoryId;

    @Column(name = "user_id", nullable = false, columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;
}
