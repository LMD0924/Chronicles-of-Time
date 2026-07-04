/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.entity.major;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("uni_course_category")
public class CourseCategory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long majorId;

    @TableField("category_name")
    private String name;

    private Long parentId;

    private Integer sortOrder;

    private String icon;

    private String color;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}