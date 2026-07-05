package org.example.generalservice.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {

    private String conversationType;

    private Long targetId;

    private String title;

    private String avatar;

    private String groupNo;

    private Integer unreadCount;

    private String lastMessage;

    private LocalDateTime lastMessageAt;
}
