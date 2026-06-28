package org.example.universityservice.vo.major;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScheduleCourseVO {
    private Long courseId;
    private String courseCode;
    private String name;
    private BigDecimal credit;
    private String courseType;
    private String examType;
    private String studentStatus;
    private BigDecimal score;
    private BigDecimal gradePoint;
    private Integer isPassed;
}
