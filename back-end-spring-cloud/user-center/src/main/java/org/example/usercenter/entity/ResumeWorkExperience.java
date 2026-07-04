/**
 * 文件说明：拾光记微服务后端用户中心数据实体源码，负责数据实体相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 工作经历实体类
 */
/**
 * 类说明：当前类是数据实体模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_work_experience")
public class ResumeWorkExperience {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("company_name")
    private String companyName;

    private String position;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("is_current")
    private Boolean isCurrent;

    private String description;

    private String achievements;

    @TableField("sort_order")
    private Integer sortOrder;
}