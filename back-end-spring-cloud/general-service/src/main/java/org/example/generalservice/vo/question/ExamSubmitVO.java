/**
 * 文件说明：在线练习和考试提交返回数据。
 */
package org.example.generalservice.vo.question;

import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitVO {
    private Long sessionId;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer unansweredCount;
    private Integer scoreTotal;
    private Integer scoreObtained;
    private Integer scorePercent;
    private Integer durationSeconds;
    private Integer suspiciousCount;
    private List<QuestionResultVO> details;
}
