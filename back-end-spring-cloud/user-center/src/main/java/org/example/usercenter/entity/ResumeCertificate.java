package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 证书实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeCertificate {

    /**
     * 主键ID
     */
    @TableId(type= IdType.ASSIGN_ID)
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 证书名称
     */
    private String certificateName;

    /**
     * 颁发机构
     */
    private String issueAuthority;

    /**
     * 获得日期
     */
    private LocalDate issueDate;

    /**
     * 分数/等级
     */
    private String score;

    /**
     * 描述
     */
    private String description;
}