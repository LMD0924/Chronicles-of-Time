package org.example.generalservice.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String conversationType;

    private Long groupId;

    private Long senderId;

    private Long receiverId;

    private String contentType;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime recalledAt;
}
