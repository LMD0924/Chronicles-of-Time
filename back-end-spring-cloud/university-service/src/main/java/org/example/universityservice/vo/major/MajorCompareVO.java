package org.example.universityservice.vo.major;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MajorCompareVO {
    private MajorVO majorA;
    private MajorVO majorB;
    private Integer onlyInACount;
    private Integer onlyInBCount;
    private Integer sharedCount;
    private List<CourseCompareItemVO> onlyInA = new ArrayList<>();
    private List<CourseCompareItemVO> onlyInB = new ArrayList<>();
    private List<CourseCompareItemVO> shared = new ArrayList<>();

    @Data
    public static class CourseCompareItemVO {
        private String courseCode;
        private String name;
        private java.math.BigDecimal credit;
        private String courseType;
        private Integer term;
    }
}
