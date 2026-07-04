/**
 * 文件说明：在线练习和考试开始请求参数。
 */
package org.example.generalservice.dto.question;

import lombok.Data;

import java.util.List;

@Data
public class ExamStartRequest {
    private Long userId;
    private String mode;                // exam、practice、mistake
    private String title;
    private String categoryLevel;
    private String subjectName;
    private String questionType;
    private List<String> knowledgePoints;
    private String difficultyLevel;
    private Integer questionCount;
    private Integer durationSeconds;
    private Boolean antiCheatEnabled;
}
