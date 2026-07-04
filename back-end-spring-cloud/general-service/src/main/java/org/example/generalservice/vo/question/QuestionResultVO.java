/**
 * 文件说明：在线练习和考试单题结果。
 */
package org.example.generalservice.vo.question;

import lombok.Data;

@Data
public class QuestionResultVO {
    private Long questionId;
    private String questionTitle;
    private String questionType;
    private String categoryLevel;
    private String subjectName;
    private String knowledgePoint;
    private String difficultyLevel;
    private String options;
    private String userAnswer;
    private String correctAnswer;
    private String answerAnalysis;
    private Boolean correct;
    private Integer score;
    private Integer answerTimeSeconds;
}
