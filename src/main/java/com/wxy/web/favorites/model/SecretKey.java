package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "t_secret_key")
@org.hibernate.annotations.Table(appliesTo = "t_secret_key", comment = "密钥表")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = { "hibernateLazyInitializer"})
public class SecretKey {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue
    private Integer id;
    @Column(name = "username", unique = true, nullable = false, columnDefinition = "varchar(100) comment '用户名'")
    private String username;
    @Column(name = "random_key", nullable = false, columnDefinition = "varchar(16) comment '16位随机字符'")
    private String randomKey;
}
