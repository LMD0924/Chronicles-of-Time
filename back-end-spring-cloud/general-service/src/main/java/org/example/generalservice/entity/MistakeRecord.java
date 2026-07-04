/**
 * 文件说明：拾光记微服务后端通用内容服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.generalservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 错题记录实体类
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("mistake_record")
public class MistakeRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;                    // 主键ID
    private Long userId;                // 用户ID
    private Long questionId;            // 题目ID
    private Long subjectId;             // 科目ID
    private Long knowledgePointId;      // 知识点ID
    private Long lastAnswerRecordId;    // 最近一次答题记录ID
    private String subjectName;         // 错题科目名
    private String mistakeName;         // 错题名称/题目简述
    private String mistakeType;         // 错题类型：单选、多选、解答、填空
    private String questionOptions;     // 题目选项（JSON格式）
    private String studentChoice;       // 学生选择的选项（选择题用）
    private String wrongAnswer;         // 学生的错误答案
    private String correctAnswer;       // 正确答案
    private String answerAnalysis;      // 答案解析
    private String knowledgePoint;      // 知识点标签
    private String mistakeReason;       // 错因
    private String correctionNotes;     // 订正笔记
    private Integer mistakeCount;       // 错误次数
    private LocalDate mistakeDate;      // 错题记录日期
    private Boolean mastered;           // 是否已掌握
    private Integer reviewCount;        // 复习次数
    private LocalDate lastReviewDate;   // 最后复习日期
    private LocalDate nextReviewDate;   // 下次复习日期
    private LocalDateTime lastMistakeAt;// 最近错误时间
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}
