package org.example.universityservice.vo.paper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SuggestionVO {

    private Long id;                // 主键ID

    @NotNull(message = "论文ID不能为空")
    private Long paperId;           // 关联的论文ID

    @NotBlank(message = "修改意见不能为空")
    private String content;         // 修改意见内容

    private LocalDateTime createdAt; // 意见提出时间
}