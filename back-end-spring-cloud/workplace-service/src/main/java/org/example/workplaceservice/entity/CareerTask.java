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
@TableName("career_task")
public class CareerTask {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long goalId;
    private String taskName;
    private String taskType;
    private String status;
    private String priority;
    private String quadrant;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer estimatedMinutes;
    private Integer actualMinutes;
    private String outcome;
    private String notes;
    private String tags;
    private Integer reminderEnabled;
    private LocalDateTime reminderAt;
    private String repeatRule;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
