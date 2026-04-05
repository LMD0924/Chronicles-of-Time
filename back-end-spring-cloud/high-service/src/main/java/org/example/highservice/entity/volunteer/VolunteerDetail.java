package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 志愿详情表实体
 */
@Data
@TableName("volunteer_detail")
public class VolunteerDetail {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer volunteerId;

    private Integer priority;

    private Integer universityId;

    private Integer majorId;

    private Boolean isMajorAdjusted;

    private Boolean matchingCheck;

    private Integer matchingScore;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}