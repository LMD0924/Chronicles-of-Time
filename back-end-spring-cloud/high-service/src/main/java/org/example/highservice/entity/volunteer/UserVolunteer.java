package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_volunteer_plan")
public class UserVolunteer {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("admission_year")
    private Integer year;

    @TableField("plan_name")
    private String name;

    private String province;
    private Integer score;

    @TableField("`rank_no`")
    @JsonProperty("rank")
    private Integer rankNo;

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
