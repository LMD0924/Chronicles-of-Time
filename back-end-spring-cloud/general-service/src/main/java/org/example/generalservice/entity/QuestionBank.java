package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 题库实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("question_bank")
public class QuestionBank {

    @TableId(type = IdType.AUTO)
    private Integer id;                 // 主键ID
    private String subjectName;         // 科目名称
    private String questionType;        // 题目类型：单选、多选、解答、填空、判断
    private String categoryLevel;       // 分类层级：高中、大学、考公、考研、考证
    private String knowledgePoint;      // 知识点
    private String questionTitle;       // 题目标题/题干
    private String options;             // 选项（JSON格式）
    private String correctAnswer;       // 正确答案
    private String answerAnalysis;      // 答案解析
    private String difficultyLevel;     // 难度等级：简单、中等、困难
    private Integer scoreValue;         // 题目分值
    private Integer useCount;           // 被使用/练习次数
    private Integer mistakeCount;       // 被做错次数
    private BigDecimal mistakeRate;     // 错误率
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}