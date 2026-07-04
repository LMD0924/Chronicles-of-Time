/**
 * 文件说明：在线练习和考试单题提交答案。
 */
package org.example.generalservice.dto.question;

import lombok.Data;

@Data
public class ExamSubmitAnswer {
    private Long questionId;
    private String userAnswer;
    private Integer answerTimeSeconds;
}
