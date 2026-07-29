package org.example.generalservice.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupMemberVO {

    private Long userId;
    private String username;
    private String name;
    private String avatar;
    private String role;
    private Integer level;
    private String levelName;
    private Integer levelProgress;
    private LocalDateTime mutedUntil;
}