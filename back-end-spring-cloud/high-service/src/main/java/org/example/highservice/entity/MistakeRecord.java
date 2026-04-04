package org.example.highservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 错题记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("mistake_records")
public class MistakeRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;                 // 主键ID
    private Integer userId;             // 用户ID
    private String subjectName;         // 错题科目名
    private String mistakeName;         // 错题名称/题目简述
    private String mistakeType;         // 错题类型：单选、多选、解答、填空
    private String questionOptions;     // 题目选项（JSON格式）
    private String studentChoice;       // 学生选择的选项（选择题用）
    private String wrongAnswer;         // 学生的错误答案
    private String correctAnswer;       // 正确答案
    private String answerAnalysis;      // 答案解析
    private String knowledgePoint;      // 知识点标签
    private LocalDate mistakeDate;      // 错题记录日期
    private Boolean mastered;           // 是否已掌握
    private Integer reviewCount;        // 复习次数
    private LocalDate lastReviewDate;   // 最后复习日期
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}