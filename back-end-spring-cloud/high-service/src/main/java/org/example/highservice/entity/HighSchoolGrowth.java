package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 高中成长记录实体类
 * &#064;Author:总会落叶
 * &#064;Date:2026/3/31
 */
@Data
@TableName("high_school_growth")
public class HighSchoolGrowth {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String grade;

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

    private BigDecimal sleepHours;

    private Integer exerciseMinutes;

    private BigDecimal screenTimeHours;

    private Integer familyCommunicationQuality;

    private String familySupport;

    @TableField("is_milestone")
    private Boolean isMilestone;

    private String milestoneName;

    private String achievementThisPeriod;

    private String improvementNeeded;

    private String nextGoal;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}