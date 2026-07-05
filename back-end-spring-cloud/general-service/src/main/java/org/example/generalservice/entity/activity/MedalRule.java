package org.example.generalservice.entity.activity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medal_rule")
public class MedalRule {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String code;

    private String name;

    private String description;

    private String medalType;

    private Integer thresholdValue;

    private String icon;

    private String color;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
