/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 题库实体类
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("question")
public class QuestionBank {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID
    private Long createdBy;             // 创建用户ID
    private String subjectName;         // 科目名称
    private String questionType;        // 题目类型：单选、多选、解答、填空、判断
    private String categoryLevel;       // 分类层级：高中、大学、考公、考研、考证
    private String knowledgePoint;      // 知识点，多个知识点用逗号分隔
    private String questionTitle;       // 题目标题/题干
    private String options;             // 选项（JSON格式）
    private String correctAnswer;       // 正确答案
    private String answerAnalysis;      // 答案解析
    private String difficultyLevel;     // 难度等级：简单、中等、困难
    private Integer scoreValue;         // 题目分值
    private Integer useCount;           // 被使用/练习次数
    private Integer mistakeCount;       // 被做错次数
    private BigDecimal mistakeRate;     // 错误率
    private Integer status;             // 数据状态：1正常，0停用
    private String auditStatus;         // 审核状态：pending、approved、rejected
    private String auditRemark;         // 审核意见
    private Long auditedBy;             // 审核人ID
    private LocalDateTime auditedAt;    // 审核时间
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}
