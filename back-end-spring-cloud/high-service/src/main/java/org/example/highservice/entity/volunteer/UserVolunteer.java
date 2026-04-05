package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_volunteer")
public class UserVolunteer {
    @TableId(type = IdType.AUTO)
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