package org.example.universityservice.entity.campus;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("uni_campus_activity")
public class CampusActivity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long organizationId;
    private String activityType;
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String location;
    private String status;
    private BigDecimal serviceHours;
    private String responsibility;
    private String resultSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
