package org.example.universityservice.vo.major;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MissingCourseVO {
    private Long courseId;
    private String courseCode;
    private String name;
    private BigDecimal credit;
    private String courseType;
    private Integer term;
    private String reason;
}
