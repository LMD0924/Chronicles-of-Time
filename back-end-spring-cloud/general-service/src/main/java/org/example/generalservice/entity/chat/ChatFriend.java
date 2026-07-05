package org.example.generalservice.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_friend")
public class ChatFriend {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long friendId;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
