package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * @author wangxiaoyuan
 * 2021/5/7 13:24
 **/
@Data
@Entity
@Table(name = "t_verification", indexes = {@Index(columnList = "account")})
@org.hibernate.annotations.Table(appliesTo = "t_verification",comment="验证码表")
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
@DynamicUpdate
@DynamicInsert
public class Verification {

    @Id
    @Column(name = "id", columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "account", columnDefinition = "varchar(100) comment '邮箱'")
    private String account;

    @Column(name = "code", columnDefinition = "varchar(100) comment '验证码'")
    private String code;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "expired_time", columnDefinition = "datetime comment '过期时间'")
    private Date expiredTime;

    @Column(name = "action", columnDefinition = "int(1) comment '动作：0-注册 1-邮箱登录 2-修改邮箱'")
    private Integer action;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "send_time", columnDefinition = "datetime comment '发送时间'")
    private Date sendTime;
}
