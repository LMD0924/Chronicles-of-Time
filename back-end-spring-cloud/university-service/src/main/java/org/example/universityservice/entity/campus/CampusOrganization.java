package org.example.universityservice.entity.campus;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("uni_campus_organization")
public class CampusOrganization {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String organizationType;
    private String organizationName;
    private String department;
    private String roleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String description;
    private String achievements;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
