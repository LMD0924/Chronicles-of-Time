package org.example.universityservice.vo.major;

import lombok.Data;

@Data
public class PrerequisiteGapVO {
    private Long courseId;
    private String courseName;
    private String prerequisite;
    private String missingPrerequisiteCourse;
}
