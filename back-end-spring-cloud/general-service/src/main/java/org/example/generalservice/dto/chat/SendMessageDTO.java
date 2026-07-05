package org.example.generalservice.dto.chat;

import lombok.Data;

@Data
public class SendMessageDTO {

    private String conversationType;

    private Long groupId;

    private Long receiverId;

    private String contentType;

    private String content;
}
