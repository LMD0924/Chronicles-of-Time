/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.entity.volunteer;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 录取模拟表实体
 */
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Data
@TableName("admission_simulation")
public class AdmissionSimulation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long volunteerDetailId;

    private String simulationStatus; // pending, admitted, rejected, waiting

    private String rejectReason;

    private Double predictedProbability;

    private LocalDateTime admittedTime;

    private Integer scoreDiff;
}