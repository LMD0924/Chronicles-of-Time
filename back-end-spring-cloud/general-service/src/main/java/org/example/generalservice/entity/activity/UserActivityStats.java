package org.example.generalservice.entity.activity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_activity_stats")
public class UserActivityStats {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Integer totalLoginDays;

    private Integer continuousLoginDays;

    private Integer maxContinuousLoginDays;

    private Long totalOnlineSeconds;

    private Long todayOnlineSeconds;

    private LocalDate lastCheckinDate;

    private LocalDate onlineDate;

    private LocalDateTime lastSeenAt;

    private Integer medalScore;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
