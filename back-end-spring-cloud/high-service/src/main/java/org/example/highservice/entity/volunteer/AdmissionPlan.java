package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("admission_plan")
public class AdmissionPlan {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("university_id")
    private Integer universityId;

    @TableField("university_name")
    private String universityName;

    @TableField("major_id")
    private Integer majorId;

    private Integer year;
    private String province;

    @TableField("student_type")
    private String studentType;

    @TableField("planned_num")
    private Integer plannedNum;

    @TableField("actual_num")
    private Integer actualNum;

    @TableField("min_score")
    private Integer minScore;

    @TableField("min_rank")
    private Integer minRank;

    @TableField("avg_score")
    private BigDecimal avgScore;

    @TableField("max_score")
    private Integer maxScore;
}