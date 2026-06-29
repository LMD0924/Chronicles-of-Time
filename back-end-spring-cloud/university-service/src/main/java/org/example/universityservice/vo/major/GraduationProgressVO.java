package org.example.universityservice.vo.major;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GraduationProgressVO {
    private String majorName;
    private String majorCode;

    // 要求学分
    private Integer totalCreditsRequired;
    private Integer compulsoryCreditsRequired;
    private Integer electiveCreditsRequired;

    // 已获学分
    private Integer totalCreditsEarned;
    private Integer compulsoryCreditsEarned;
    private Integer electiveCreditsEarned;

    // 绩点和进度
    private BigDecimal gpa;
    private Integer progressPercent;
    private String status;
}