/**
 * 文件说明：拾光记微服务后端用户中心数据实体源码，负责数据实体相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 证书实体类
 */
/**
 * 类说明：当前类是数据实体模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeCertificate {

    /**
     * 主键ID
     */
    @TableId(type= IdType.ASSIGN_ID)
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 证书名称
     */
    private String certificateName;

    /**
     * 颁发机构
     */
    private String issueAuthority;

    /**
     * 获得日期
     */
    private LocalDate issueDate;

    /**
     * 分数/等级
     */
    private String score;

    /**
     * 描述
     */
    private String description;
}