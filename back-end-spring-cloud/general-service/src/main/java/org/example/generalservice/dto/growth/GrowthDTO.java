/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.dto.growth;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/3/31
 * @Description:
 */
/**
 * 高中成长记录 DTO
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
public class GrowthDTO {

    private Long id;
    private Long userId;
    private String stage;
    private String semester;
    private LocalDate recordDate;
    private String examName;
    private String examRank;
    private BigDecimal examScore;
    private String bestSubject;
    private String weakestSubject;
    private BigDecimal studyHours;
    private String studyNotes;
    private String competitionName;
    private String competitionAward;
    private String activityName;
    private String activityRole;
    private String interestTested;
    private String interestContinued;
    private String skillLearned;
    private Integer stressLevel;
    private Integer happinessLevel;
    private String moodNotes;
    private String challengeText;
    private String helpNeeded;
    private Integer closeFriendsCount;
    private Integer newFriends;
    private String conflictExperience;
    private String leadershipExp;
    private String selfAwareness;
    private String careerInterest;
    private String dreamCollege;
    private String dreamMajor;
    private String companyName;
    private String jobTitle;
    private String jobContent;
    private String workSkills;
    private String workAchievements;
    private String workChallenges;
    private String careerPlan;
    private BigDecimal sleepHours;
    private Integer exerciseMinutes;
    private BigDecimal screenTimeHours;
    private Integer familyCommunicationQuality;
    private String familySupport;
    private Boolean isMilestone;
    private String milestoneName;
    private String achievementThisPeriod;
    private String improvementNeeded;
    private String nextGoal;
}

