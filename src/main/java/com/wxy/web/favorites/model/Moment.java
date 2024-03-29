package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "t_moment", indexes = {@Index(columnList = "user_id")})
@org.hibernate.annotations.Table(appliesTo = "t_moment",comment="瞬间")
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
@DynamicUpdate
@DynamicInsert
public class Moment {

    @Id
    @Column(name = "id", columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "content", columnDefinition = "text comment '内容'")
    private String content;

    @Column(name = "text", columnDefinition = "text comment '纯文本内容'")
    private String text;

    @Column(name = "user_id", columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time", columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    @Column(name = "is_top", columnDefinition = "int(1) default 0 comment '置顶: 0-否 1-是'")
    private Integer isTop;
}
