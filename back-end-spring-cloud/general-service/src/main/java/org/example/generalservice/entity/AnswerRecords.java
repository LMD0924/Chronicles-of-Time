/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

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
    private Long id;

    private Long userId;

    private Long sessionId;

    private Long questionId;

    private Long subjectId;

    private Long knowledgePointId;

    private String subjectName;

    private String questionType;

    private String categoryLevel;

    private String knowledgePoint;

    private String userAnswer;

    private String correctAnswer;

    @TableField("is_correct")
    private Integer isCorrect;  // 0-错误，1-正确

    private Integer score;

    private Integer answerTimeSeconds;

    @TableField("mistake_added")
    private Integer mistakeAdded;

    private String examSession;

    private LocalDateTime answerAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
