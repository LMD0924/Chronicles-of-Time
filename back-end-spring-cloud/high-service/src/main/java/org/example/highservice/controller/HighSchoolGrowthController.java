package org.example.highservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commondb.utils.RestBean;
import org.example.highservice.dto.HighSchoolGrowthDTO;
import org.example.highservice.dto.HighSchoolGrowthQueryDTO;
import org.example.highservice.dto.HighSchoolGrowthStatsDTO;
import org.example.highservice.service.HighSchoolGrowthService;
import org.example.highservice.vo.HighSchoolGrowthVO;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 高中成长记录 Controller
 * @Author:总会落叶
 * @Date:2026/3/31
 */
@Slf4j
@RestController
@RequestMapping("/api/highschool/growth")
@RequiredArgsConstructor
public class HighSchoolGrowthController {

    private final HighSchoolGrowthService growthService;

    /**
     * 新增成长记录
     */
    @PostMapping("/add")
    public RestBean<Boolean> addRecord(@RequestBody HighSchoolGrowthDTO dto,
                                       HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        dto.setUserId(userId);

        boolean result = growthService.addRecord(dto);
        if (result) {
            return RestBean.success("添加成功", true);
        } else {
            return RestBean.fail("添加失败");
        }
    }

    /**
     * 更新成长记录
     */
    @PostMapping("/update")
    public RestBean<Boolean> updateRecord(@RequestBody HighSchoolGrowthDTO dto,
                                          HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        dto.setUserId(userId);

        boolean result = growthService.updateRecord(dto);
        if (result) {
            return RestBean.success("更新成功", true);
        } else {
            return RestBean.fail("更新失败");
        }
    }

    /**
     * 删除成长记录
     */
    @PostMapping("/delete")
    public RestBean<Boolean> deleteRecord(@RequestParam Long id,
                                          HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        boolean result = growthService.deleteRecord(id, userId);
        if (result) {
            return RestBean.success("删除成功", true);
        } else {
            return RestBean.fail("删除失败");
        }
    }

    /**
     * 批量删除
     */
    @PostMapping("/batchDelete")
    public RestBean<Boolean> batchDelete(@RequestBody List<Long> ids,
                                         HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        boolean result = growthService.batchDelete(ids, userId);
        if (result) {
            return RestBean.success("批量删除成功", true);
        } else {
            return RestBean.fail("批量删除失败");
        }
    }

    /**
     * 查询成长记录详情
     */
    @GetMapping("/detail")
    public RestBean<HighSchoolGrowthVO> getDetail(@RequestParam Long id,
                                                  HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        HighSchoolGrowthVO vo = growthService.getRecordById(id, userId);
        if (vo != null) {
            return RestBean.success("查询成功", vo);
        } else {
            return RestBean.fail("记录不存在");
        }
    }

    /**
     * 分页查询成长记录列表
     */
    @PostMapping("/list")
    public RestBean<List<HighSchoolGrowthVO>> queryList(@RequestBody HighSchoolGrowthQueryDTO queryDTO,
                                                        HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        queryDTO.setUserId(userId);

        List<HighSchoolGrowthVO> list = growthService.queryRecords(queryDTO);
        return RestBean.success("查询成功", list);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public RestBean<HighSchoolGrowthStatsDTO> getStats(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        HighSchoolGrowthStatsDTO stats = growthService.getStats(userId);
        return RestBean.success("获取成功", stats);
    }

    /**
     * 获取成长趋势
     */
    @GetMapping("/trend")
    public RestBean<List<Map<String, Object>>> getGrowthTrend(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        List<Map<String, Object>> trend = growthService.getGrowthTrend(userId);
        return RestBean.success("获取成功", trend);
    }

    /**
     * 获取里程碑记录
     */
    @GetMapping("/milestones")
    public RestBean<List<HighSchoolGrowthVO>> getMilestones(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        List<HighSchoolGrowthVO> milestones = growthService.getMilestones(userId);
        return RestBean.success("获取成功", milestones);
    }

    /**
     * 按年级统计
     */
    @GetMapping("/countByGrade")
    public RestBean<List<Map<String, Object>>> countByGrade(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null) {
            return RestBean.fail("用户未登录");
        }

        Long userId = Long.parseLong(userIdStr);
        List<Map<String, Object>> result = growthService.countByGrade(userId);
        return RestBean.success("获取成功", result);
    }
}