package org.example.generalservice.dto.growth;

import lombok.Data;

import java.time.LocalDate; /**
 * 查询条件 DTO
 */
@Data
public class GrowthQueryDTO {
    private Long userId;
    private String stage;
    private String semester;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isMilestone;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
