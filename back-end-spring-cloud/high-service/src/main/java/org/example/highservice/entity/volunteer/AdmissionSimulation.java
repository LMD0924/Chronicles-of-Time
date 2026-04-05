package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 录取模拟表实体
 */
@Data
@TableName("admission_simulation")
public class AdmissionSimulation {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer volunteerDetailId;

    private String simulationStatus; // pending, admitted, rejected, waiting

    private String rejectReason;

    private Double predictedProbability;

    private LocalDateTime admittedTime;

    private Integer scoreDiff;
}