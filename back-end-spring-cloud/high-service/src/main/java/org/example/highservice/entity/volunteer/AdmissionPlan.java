package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("gaokao_admission_plan")
public class AdmissionPlan {
    @TableId(type = IdType.ASSIGN_ID)
    private Integer id;

    @TableField("university_id")
    private Integer universityId;

    @TableField("university_name")
    private String universityName;

    @TableField("major_id")
    private Integer majorId;

    @TableField("admission_year")
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