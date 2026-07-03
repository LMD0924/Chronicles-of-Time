/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("uni_course")
public class Course {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long majorId;

    private Long categoryId;

    private String courseCode;

    @TableField("course_name")
    private String name;

    private BigDecimal credit;

    private Integer totalHours;

    private Integer theoryHours;

    private Integer labHours;

    private String courseType;

    @TableField("term_no")
    private Integer term;

    private String examType;

    private String description;

    @TableField("prerequisite_text")
    private String prerequisite;

    private Integer sortOrder;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}