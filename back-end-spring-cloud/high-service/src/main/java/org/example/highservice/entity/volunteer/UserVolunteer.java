/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("user_volunteer_plan")
public class UserVolunteer {
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;

    @TableField("user_id")
    private Long userId;

    private Integer year;
    private String name;
    private String province;
    private Integer score;
    @TableField("`rank`")
    private Integer rank;

    @TableField("student_type")
    private String studentType;

    @TableField("preference_order")
    private Integer preferenceOrder;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("is_final")
    private Boolean isFinal;

    @TableField("selected_subjects")
    private String selectedSubjects;
}