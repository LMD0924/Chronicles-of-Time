package org.example.universityservice.vo.major;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MajorVO {
    private Long id;
    private String name;
    private String code;
    private Integer totalCredits;
    private Integer compulsoryCredits;
    private Integer electiveCredits;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}