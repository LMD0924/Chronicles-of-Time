package org.example.generalservice.entity.activity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_medal")
public class UserMedal {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long ruleId;

    private String code;

    private String name;

    private String description;

    private String medalType;

    private Integer sourceValue;

    private String icon;

    private String color;

    private LocalDateTime awardedAt;
}
