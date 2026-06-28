package org.example.universityservice.vo.major;

import lombok.Data;

import java.util.List;

@Data
public class CourseTreeVO {
    private List<CategoryTreeNodeVO> categories;
    private Integer totalCourses;
    private Integer compulsoryCount;
    private Integer electiveCount;
}