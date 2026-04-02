package org.example.highservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.highservice.entity.CourseSelectionIntention;

import java.util.List;

public interface CourseSelectionIntentionService extends IService<CourseSelectionIntention> {
    CourseSelectionIntention saveOrUpdateByStudent(CourseSelectionIntention intention);
    List<CourseSelectionIntention> listByStudentId(Long studentId);
}
