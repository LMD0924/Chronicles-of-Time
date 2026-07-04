/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 成绩记录实体类
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("score_record")
public class ScoreRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID
    private Long userId;                // 用户ID
    private Long subjectId;             // 科目ID
    private String subjectName;         // 科目名称
    private String examName;            // 考试名称
    private String examType;            // 考试类型
    private BigDecimal score;           // 分数
    private BigDecimal fullScore;       // 满分
    private Integer classRank;          // 班级排名
    private Integer gradeRank;          // 年级排名
    private String notes;               // 备注
    private LocalDate examDate;         // 考试日期
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}
