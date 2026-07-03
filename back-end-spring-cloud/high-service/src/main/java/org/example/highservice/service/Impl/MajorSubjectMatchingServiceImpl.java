/**
 * 文件说明：拾光记微服务后端高中服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.MajorSubjectMatching;
import org.example.highservice.mapper.MajorSubjectMatchingMapper;
import org.example.highservice.service.MajorSubjectMatchingService;
import org.springframework.stereotype.Service;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Service
public class MajorSubjectMatchingServiceImpl
        extends ServiceImpl<MajorSubjectMatchingMapper, MajorSubjectMatching>
        implements MajorSubjectMatchingService {
}
