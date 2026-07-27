package org.example.generalservice.vo.activity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrowthTaskVO {

    private String key;

    private String title;

    private String description;

    private String icon;

    private Integer current;

    private Integer target;

    private Integer rewardExperience;

    private Boolean completed;

    private String actionPath;
}
