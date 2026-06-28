package org.example.universityservice.vo.major;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GpaSimulateVO {
    private BigDecimal currentGpa;
    private BigDecimal projectedGpa;
    private BigDecimal scholarshipLine;
    private Boolean meetsScholarship;
    private String scholarshipTip;
    private Integer countedCourses;
}
