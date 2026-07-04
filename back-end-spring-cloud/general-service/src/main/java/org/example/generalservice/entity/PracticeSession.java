/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责在线练习和考试会话数据。
 */
package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 练习或考试会话实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("practice_session")
public class PracticeSession {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String sessionType;         // practice、exam、mistake
    private String title;
    private String categoryLevel;
    private String subjectName;
    private String knowledgePoints;
    private String difficultyLevel;
    private String questionIds;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer scoreTotal;
    private Integer scoreObtained;
    private Integer durationSeconds;
    private Integer antiCheatEnabled;
    private Integer suspiciousCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer status;             // 1运行中，2已完成，3已放弃
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
