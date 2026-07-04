/**
 * 文件说明：在线练习、考试和错题练习业务接口。
 */
package org.example.generalservice.service.question;

import org.example.generalservice.dto.question.ExamStartRequest;
import org.example.generalservice.dto.question.ExamSubmitRequest;
import org.example.generalservice.vo.question.ExamDetailVO;
import org.example.generalservice.vo.question.ExamHistoryVO;
import org.example.generalservice.vo.question.ExamStartVO;
import org.example.generalservice.vo.question.ExamSubmitVO;

import java.util.List;

public interface OnlineExamService {
    ExamStartVO startExam(ExamStartRequest request);

    ExamSubmitVO submitExam(ExamSubmitRequest request);

    List<ExamHistoryVO> getHistory(Long userId, String mode);

    ExamDetailVO getDetail(Long userId, Long sessionId);
}
