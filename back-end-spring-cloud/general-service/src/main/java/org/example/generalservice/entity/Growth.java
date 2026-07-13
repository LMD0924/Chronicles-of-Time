/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.entity;

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
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("growth_record")
public class Growth {

    @TableId(type = IdType.ASSIGN_ID)
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

    @TableField("company_name")
    private String companyName;

    @TableField("job_title")
    private String jobTitle;

    @TableField("job_content")
    private String jobContent;

    @TableField("work_skills")
    private String workSkills;

    @TableField("work_achievements")
    private String workAchievements;

    @TableField("work_challenges")
    private String workChallenges;

    @TableField("career_plan")
    private String careerPlan;

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

    @TableField(fill = FieldFill.INSERT,value="created_at")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE,value="updated_at")
    private LocalDateTime updateTime;
}
