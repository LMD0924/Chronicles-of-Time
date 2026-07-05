package org.example.generalservice.vo.activity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ActivitySummaryVO {

    private Long userId;

    private Integer totalLoginDays;

    private Integer continuousLoginDays;

    private Integer maxContinuousLoginDays;

    private Long totalOnlineSeconds;

    private Long todayOnlineSeconds;

    private LocalDate lastCheckinDate;

    private LocalDateTime lastSeenAt;

    private Integer medalScore;

    private Boolean checkedInToday;

    private List<UserMedalVO> medals = new ArrayList<>();

    private List<UserMedalVO> newlyAwarded = new ArrayList<>();
}
