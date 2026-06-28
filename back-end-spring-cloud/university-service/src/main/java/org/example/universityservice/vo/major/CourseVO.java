package org.example.universityservice.vo.major;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseVO {
    private Long id;
    private Long majorId;
    private Long categoryId;
    private String courseCode;
    private String name;
    private BigDecimal credit;
    private Integer totalHours;
    private Integer theoryHours;
    private Integer labHours;
    private String courseType;
    private Integer term;
    private String examType;
    private String description;
    private String prerequisite;

    // 学生选课相关字段
    private String studentStatus;
    private BigDecimal studentScore;
    private BigDecimal studentGradePoint;
    private Integer isPassed;
    private Integer isRetake;
}