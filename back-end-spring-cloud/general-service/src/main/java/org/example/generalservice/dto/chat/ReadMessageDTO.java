package org.example.generalservice.dto.chat;

import lombok.Data;

import java.util.List;

@Data
public class ReadMessageDTO {

    private String conversationType;

    private Long groupId;

    private Long friendId;

    private List<Long> messageIds;
}
