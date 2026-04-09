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
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_info")
public class UserInfo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String university; // 学校
    private String major; // 专业
    private String choose; // 意向

}
