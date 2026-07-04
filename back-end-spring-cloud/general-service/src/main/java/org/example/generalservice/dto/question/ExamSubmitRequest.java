/**
 * 文件说明：在线练习和考试提交请求参数。
 */
package org.example.generalservice.dto.question;

import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitRequest {
    private Long userId;
    private Long sessionId;
    private Integer durationSeconds;
    private Integer suspiciousCount;
    private List<ExamSubmitAnswer> answers;
}
