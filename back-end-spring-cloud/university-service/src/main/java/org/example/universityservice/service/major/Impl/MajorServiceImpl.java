/**
 * 文件说明：拾光记微服务后端大学服务业务服务源码，负责业务服务相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.universityservice.service.major.Impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.major.Course;
import org.example.universityservice.entity.major.Major;
import org.example.universityservice.mapper.major.MajorMapper;
import org.example.universityservice.service.major.CourseService;
import org.example.universityservice.service.major.MajorService;
import org.example.universityservice.vo.major.MajorCompareVO;
import org.example.universityservice.vo.major.MajorVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 类说明：当前类是业务服务模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("cot_university")
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    private final MajorMapper majorMapper;
    private CourseService courseService;

    @Autowired
    @Lazy
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    @Override
    public List<Major> getAllMajors() {
        return majorMapper.selectList(null);
    }

    @Override
    public Major getMajorById(Long id) {
        return majorMapper.selectById(id);
    }

    @Override
    public Major getMajorByCode(String code) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getCode, code);
        return majorMapper.selectOne(wrapper);
    }

    @Override
    public boolean createMajor(Major major) {
        return majorMapper.insert(major) > 0;
    }

    @Override
    public boolean updateMajor(Major major) {
        return majorMapper.updateById(major) > 0;
    }

    @Override
    public boolean deleteMajor(Long id) {
        return majorMapper.deleteById(id) > 0;
    }

    @Override
    public MajorCompareVO compareMajors(Long majorId1, Long majorId2) {
        MajorCompareVO vo = new MajorCompareVO();
        Major m1 = getMajorById(majorId1);
        Major m2 = getMajorById(majorId2);
        if (m1 != null) {
            MajorVO a = new MajorVO();
            BeanUtils.copyProperties(m1, a);
            vo.setMajorA(a);
        }
        if (m2 != null) {
            MajorVO b = new MajorVO();
            BeanUtils.copyProperties(m2, b);
            vo.setMajorB(b);
        }
        List<Course> c1 = courseService.getCoursesByMajorId(majorId1);
        List<Course> c2 = courseService.getCoursesByMajorId(majorId2);
        Map<String, Course> map2 = c2.stream().collect(Collectors.toMap(
                c -> c.getCourseCode() != null ? c.getCourseCode() : c.getName(),
                c -> c, (a, b) -> a));
        Set<String> codes1 = new HashSet<>();
        for (Course course : c1) {
            String key = course.getCourseCode() != null ? course.getCourseCode() : course.getName();
            codes1.add(key);
            if (map2.containsKey(key)) {
                vo.getShared().add(toCompareItem(course));
            } else {
                vo.getOnlyInA().add(toCompareItem(course));
            }
        }
        for (Course course : c2) {
            String key = course.getCourseCode() != null ? course.getCourseCode() : course.getName();
            if (!codes1.contains(key)) {
                vo.getOnlyInB().add(toCompareItem(course));
            }
        }
        vo.setOnlyInACount(vo.getOnlyInA().size());
        vo.setOnlyInBCount(vo.getOnlyInB().size());
        vo.setSharedCount(vo.getShared().size());
        return vo;
    }

    private MajorCompareVO.CourseCompareItemVO toCompareItem(Course course) {
        MajorCompareVO.CourseCompareItemVO item = new MajorCompareVO.CourseCompareItemVO();
        item.setCourseCode(course.getCourseCode());
        item.setName(course.getName());
        item.setCredit(course.getCredit());
        item.setCourseType(course.getCourseType());
        item.setTerm(course.getTerm());
        return item;
    }
}