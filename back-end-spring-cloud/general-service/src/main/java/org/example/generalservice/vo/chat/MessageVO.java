package org.example.generalservice.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;
    private String conversationType;
    private Long groupId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String senderRole;
    private Long receiverId;
    private String contentType;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime recalledAt;
    private Integer readCount;
    private Integer unreadCount;
    private Boolean readByMe;
    private Boolean mine;
}
