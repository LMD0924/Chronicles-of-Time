package org.example.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.usercenter.entity.ResumeCertificate;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 证书Service接口
 */
public interface ResumeCertificateService extends IService<ResumeCertificate> {

    /**
     * 新增证书
     */
    Integer addCertificate(ResumeCertificate resumeCertificate);

}