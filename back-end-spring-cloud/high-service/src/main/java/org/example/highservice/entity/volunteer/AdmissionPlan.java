/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
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