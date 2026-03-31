package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 教育经历实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("resume_education")  // ✅ 添加表名注解
public class ResumeEducation {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 简历ID（外键）
     */
    @TableField("resume_id")  // ✅ 显式指定数据库列名
    private Long resumeId;

    /**
     * 学校名称
     */
    @TableField("school_name")  // ✅ 显式指定
    private String schoolName;

    /**
     * 学历：博士/硕士/本科/大专/高中
     */
    private String degree;

    /**
     * 专业
     */
    private String major;

    /**
     * 开始时间
     */
    @TableField("start_date")  // ✅ 显式指定
    private LocalDate startDate;

    /**
     * 结束时间
     */
    @TableField("end_date")  // ✅ 显式指定
    private LocalDate endDate;

    /**
     * 是否在读：false-否，true-是
     */
    @TableField("is_current")  // ✅ 显式指定
    private Boolean isCurrent;  // 改为 Boolean 包装类型，避免默认值问题

    /**
     * 描述/荣誉
     */
    private String description;

    /**
     * 排序
     */
    @TableField("sort_order")  // ✅ 显式指定
    private Integer sortOrder;
}