/**
 * 文件说明：拾光记微服务后端用户中心数据实体源码，负责数据实体相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @Author:总会落叶
 * @Date:2026/4/5
 * @Description:
 */
/**
 * 类说明：当前类是数据实体模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_profile")
public class UserInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    @TableField("university_name")
    private String university; // 学校
    @TableField("major_name")
    private String major; // 专业
    private Long majorId; // 专业ID
    @TableField("career_direction")
    private String choose; // 意向

}
