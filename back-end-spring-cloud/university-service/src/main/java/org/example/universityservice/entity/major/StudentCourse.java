/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("uni_student_course")
public class StudentCourse {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "专业ID不能为空")
    private Long majorId;

    @NotNull(message = "修读学期不能为空")
    private String semester;

    private BigDecimal score;

    private BigDecimal gradePoint;

    private Integer isPassed;

    private Integer isRetake;

    private Integer retakeCount;

    private String status;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}