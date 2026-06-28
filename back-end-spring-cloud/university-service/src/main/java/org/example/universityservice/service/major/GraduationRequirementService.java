package org.example.universityservice.service.major;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.major.GraduationRequirement;

public interface GraduationRequirementService extends IService<GraduationRequirement> {

    /**
     * 根据用户ID和专业ID获取毕业要求
     */
    GraduationRequirement getByUserIdAndMajorId(Long userId, Long majorId);

    /**
     * 创建或更新毕业要求
     */
    boolean saveOrUpdate(GraduationRequirement requirement);

    /**
     * 计算并更新毕业进度
     */
    void calculateProgress(Long userId, Long majorId);
}