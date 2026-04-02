/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 高中成长记录 Service 实现类（集成选课功能）
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.commoncore.utils.MyBeanUtils;
import org.example.highservice.dto.HighSchoolGrowthDTO;
import org.example.highservice.dto.HighSchoolGrowthQueryDTO;
import org.example.highservice.dto.HighSchoolGrowthStatsDTO;
import org.example.highservice.entity.GradingScale;
import org.example.highservice.entity.HighSchoolGrowth;
import org.example.highservice.entity.StudentCourseSelection;
import org.example.highservice.mapper.GradingScaleMapper;
import org.example.highservice.mapper.HighSchoolGrowthMapper;
import org.example.highservice.mapper.StudentCourseSelectionMapper;
import org.example.highservice.service.HighSchoolGrowthService;
import org.example.highservice.vo.HighSchoolGrowthVO;
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
public class HighSchoolGrowthServiceImpl extends ServiceImpl<HighSchoolGrowthMapper, HighSchoolGrowth>
        implements HighSchoolGrowthService {

    private final HighSchoolGrowthMapper growthMapper;
    private final GradingScaleMapper gradingScaleMapper;
    private final StudentCourseSelectionMapper studentCourseSelectionMapper; // 添加选课Mapper

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRecord(HighSchoolGrowthDTO dto) {
        HighSchoolGrowth entity = new HighSchoolGrowth();
        MyBeanUtils.copyNonNullProperties(dto, entity);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        int result = growthMapper.insert(entity);
        log.info("新增成长记录成功，ID: {}, 用户ID: {}", entity.getId(), dto.getUserId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRecord(HighSchoolGrowthDTO dto) {
        if (dto.getId() == null) {
            log.error("更新失败：记录ID不能为空");
            return false;
        }

        // 检查记录是否存在
        HighSchoolGrowth existing = growthMapper.selectById(dto.getId());
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
        LambdaQueryWrapper<HighSchoolGrowth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HighSchoolGrowth::getId, id)
                .eq(HighSchoolGrowth::getUserId, userId);

        int result = growthMapper.delete(wrapper);
        log.info("删除成长记录成功，ID: {}, 用户ID: {}", id, userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelete(List<Long> ids, Long userId) {
        LambdaQueryWrapper<HighSchoolGrowth> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(HighSchoolGrowth::getId, ids)
                .eq(HighSchoolGrowth::getUserId, userId);

        int result = growthMapper.delete(wrapper);
        log.info("批量删除成长记录成功，数量: {}, 用户ID: {}", result, userId);
        return result > 0;
    }

    @Override
    public HighSchoolGrowthVO getRecordById(Long id, Long userId) {
        LambdaQueryWrapper<HighSchoolGrowth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HighSchoolGrowth::getId, id)
                .eq(HighSchoolGrowth::getUserId, userId);

        HighSchoolGrowth entity = growthMapper.selectOne(wrapper);
        if (entity == null) {
            return null;
        }

        return convertToVO(entity);
    }

    @Override
    public List<HighSchoolGrowthVO> queryRecords(HighSchoolGrowthQueryDTO queryDTO) {
        LambdaQueryWrapper<HighSchoolGrowth> wrapper = buildQueryWrapper(queryDTO);

        // 分页查询
        Page<HighSchoolGrowth> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<HighSchoolGrowth> resultPage = growthMapper.selectPage(page, wrapper);

        return resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public HighSchoolGrowthStatsDTO getStats(Long userId) {
        // 查询所有记录
        LambdaQueryWrapper<HighSchoolGrowth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HighSchoolGrowth::getUserId, userId);
        List<HighSchoolGrowth> records = growthMapper.selectList(wrapper);

        if (records.isEmpty()) {
            return new HighSchoolGrowthStatsDTO();
        }

        HighSchoolGrowthStatsDTO stats = new HighSchoolGrowthStatsDTO();

        // 基础统计
        stats.setTotalRecords(records.size());
        stats.setMilestoneCount((int) records.stream().filter(r -> Boolean.TRUE.equals(r.getIsMilestone())).count());

        // 平均值统计
        stats.setAvgStudyHours(calculateAvgBigDecimal(records, HighSchoolGrowth::getStudyHours));
        stats.setAvgStressLevel(calculateAvgInteger(records, HighSchoolGrowth::getStressLevel));
        stats.setAvgHappinessLevel(calculateAvgInteger(records, HighSchoolGrowth::getHappinessLevel));
        stats.setAvgSleepHours(calculateAvgBigDecimal(records, HighSchoolGrowth::getSleepHours));
        stats.setAvgExerciseMinutes(calculateAvgInteger(records, HighSchoolGrowth::getExerciseMinutes));

        // 获取最新的一条记录中的职业兴趣等信息
        HighSchoolGrowth latest = records.stream()
                .max((a, b) -> a.getRecordDate().compareTo(b.getRecordDate()))
                .orElse(null);

        if (latest != null) {
            stats.setCareerInterest(latest.getCareerInterest());
            stats.setDreamCollege(latest.getDreamCollege());
            stats.setDreamMajor(latest.getDreamMajor());
        }

        // 各年级统计
        stats.setGrade10Count((int) records.stream().filter(r -> "高一".equals(r.getGrade())).count());
        stats.setGrade11Count((int) records.stream().filter(r -> "高二".equals(r.getGrade())).count());
        stats.setGrade12Count((int) records.stream().filter(r -> "高三".equals(r.getGrade())).count());

        return stats;
    }

    @Override
    public List<Map<String, Object>> getGrowthTrend(Long userId) {
        return growthMapper.getGrowthTrend(userId);
    }

    @Override
    public List<HighSchoolGrowthVO> getMilestones(Long userId) {
        List<HighSchoolGrowth> milestones = growthMapper.getMilestones(userId);
        return milestones.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> countByGrade(Long userId) {
        return growthMapper.countByGrade(userId);
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
    public List<StudentCourseSelection> getTopStudents(String grade, int limit) {
        log.info("获取年级排名前{}的学生，年级: {}", limit, grade);
        return studentCourseSelectionMapper.getTopStudents(grade, limit);
    }

    /**
     * 获取班级成绩统计
     */
    public List<Map<String, Object>> getClassScoreStatistics(String grade) {
        log.info("获取班级成绩统计，年级: {}", grade);
        return studentCourseSelectionMapper.getClassScoreStatistics(grade);
    }

    /**
     * 获取各组合的平均分排名
     */
    public List<Map<String, Object>> getCombinationScoreRanking(String grade) {
        log.info("获取各组合平均分排名，年级: {}", grade);
        return studentCourseSelectionMapper.getCombinationScoreRanking(grade);
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
    private LambdaQueryWrapper<HighSchoolGrowth> buildQueryWrapper(HighSchoolGrowthQueryDTO queryDTO) {
        LambdaQueryWrapper<HighSchoolGrowth> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getUserId() != null) {
            wrapper.eq(HighSchoolGrowth::getUserId, queryDTO.getUserId());
        }
        if (StringUtils.hasText(queryDTO.getGrade())) {
            wrapper.eq(HighSchoolGrowth::getGrade, queryDTO.getGrade());
        }
        if (StringUtils.hasText(queryDTO.getSemester())) {
            wrapper.eq(HighSchoolGrowth::getSemester, queryDTO.getSemester());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(HighSchoolGrowth::getRecordDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(HighSchoolGrowth::getRecordDate, queryDTO.getEndDate());
        }
        if (queryDTO.getIsMilestone() != null) {
            wrapper.eq(HighSchoolGrowth::getIsMilestone, queryDTO.getIsMilestone());
        }

        wrapper.orderByDesc(HighSchoolGrowth::getRecordDate);

        return wrapper;
    }

    /**
     * 计算 BigDecimal 类型的平均值
     */
    private BigDecimal calculateAvgBigDecimal(List<HighSchoolGrowth> records,
                                              java.util.function.Function<HighSchoolGrowth, BigDecimal> extractor) {
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
    private BigDecimal calculateAvgInteger(List<HighSchoolGrowth> records,
                                           java.util.function.Function<HighSchoolGrowth, Integer> extractor) {
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
    private HighSchoolGrowthVO convertToVO(HighSchoolGrowth entity) {
        HighSchoolGrowthVO vo = new HighSchoolGrowthVO();
        MyBeanUtils.copyNonNullProperties(entity, vo);
        return vo;
    }
}