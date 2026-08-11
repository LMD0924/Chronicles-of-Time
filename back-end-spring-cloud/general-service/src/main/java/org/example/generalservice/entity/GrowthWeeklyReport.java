package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("growth_weekly_report")
public class GrowthWeeklyReport {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String reportJson;
    private String reflection;
    private String nextWeekFocus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
