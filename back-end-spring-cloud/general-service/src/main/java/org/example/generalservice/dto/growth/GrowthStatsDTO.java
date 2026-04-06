package org.example.generalservice.dto.growth;

import lombok.Data;

import java.math.BigDecimal;
/**
 * 统计数据 DTO
 */
@Data
public class GrowthStatsDTO {
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
}
