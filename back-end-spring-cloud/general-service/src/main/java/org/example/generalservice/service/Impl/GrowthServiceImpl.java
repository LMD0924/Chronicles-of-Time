/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 高中成长记录 Service 实现类（集成选课功能）
 */
package org.example.generalservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncore.utils.MyBeanUtils;
import org.example.generalservice.dto.growth.GrowthDTO;
import org.example.generalservice.dto.growth.GrowthQueryDTO;
import org.example.generalservice.dto.growth.GrowthStatsDTO;
import org.example.generalservice.entity.Growth;
import org.example.generalservice.mapper.GrowthMapper;
import org.example.generalservice.service.GrowthService;
import org.example.generalservice.vo.GrowthVO;
import org.example.highservice.entity.GradingScale;
import org.example.highservice.entity.StudentCourseSelection;
import org.example.highservice.mapper.GradingScaleMapper;
import org.example.highservice.mapper.StudentCourseSelectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 高中成长记录 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrowthServiceImpl extends ServiceImpl<GrowthMapper, Growth>
        implements GrowthService {

    private final GrowthMapper growthMapper;
    private final GradingScaleMapper gradingScaleMapper;
    private final StudentCourseSelectionMapper studentCourseSelectionMapper; // 添加选课Mapper

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRecord(GrowthDTO dto) {
        Growth entity = new Growth();
        MyBeanUtils.copyNonNullProperties(dto, entity);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        int result = growthMapper.insert(entity);
        log.info("新增成长记录成功，ID: {}, 用户ID: {}", entity.getId(), dto.getUserId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRecord(GrowthDTO dto) {
        if (dto.getId() == null) {
            log.error("更新失败：记录ID不能为空");
            return false;
        }

        // 检查记录是否存在
        Growth existing = growthMapper.selectById(dto.getId());
        if (existing == null) {
            log.error("更新失败：记录不存在，ID: {}", dto.getId());
            return false;
        }

        // 复制非空属性
        MyBeanUtils.copyNonNullProperties(dto, existing);
        existing.setUpdateTime(LocalDateTime.now());

        int result = growthMapper.updateById(existing);
        log.info("更新成长记录成功，ID: {}", dto.getId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRecord(Long id, Long userId) {
        LambdaQueryWrapper<Growth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Growth::getId, id)
                .eq(Growth::getUserId, userId);

        int result = growthMapper.delete(wrapper);
        log.info("删除成长记录成功，ID: {}, 用户ID: {}", id, userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(List<Long> ids, Long userId) {
        LambdaQueryWrapper<Growth> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Growth::getId, ids)
                .eq(Growth::getUserId, userId);

        int result = growthMapper.delete(wrapper);
        log.info("批量删除成长记录成功，数量: {}, 用户ID: {}", result, userId);
        return result > 0;
    }

    @Override
    public GrowthVO getRecordById(Long id, Long userId) {
        LambdaQueryWrapper<Growth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Growth::getId, id)
                .eq(Growth::getUserId, userId);

        Growth entity = growthMapper.selectOne(wrapper);
        if (entity == null) {
            return null;
        }

        return convertToVO(entity);
    }

    @Override
    public List<GrowthVO> queryRecords(GrowthQueryDTO queryDTO) {
        LambdaQueryWrapper<Growth> wrapper = buildQueryWrapper(queryDTO);

        // 分页查询
        Page<Growth> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Growth> resultPage = growthMapper.selectPage(page, wrapper);

        return resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public GrowthStatsDTO getStats(Long userId) {
        // 查询所有记录
        LambdaQueryWrapper<Growth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Growth::getUserId, userId);
        List<Growth> records = growthMapper.selectList(wrapper);

        if (records.isEmpty()) {
            return new GrowthStatsDTO();
        }

        GrowthStatsDTO stats = new GrowthStatsDTO();

        // 基础统计
        stats.setTotalRecords(records.size());
        stats.setMilestoneCount((int) records.stream().filter(r -> Boolean.TRUE.equals(r.getIsMilestone())).count());

        // 平均值统计
        stats.setAvgStudyHours(calculateAvgBigDecimal(records, Growth::getStudyHours));
        stats.setAvgStressLevel(calculateAvgInteger(records, Growth::getStressLevel));
        stats.setAvgHappinessLevel(calculateAvgInteger(records, Growth::getHappinessLevel));
        stats.setAvgSleepHours(calculateAvgBigDecimal(records, Growth::getSleepHours));
        stats.setAvgExerciseMinutes(calculateAvgInteger(records, Growth::getExerciseMinutes));

        // 获取最新的一条记录中的职业兴趣等信息
        Growth latest = records.stream()
                .max((a, b) -> a.getRecordDate().compareTo(b.getRecordDate()))
                .orElse(null);

        if (latest != null) {
            stats.setCareerInterest(latest.getCareerInterest());
            stats.setDreamCollege(latest.getDreamCollege());
            stats.setDreamMajor(latest.getDreamMajor());
        }



        return stats;
    }

    @Override
    public List<Map<String, Object>> getGrowthTrend(Long userId) {
        return growthMapper.getGrowthTrend(userId);
    }

    @Override
    public List<GrowthVO> getMilestones(Long userId) {
        List<Growth> milestones = growthMapper.getMilestones(userId);
        return milestones.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> countByStage(Long userId) {
        return growthMapper.countByStage(userId);
    }

    // ==================== 选课相关功能 ====================

    /**
     * 获取热门组合排名
     */
    public List<Map<String, Object>> getHotCombinations() {
        log.info("获取热门组合排名");
        return studentCourseSelectionMapper.getCombinationStatistics();
    }

    /**
     * 获取年级排名前N的学生
     */
    public List<StudentCourseSelection> getTopStudents(String stage, int limit) {
        log.info("获取年级排名前{}的学生，阶段: {}", limit, stage);
        return studentCourseSelectionMapper.getTopStudents(stage, limit);
    }

    /**
     * 获取班级成绩统计
     */
    public List<Map<String, Object>> getClassScoreStatistics(String stage) {
        log.info("获取班级成绩统计，阶段: {}", stage);
        return studentCourseSelectionMapper.getClassScoreStatistics(stage);
    }

    /**
     * 获取各组合的平均分排名
     */
    public List<Map<String, Object>> getCombinationScoreRanking(String stage) {
        log.info("获取各组合平均分排名，阶段: {}", stage);
        return studentCourseSelectionMapper.getCombinationScoreRanking(stage);
    }

    /**
     * 获取选课趋势
     */
    public List<Map<String, Object>> getSelectionTrend() {
        log.info("获取选课趋势");
        return studentCourseSelectionMapper.getSelectionTrend();
    }

    /**
     * 根据专业推荐选课组合
     */
    public List<Map<String, Object>> recommendByMajor(String majorName) {
        log.info("根据专业推荐选课组合，专业: {}", majorName);
        return studentCourseSelectionMapper.recommendByMajor(majorName);
    }

    /**
     * 获取公开的选课记录
     */
    public List<StudentCourseSelection> getPublicSelections() {
        log.info("获取公开的选课记录");
        return studentCourseSelectionMapper.getPublicSelections();
    }

    /**
     * 获取学生的选课记录
     */
    public List<StudentCourseSelection> getStudentSelections(Long studentId) {
        log.info("获取学生选课记录，学生ID: {}", studentId);
        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getStudentId, studentId)
                .orderByDesc(StudentCourseSelection::getCreateTime);
        return studentCourseSelectionMapper.selectList(wrapper);
    }

    // ==================== 赋分计算 ====================

    /**
     * 计算赋分成绩
     */
    @Override
    public BigDecimal calculateWeightedScore(Long subjectId, BigDecimal rawScore, String academicYear) {
        if (rawScore == null) {
            return null;
        }

        // 使用注入的gradingScaleMapper
        GradingScale scale = gradingScaleMapper.getScaleByRawScore(subjectId, rawScore, academicYear);

        if (scale != null) {
            // 线性转换赋分
            BigDecimal range = scale.getRawScoreMax().subtract(scale.getRawScoreMin());
            if (range.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = rawScore.subtract(scale.getRawScoreMin())
                        .divide(range, 4, RoundingMode.HALF_UP);
                BigDecimal weightedScore = BigDecimal.valueOf(scale.getAssignedScoreMin())
                        .add(ratio.multiply(BigDecimal.valueOf(scale.getAssignedScoreMax() - scale.getAssignedScoreMin())));
                log.debug("赋分计算: 科目ID={}, 原始分={}, 赋分={}", subjectId, rawScore, weightedScore);
                return weightedScore.setScale(2, RoundingMode.HALF_UP);
            }
        }

        log.warn("未找到赋分等级配置，返回原始分: 科目ID={}, 原始分={}", subjectId, rawScore);
        return rawScore;
    }

    /**
     * 批量计算赋分成绩
     */
    public List<Map<String, Object>> batchCalculateWeightedScore(List<Map<String, Object>> scoreList, String academicYear) {
        return scoreList.stream().map(score -> {
            Long subjectId = ((Number) score.get("subjectId")).longValue();
            BigDecimal rawScore = (BigDecimal) score.get("rawScore");
            BigDecimal weightedScore = calculateWeightedScore(subjectId, rawScore, academicYear);
            score.put("weightedScore", weightedScore);
            return score;
        }).collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Growth> buildQueryWrapper(GrowthQueryDTO queryDTO) {
        LambdaQueryWrapper<Growth> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getUserId() != null) {
            wrapper.eq(Growth::getUserId, queryDTO.getUserId());
        }
        if (StringUtils.hasText(queryDTO.getStage())) {
            wrapper.eq(Growth::getStage, queryDTO.getStage());
        }
        if (StringUtils.hasText(queryDTO.getSemester())) {
            wrapper.eq(Growth::getSemester, queryDTO.getSemester());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(Growth::getRecordDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(Growth::getRecordDate, queryDTO.getEndDate());
        }
        if (queryDTO.getIsMilestone() != null) {
            wrapper.eq(Growth::getIsMilestone, queryDTO.getIsMilestone());
        }

        wrapper.orderByDesc(Growth::getRecordDate);

        return wrapper;
    }

    /**
     * 计算 BigDecimal 类型的平均值
     */
    private BigDecimal calculateAvgBigDecimal(List<Growth> records,
                                              java.util.function.Function<Growth, BigDecimal> extractor) {
        double average = records.stream()
                .map(extractor)
                .filter(value -> value != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 计算 Integer 类型的平均值（返回 BigDecimal 保留1位小数）
     */
    private BigDecimal calculateAvgInteger(List<Growth> records,
                                           java.util.function.Function<Growth, Integer> extractor) {
        double average = records.stream()
                .map(extractor)
                .filter(value -> value != null)
                .mapToDouble(Integer::doubleValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 转换为 VO
     */
    private GrowthVO convertToVO(Growth entity) {
        GrowthVO vo = new GrowthVO();
        MyBeanUtils.copyNonNullProperties(entity, vo);
        return vo;
    }
}