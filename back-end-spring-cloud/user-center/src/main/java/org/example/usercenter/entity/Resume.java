package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @TableId(type = IdType.ASSIGN_ID)  // 使用雪花算法生成ID
    private Long id; //简历id
    private Long userId; // 用户id
    private String name; // 姓名
    private String gender; // 性别
    private String birthDate; // 生日
    private String phone; // 电话
    private String email; // 邮箱
    private String address; // 地址
    private String avatar; // 头像
    private String jobTitle; //求职岗位
    private String jobStatus; // 求职状态
    private Integer expectedSalary; // 期望薪资
    private Integer workYears; // 工作年限
    private String selfEvaluation; // 自我评价
    private Integer isPublic; // 是否公开 0:不公开 1:公开
    private Long viewCount; // 浏览量
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
