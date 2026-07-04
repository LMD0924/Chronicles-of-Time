/**
 * 文件说明：拾光记微服务后端用户中心数据访问映射源码，负责数据访问映射相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.usercenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.usercenter.entity.UserInfo;

/*
 * @Author:总会落叶
 * @Date:2026/4/5
 * @Description:
 */
/**
 * 类说明：当前类是数据访问映射模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
