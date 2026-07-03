/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.vo.major;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
public class MissingCourseVO {
    private Long courseId;
    private String courseCode;
    private String name;
    private BigDecimal credit;
    private String courseType;
    private Integer term;
    private String reason;
}
