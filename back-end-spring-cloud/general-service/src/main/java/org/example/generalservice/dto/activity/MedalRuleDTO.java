package org.example.generalservice.dto.activity;

import lombok.Data;

@Data
public class MedalRuleDTO {

    private Long id;

    private String code;

    private String name;

    private String description;

    private String medalType;

    private Integer thresholdValue;

    private String icon;

    private String color;

    private Boolean enabled;
}
