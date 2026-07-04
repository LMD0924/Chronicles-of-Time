/**
 * 文件说明：在线练习和考试历史详情。
 */
package org.example.generalservice.vo.question;

import lombok.Data;

import java.util.List;

@Data
public class ExamDetailVO {
    private ExamHistoryVO session;
    private List<QuestionResultVO> details;
}
