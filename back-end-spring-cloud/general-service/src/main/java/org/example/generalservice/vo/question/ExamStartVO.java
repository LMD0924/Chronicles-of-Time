/**
 * 文件说明：在线练习和考试开始返回数据。
 */
package org.example.generalservice.vo.question;

import lombok.Data;
import org.example.generalservice.entity.QuestionBank;

import java.util.List;

@Data
public class ExamStartVO {
    private Long sessionId;
    private String mode;
    private Integer totalQuestions;
    private Integer durationSeconds;
    private Boolean antiCheatEnabled;
    private String drawStrategy;
    private List<QuestionBank> questions;
}
