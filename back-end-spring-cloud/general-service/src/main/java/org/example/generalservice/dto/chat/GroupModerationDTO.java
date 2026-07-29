package org.example.generalservice.dto.chat;

import lombok.Data;

@Data
public class GroupModerationDTO {

    private Long userId;
    private Long messageId;
    private Integer muteMinutes;
    private Boolean enabled;
    private String role;
}