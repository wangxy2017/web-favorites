package com.wxy.web.favorites.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "t_user_file",indexes = {@Index(columnList = "user_id")})
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
@EntityListeners(AuditingEntityListener.class)
public class UserFile {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue
    private Integer id;

    @Column(name = "user_id", nullable = false, columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

    @Column(name = "pid", columnDefinition = "int(10) comment '父id'")
    private Integer pid;

    @CreatedDate
    @Column(name = "create_time", columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time", columnDefinition = "datetime comment '修改时间'")
    private Date updateTime;

    @Column(name = "filename", nullable = false, columnDefinition = "varchar(100) comment '文件名称'")
    private String filename;

    @Column(name = "path", columnDefinition = "varchar(500) comment '路径'")
    private String path;

    @Column(name = "is_dir", columnDefinition = "int(1) comment '置顶: 0-否 1-是'")
    private Integer isDir;

    @Column(name = "size", nullable = false, columnDefinition = "int(10) comment '大小(byte)'")
    private Integer size;

}
