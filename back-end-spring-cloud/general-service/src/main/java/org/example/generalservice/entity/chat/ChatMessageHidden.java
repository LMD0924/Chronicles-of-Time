package org.example.generalservice.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message_hidden")
public class ChatMessageHidden {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long messageId;
    private Long userId;
    private LocalDateTime hiddenAt;
}
