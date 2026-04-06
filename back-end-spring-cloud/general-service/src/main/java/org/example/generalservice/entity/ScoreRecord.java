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
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("score_records")
public class ScoreRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;                 // 主键ID
    private Integer userId;             // 用户ID
    private String subjectName;         // 科目名称
    private BigDecimal score;           // 分数
    private LocalDate examDate;         // 考试日期
    private LocalDateTime createdAt;    // 创建时间
}