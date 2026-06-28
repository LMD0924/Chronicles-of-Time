package org.example.universityservice.vo.major;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GraduationGapVO {
    private GraduationProgressVO progress;
    private Integer compulsoryCreditsShort;
    private Integer electiveCreditsShort;
    private Integer totalCreditsShort;
    private List<MissingCourseVO> missingCompulsory = new ArrayList<>();
    private List<MissingCourseVO> missingElective = new ArrayList<>();
    private List<PrerequisiteGapVO> prerequisiteGaps = new ArrayList<>();
}
