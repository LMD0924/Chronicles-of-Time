/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.vo.paper;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
public class PaperVO {

    private Long id;                // 主键ID

    private Long userId;            // 用户ID

    @NotBlank(message = "论文题目不能为空")
    private String title;           // 论文题目

    private String supervisor;      // 导师姓名

    private String direction;       // 研究方向

    private String content;         // 论文内容

    private String stage;

    private String status;

    private LocalDateTime createdAt; // 创建时间

    private LocalDateTime updatedAt; // 更新时间
}