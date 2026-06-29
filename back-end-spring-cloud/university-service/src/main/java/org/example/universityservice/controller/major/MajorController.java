package org.example.universityservice.controller.major;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.universityservice.entity.major.Major;
import org.example.universityservice.service.major.MajorService;
import org.example.universityservice.vo.major.MajorCompareVO;
import org.example.universityservice.vo.major.MajorVO;
import org.example.commondb.utils.RestBean;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/university/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    @GetMapping("/list")
    public RestBean<List<MajorVO>> getAllMajors() {
        List<Major> majors = majorService.getAllMajors();
        List<MajorVO> voList = majors.stream().map(m -> {
            MajorVO vo = new MajorVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).collect(Collectors.toList());
        return RestBean.success(voList);
    }

    @GetMapping("/{id}")
    public RestBean<MajorVO> getMajorById(@PathVariable Long id) {
        Major major = majorService.getMajorById(id);
        if (major == null) {
            return RestBean.fail("专业不存在");
        }
        MajorVO vo = new MajorVO();
        BeanUtils.copyProperties(major, vo);
        return RestBean.success(vo);
    }

    @PostMapping("/create")
    public RestBean<String> createMajor(@Valid @RequestBody MajorVO majorVO) {
        Major major = new Major();
        BeanUtils.copyProperties(majorVO, major);
        boolean success = majorService.createMajor(major);
        return success ? RestBean.success("创建成功") : RestBean.fail("创建失败");
    }

    @PutMapping("/update")
    public RestBean<String> updateMajor(@RequestBody MajorVO majorVO) {
        if (majorVO.getId() == null) {
            return RestBean.fail("专业ID不能为空");
        }
        Major major = new Major();
        BeanUtils.copyProperties(majorVO, major);
        boolean success = majorService.updateMajor(major);
        return success ? RestBean.success("更新成功") : RestBean.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public RestBean<String> deleteMajor(@PathVariable Long id) {
        boolean success = majorService.deleteMajor(id);
        return success ? RestBean.success("删除成功") : RestBean.fail("删除失败");
    }

    @GetMapping("/compare")
    public RestBean<MajorCompareVO> compareMajors(
            @RequestParam Long majorId1,
            @RequestParam Long majorId2) {
        return RestBean.success(majorService.compareMajors(majorId1, majorId2));
    }
}