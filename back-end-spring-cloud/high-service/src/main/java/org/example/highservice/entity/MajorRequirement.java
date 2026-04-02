/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 大学专业选科要求实体类
 */
package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.Year;

@Data
@TableName("major_requirement")
public class MajorRequirement {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 专业代码
     */
    private String majorCode;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 专业类别（如：工学/理学/医学）
     */
    private String category;

    /**
     * 首选科目要求：物理/历史/不限
     */
    private String firstSubjectRequired;

    /**
     * 再选科目要求：如：化学(必选) 或 化学/生物(二选一)
     */
    private String secondSubjectRequired;

    /**
     * 详细选科要求说明
     */
    private String requirementDetail;

    /**
     * 大学名称
     */
    private String universityName;

    /**
     * 大学层次：985/211/双一流/普通
     */
    private String universityLevel;

    /**
     * 匹配度均值（仅在 match 查询里返回）
     */
    @TableField(exist = false)
    private Double avgMatchingScore;

    /**
     * 匹配科目列表（仅在 match 查询里返回）
     */
    @TableField(exist = false)
    private String matchedSubjects;

    /**
     * 所在省份
     */
    private String province;

    /**
     * 招生年份
     */
    private Year admissionYear;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}