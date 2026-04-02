package org.example.highservice.dto;

import lombok.Data;

import java.time.LocalDate; /**
 * 查询条件 DTO
 */
@Data
public class HighSchoolGrowthQueryDTO {
    private Long userId;
    private String grade;
    private String semester;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isMilestone;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
