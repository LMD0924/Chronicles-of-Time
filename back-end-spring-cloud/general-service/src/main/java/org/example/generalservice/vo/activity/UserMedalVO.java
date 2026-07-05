package org.example.generalservice.vo.activity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMedalVO {

    private Long id;

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
