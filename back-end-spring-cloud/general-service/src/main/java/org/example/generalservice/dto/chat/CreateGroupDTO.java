package org.example.generalservice.dto.chat;

import lombok.Data;

@Data
public class CreateGroupDTO {

    private String name;

    private String announcement;

    private Boolean searchable;
}
