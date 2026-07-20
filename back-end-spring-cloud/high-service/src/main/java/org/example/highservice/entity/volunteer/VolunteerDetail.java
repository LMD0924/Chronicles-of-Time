package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_volunteer_detail")
public class VolunteerDetail {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("volunteer_plan_id")
    private Long volunteerId;

    @TableField("priority_no")
    private Integer priority;

    private Long universityId;
    private Long majorId;
    private Boolean isMajorAdjusted;
    private Boolean matchingCheck;
    private Integer matchingScore;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}