package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notification_preference")
public class NotificationPreference {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private Integer enabled;
    private Integer quietHoursEnabled;
    private String quietStart;
    private String quietEnd;
    private String preferredStage;
    private Integer weekStartsMonday;
    private Integer defaultReminderMinutes;
    private String defaultStartTime;
    private String defaultEndTime;
    private Integer browserNotificationsEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
