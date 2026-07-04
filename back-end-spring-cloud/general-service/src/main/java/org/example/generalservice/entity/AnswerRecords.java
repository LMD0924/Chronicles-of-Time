/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 答题记录表实体类
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("answer_record")
public class AnswerRecords {

    @TableId(type = IdType.ASSIGN_ID)
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