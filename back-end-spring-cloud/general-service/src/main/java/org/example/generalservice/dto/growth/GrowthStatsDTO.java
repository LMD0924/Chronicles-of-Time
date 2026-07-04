/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.dto.growth;

import lombok.Data;

import java.math.BigDecimal;
/**
 * 统计数据 DTO
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
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
