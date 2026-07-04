/**
 * 文件说明：在线练习和考试历史列表项。
 */
package org.example.generalservice.vo.question;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamHistoryVO {
    private Long sessionId;
    private String mode;
    private String title;
    private String categoryLevel;
    private String subjectName;
    private String knowledgePoints;
    private String difficultyLevel;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer scoreTotal;
    private Integer scoreObtained;
    private Integer scorePercent;
    private Integer durationSeconds;
    private Integer suspiciousCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
