/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.vo.major;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
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
