package org.example.highservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.highservice.entity.CourseGuidance;

import java.util.List;

public interface CourseGuidanceService extends IService<CourseGuidance> {
    CourseGuidance saveOrUpdateByStudent(CourseGuidance guidance);
    List<CourseGuidance> listByStudentId(Long studentId);
}
