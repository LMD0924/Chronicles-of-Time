package org.example.highservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * @Author:总会落叶
 * @Date:2026/4/3
 * @Description: 成绩记录DTO
 */
@Data
public class ScoreRecordDTO {
    private Integer userId;
    private String subjectName;
    private BigDecimal score;
    private LocalDate examDate;
}