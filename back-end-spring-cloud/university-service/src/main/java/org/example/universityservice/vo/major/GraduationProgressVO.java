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
public class GraduationProgressVO {
    private String majorName;
    private String majorCode;

    // 要求学分
    private Integer totalCreditsRequired;
    private Integer compulsoryCreditsRequired;
    private Integer electiveCreditsRequired;

    // 已获学分
    private Integer totalCreditsEarned;
    private Integer compulsoryCreditsEarned;
    private Integer electiveCreditsEarned;

    // 绩点和进度
    private BigDecimal gpa;
    private Integer progressPercent;
    private String status;
}