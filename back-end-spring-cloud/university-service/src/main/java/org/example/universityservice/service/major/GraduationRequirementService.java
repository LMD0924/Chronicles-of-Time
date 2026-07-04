/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.major;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.universityservice.entity.major.GraduationRequirement;
/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */

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