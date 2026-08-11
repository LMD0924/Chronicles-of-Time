package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class SystemNotification {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String noticeType;
    private String title;
    private String content;
    private String bizType;
    private Long bizId;
    private String dedupeKey;
    private String actionPath;
    private String dueAt;
    private Integer readStatus;
    private LocalDateTime readAt;
    private LocalDateTime dismissedAt;
    private LocalDateTime createdAt;
}
