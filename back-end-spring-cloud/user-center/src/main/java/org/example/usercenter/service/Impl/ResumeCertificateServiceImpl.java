package org.example.usercenter.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.usercenter.entity.ResumeCertificate;
import org.example.usercenter.mapper.ResumeCertificateMapper;
import org.example.usercenter.service.ResumeCertificateService;
import org.springframework.stereotype.Service;

/*
 * @Author:总会落叶
 * @Date:2026/3/27
 * @Description: 证书Service实现类
 */
@Service
@RequiredArgsConstructor
public class ResumeCertificateServiceImpl extends ServiceImpl<ResumeCertificateMapper, ResumeCertificate> implements ResumeCertificateService {

        private final ResumeCertificateMapper resumeCertificateMapper;

    /**
     * 新增证书
     */
    @Override
    public Integer addCertificate(ResumeCertificate resumeCertificate) {
        return resumeCertificateMapper.insert(resumeCertificate);
    }
}