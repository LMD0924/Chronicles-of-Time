package org.example.generalservice.vo.activity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminActivityUserVO {

    private Long userId;

    private String username;

    private String name;

    private String avatar;

    private Integer totalLoginDays;

    private Integer continuousLoginDays;

    private Long totalOnlineSeconds;

    private Long todayOnlineSeconds;

    private Integer medalCount;

    private Integer medalScore;

    private LocalDate lastCheckinDate;

    private LocalDateTime lastSeenAt;

    private Boolean online;
}
