package org.example.universityservice.vo.major;

import lombok.Data;

import java.util.List;

@Data
public class CategoryTreeNodeVO {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private List<CourseVO> courses;
}