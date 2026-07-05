package org.example.generalservice.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_group")
public class ChatGroup {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String groupNo;

    private String name;

    private String announcement;

    private Long ownerId;

    private Integer memberCount;

    private Boolean searchable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
