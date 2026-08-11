package org.example.generalservice.dto.notification;

import lombok.Data;

@Data
public class NotificationSyncItem {
    private String noticeType;
    private String title;
    private String content;
    private String bizType;
    private Long bizId;
    private String dedupeKey;
    private String actionPath;
    private String dueAt;
}
