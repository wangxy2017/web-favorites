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
import java.util.List;

@Data
@Entity
@Table(name = "t_user_file", indexes = {
        @Index(name = "idx_user_id_pid_filename", columnList = "user_id"),
        @Index(name = "idx_user_id_pid_filename", columnList = "pid")})
@org.hibernate.annotations.Table(appliesTo = "t_user_file",comment="文件表")
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
@DynamicUpdate
@DynamicInsert
public class UserFile {

    @Id
    @Column(name = "id", columnDefinition = "int(10) comment '主键ID(自增)'")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "user_id", columnDefinition = "int(10) comment '用户ID'")
    private Integer userId;

    @Column(name = "pid", columnDefinition = "int(10) comment '父id'")
    private Integer pid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time", columnDefinition = "datetime comment '创建时间'")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "update_time", columnDefinition = "datetime comment '修改时间'")
    private Date updateTime;

    @Column(name = "filename", columnDefinition = "varchar(100) comment '文件名称'")
    private String filename;

    @Column(name = "path", columnDefinition = "varchar(500) comment '路径'")
    private String path;

    @Column(name = "share_id", unique = true, columnDefinition = "varchar(64) comment '分享id'")
    private String shareId;

    @Column(name = "is_dir", columnDefinition = "int(1) default 0 comment '置顶: 0-否 1-是'")
    private Integer isDir;

    @Column(name = "size", columnDefinition = "bigint(20) default 0 comment '大小(byte)'")
    private Long size;

    @Transient
    private List<UserFile> children;

}
