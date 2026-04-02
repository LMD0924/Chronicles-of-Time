package org.example.highservice.dto;

import lombok.Data;

import java.math.BigDecimal;
/**
 * 统计数据 DTO
 */
@Data
public class HighSchoolGrowthStatsDTO {
    private Integer totalRecords;
    private Integer milestoneCount;
    private BigDecimal avgStudyHours;
    private BigDecimal avgStressLevel;
    private BigDecimal avgHappinessLevel;
    private BigDecimal avgSleepHours;
    private BigDecimal avgExerciseMinutes;
    private String bestSubject;
    private String careerInterest;
    private String dreamCollege;
    private String dreamMajor;

    // 各年级统计
    private Integer grade10Count;  // 高一
    private Integer grade11Count;  // 高二
    private Integer grade12Count;  // 高三
}
