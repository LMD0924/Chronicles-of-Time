/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 成绩记录DTO
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
public class ScoreRecordDTO {
    private Long userId;
    private String subjectName;
    private BigDecimal score;
    private LocalDate examDate;
}