package org.example.universityservice.vo.major;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SemesterScheduleVO {
    private Integer term;
    private String termLabel;
    private Integer totalCredits;
    private List<ScheduleCourseVO> courses = new ArrayList<>();
}
