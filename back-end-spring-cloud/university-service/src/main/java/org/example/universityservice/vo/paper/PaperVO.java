package org.example.universityservice.vo.paper;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaperVO {

    private Long id;                // 主键ID

    private Long userId;            // 用户ID

    @NotBlank(message = "论文题目不能为空")
    private String title;           // 论文题目

    private String supervisor;      // 导师姓名

    private String direction;       // 研究方向

    private String content;         // 论文内容

    private LocalDateTime createdAt; // 创建时间

    private LocalDateTime updatedAt; // 更新时间
}