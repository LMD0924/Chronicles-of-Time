package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.MajorSubjectMatching;
import org.example.highservice.mapper.MajorSubjectMatchingMapper;
import org.example.highservice.service.MajorSubjectMatchingService;
import org.springframework.stereotype.Service;

@Service
public class MajorSubjectMatchingServiceImpl
        extends ServiceImpl<MajorSubjectMatchingMapper, MajorSubjectMatching>
        implements MajorSubjectMatchingService {
}
