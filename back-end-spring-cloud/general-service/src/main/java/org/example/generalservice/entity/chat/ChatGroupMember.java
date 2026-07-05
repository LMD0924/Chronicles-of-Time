package org.example.generalservice.entity.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_group_member")
public class ChatGroupMember {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long groupId;

    private Long userId;

    private String role;

    private String status;

    private LocalDateTime joinedAt;

    private LocalDateTime lastReadAt;
}
