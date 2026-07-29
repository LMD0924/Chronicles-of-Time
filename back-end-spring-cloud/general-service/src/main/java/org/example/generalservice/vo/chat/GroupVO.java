package org.example.generalservice.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupVO {

    private Long id;
    private String groupNo;
    private String name;
    private String announcement;
    private Long ownerId;
    private String ownerName;
    private String ownerAvatar;
    private Integer memberCount;
    private String role;
    private LocalDateTime mutedUntil;
    private Boolean mutedAll;
    private Long pinnedMessageId;
    private String pinnedMessage;
    private String pinnedMessageSenderName;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Boolean joined;
}