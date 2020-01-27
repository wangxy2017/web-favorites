package com.wxy.web.favorites.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_category")
@org.hibernate.annotations.Table(appliesTo = "t_category", comment = "分类表")
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue
    private Integer id;

    @Column(name = "name", nullable = false, columnDefinition = "varchar(100) comment '名称'")
    private String name;

    @Column(name = "user_id", nullable = false, columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;
}
