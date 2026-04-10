package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 答题记录表实体类
 */
@Data
@TableName("answer_records")
public class AnswerRecords {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long userId;

    private Integer questionId;

    private String subjectName;

    private String questionType;

    private String categoryLevel;

    private String knowledgePoint;

    private String userAnswer;

    private String correctAnswer;

    @TableField("is_correct")
    private Integer isCorrect;  // 0-错误，1-正确

    private Integer score;

    private Integer answerTime;

    @TableField("mistake_added")
    private Integer mistakeAdded;

    private String examSession;

    private Date answerDate;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;
}