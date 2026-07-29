package org.example.generalservice.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {

    private String conversationType;
    private Long targetId;
    private String title;
    private String avatar;
    private String remark;
    private Integer level;
    private String levelName;
    private String groupNo;
    private Long ownerId;
    private String ownerName;
    private String role;
    private LocalDateTime mutedUntil;
    private Boolean mutedAll;
    private Long pinnedMessageId;
    private String pinnedMessage;
    private String pinnedMessageSenderName;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}