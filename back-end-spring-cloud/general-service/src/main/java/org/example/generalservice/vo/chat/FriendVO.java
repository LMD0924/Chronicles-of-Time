package org.example.generalservice.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendVO {

    private Long id;

    private Long userId;

    private Long friendId;

    private String username;

    private String name;

    private String avatar;

    private String status;

    private Integer unreadCount;

    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private Boolean lastMessageRead;
}
