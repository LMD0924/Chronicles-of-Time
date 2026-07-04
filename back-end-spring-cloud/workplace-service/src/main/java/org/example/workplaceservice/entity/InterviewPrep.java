/**
 * 文件说明：拾光记微服务后端业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.workplaceservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("interview_prep")
public class InterviewPrep {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String companyName;
    private String positionName;
    private String interviewRound;
    private LocalDate interviewDate;
    private String status;
    private String questionBank;
    private String preparationNotes;
    private String feedback;
    private String result;
    private Integer confidenceScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
