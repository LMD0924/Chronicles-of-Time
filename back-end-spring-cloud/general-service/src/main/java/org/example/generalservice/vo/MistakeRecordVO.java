package org.example.generalservice.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 错题展示VO
 */
@Data
@Builder
public class MistakeRecordVO {
    private Integer id;
    private String subjectName;
    private String mistakeName;
    private String mistakeType;
    private String questionOptions;     // 选项
    private String studentChoice;       // 学生选项
    private String wrongAnswer;         // 错误答案
    private String correctAnswer;       // 正确答案
    private String answerAnalysis;      // 解析
    private String knowledgePoint;      // 知识点
    private LocalDate mistakeDate;
    private Boolean mastered;
    private Integer reviewCount;
}