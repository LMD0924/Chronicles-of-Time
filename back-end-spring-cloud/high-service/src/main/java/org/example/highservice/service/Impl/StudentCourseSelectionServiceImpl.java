/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 学生选课记录Service实现类（完整版）
 */
package org.example.highservice.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.highservice.entity.*;
import org.example.highservice.mapper.*;
import org.example.highservice.service.StudentCourseSelectionService;
import org.example.highservice.dto.SelectionQueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentCourseSelectionServiceImpl
        extends ServiceImpl<StudentCourseSelectionMapper, StudentCourseSelection>
        implements StudentCourseSelectionService {

    @Autowired
    private StudentCourseSelectionMapper selectionMapper;

    @Autowired
    private SubjectCombinationMapper combinationMapper;

    @Autowired
    private GradingScaleMapper gradingScaleMapper;

    @Autowired
    private CourseSelectionHistoryMapper historyMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private MajorRequirementMapper majorRequirementMapper;

    // ==================== 基础选课操作 ====================

    @Override
    @Transactional
    public boolean selectCourse(StudentCourseSelection selection) {
        // 参数校验
        if (selection.getStudentId() == null || selection.getGrade() == null) {
            return false;
        }

        // 检查是否已经选课
        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getStudentId, selection.getStudentId())
                .eq(StudentCourseSelection::getAcademicYear, selection.getAcademicYear())
                .eq(StudentCourseSelection::getSemester, selection.getSemester())
                .ne(StudentCourseSelection::getStatus, 4); // 排除已退选的
        long count = this.count(wrapper);
        if (count > 0) {
            return false; // 已经选过课了
        }

        // 设置初始状态
        selection.setStatus(1);
        selection.setIsConfirmed(false);
        selection.setIsPublic(false); // 默认不公开
        selection.setCreateTime(LocalDateTime.now());
        selection.setUpdateTime(LocalDateTime.now());

        // 计算总分
        calculateTotalScore(selection);

        // 计算赋分成绩
        calculateWeightedScores(selection);

        // 保存记录
        boolean success = this.save(selection);

        // 记录历史
        if (success) {
            recordHistory(selection, "1", null, "首次选课");
        }

        return success;
    }

    @Override
    @Transactional
    public boolean confirmSelection(Long id) {
        StudentCourseSelection selection = this.getById(id);
        if (selection == null || selection.getIsConfirmed()) {
            return false;
        }

        selection.setIsConfirmed(true);
        selection.setStatus(2);
        selection.setConfirmTime(LocalDateTime.now());
        selection.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(selection);

        if (success) {
            recordHistory(selection, "4", null, "确认选课");
        }

        return success;
    }

    @Override
    @Transactional
    public boolean modifySelection(StudentCourseSelection newSelection) {
        StudentCourseSelection oldSelection = this.getById(newSelection.getId());
        if (oldSelection == null) {
            return false;
        }

        // 记录变更历史
        recordHistory(oldSelection, "2", newSelection, "修改选课");

        // 更新选课信息
        newSelection.setStatus(3);
        newSelection.setUpdateTime(LocalDateTime.now());
        newSelection.setIsConfirmed(false);

        // 重新计算总分和赋分
        calculateTotalScore(newSelection);
        calculateWeightedScores(newSelection);

        return this.updateById(newSelection);
    }

    @Override
    @Transactional
    public boolean cancelSelection(Long id, String reason) {
        StudentCourseSelection selection = this.getById(id);
        if (selection == null) {
            return false;
        }

        // 记录退选历史
        recordHistory(selection, "3", null, reason);

        // 更新状态
        selection.setStatus(4);
        selection.setIsConfirmed(false);
        selection.setUpdateTime(LocalDateTime.now());

        return this.updateById(selection);
    }

    // ==================== 查询方法 ====================

    @Override
    public List<StudentCourseSelection> getStudentSelections(Long studentId) {
        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getStudentId, studentId)
                .orderByDesc(StudentCourseSelection::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> getGradeStatistics(String grade, String academicYear) {
        Map<String, Object> stats = new HashMap<>();

        // 总人数
        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getGrade, grade)
                .eq(StudentCourseSelection::getAcademicYear, academicYear)
                .eq(StudentCourseSelection::getIsConfirmed, true);
        long totalCount = this.count(wrapper);
        stats.put("totalCount", totalCount);

        // 各组合统计
        List<Map<String, Object>> combinationStats = selectionMapper.getCombinationStatistics();
        stats.put("combinationStats", combinationStats);

        // 各首选科目统计
        List<Map<String, Object>> firstSubjectStats = selectionMapper.getFirstSubjectStatistics();
        stats.put("firstSubjectStats", firstSubjectStats);

        // 平均分统计
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getGrade, grade)
                .eq(StudentCourseSelection::getAcademicYear, academicYear)
                .eq(StudentCourseSelection::getIsConfirmed, true);
        List<StudentCourseSelection> selections = this.list(wrapper);

        if (!selections.isEmpty()) {
            BigDecimal avgTotal = selections.stream()
                    .map(StudentCourseSelection::getTotalScoreWeighted)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(selections.size()), 2, RoundingMode.HALF_UP);
            stats.put("avgTotalScore", avgTotal);

            // 最高分和最低分
            BigDecimal maxScore = selections.stream()
                    .map(StudentCourseSelection::getTotalScoreWeighted)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal minScore = selections.stream()
                    .map(StudentCourseSelection::getTotalScoreWeighted)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            stats.put("maxScore", maxScore);
            stats.put("minScore", minScore);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getClassStatistics(String grade, String className) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getGrade, grade)
                .eq(StudentCourseSelection::getClassName, className)
                .eq(StudentCourseSelection::getIsConfirmed, true);

        List<StudentCourseSelection> selections = this.list(wrapper);
        stats.put("selections", selections);
        stats.put("count", selections.size());

        // 班级平均分
        if (!selections.isEmpty()) {
            BigDecimal avgScore = selections.stream()
                    .map(StudentCourseSelection::getTotalScoreWeighted)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(selections.size()), 2, RoundingMode.HALF_UP);
            stats.put("avgScore", avgScore);

            // 各分数段统计
            Map<String, Integer> scoreRanges = new LinkedHashMap<>();
            scoreRanges.put("600分以上", 0);
            scoreRanges.put("550-600分", 0);
            scoreRanges.put("500-550分", 0);
            scoreRanges.put("450-500分", 0);
            scoreRanges.put("400-450分", 0);
            scoreRanges.put("400分以下", 0);

            for (StudentCourseSelection s : selections) {
                BigDecimal score = s.getTotalScoreWeighted();
                if (score == null) continue;
                if (score.compareTo(BigDecimal.valueOf(600)) >= 0) {
                    scoreRanges.put("600分以上", scoreRanges.get("600分以上") + 1);
                } else if (score.compareTo(BigDecimal.valueOf(550)) >= 0) {
                    scoreRanges.put("550-600分", scoreRanges.get("550-600分") + 1);
                } else if (score.compareTo(BigDecimal.valueOf(500)) >= 0) {
                    scoreRanges.put("500-550分", scoreRanges.get("500-550分") + 1);
                } else if (score.compareTo(BigDecimal.valueOf(450)) >= 0) {
                    scoreRanges.put("450-500分", scoreRanges.get("450-500分") + 1);
                } else if (score.compareTo(BigDecimal.valueOf(400)) >= 0) {
                    scoreRanges.put("400-450分", scoreRanges.get("400-450分") + 1);
                } else {
                    scoreRanges.put("400分以下", scoreRanges.get("400分以下") + 1);
                }
            }
            stats.put("scoreRanges", scoreRanges);
        }

        return stats;
    }

    // ==================== 隐私控制方法 ====================

    @Override
    public boolean updatePublicStatus(Long id, Boolean isPublic) {
        return selectionMapper.updatePublicStatus(id, isPublic) > 0;
    }

    @Override
    public boolean batchUpdatePublicStatus(List<Long> ids, Boolean isPublic) {
        return selectionMapper.batchUpdatePublicStatus(ids, isPublic) > 0;
    }

    @Override
    public List<StudentCourseSelection> getPublicSelections() {
        return selectionMapper.getPublicSelections();
    }

    // ==================== 推荐和统计方法 ====================

    @Override
    public List<Map<String, Object>> recommendByMajor(String majorName) {
        return selectionMapper.recommendByMajor(majorName);
    }

    @Override
    public List<Map<String, Object>> getHotCombinations() {
        return selectionMapper.getHotCombinations();
    }

    @Override
    public List<StudentCourseSelection> getTopStudents(String grade, int limit) {
        return selectionMapper.getTopStudents(grade, limit);
    }

    @Override
    public List<Map<String, Object>> getClassScoreStatistics(String grade) {
        return selectionMapper.getClassScoreStatistics(grade);
    }

    @Override
    public List<Map<String, Object>> getCombinationScoreRanking(String grade) {
        return selectionMapper.getCombinationScoreRanking(grade);
    }

    @Override
    public List<Map<String, Object>> getSelectionTrend() {
        return selectionMapper.getSelectionTrend();
    }

    // ==================== 赋分计算 ====================

    @Override
    public BigDecimal calculateWeightedScore(Long subjectId, BigDecimal rawScore, String academicYear) {
        if (rawScore == null) {
            return null;
        }

        GradingScale scale = gradingScaleMapper.getScaleByRawScore(subjectId, rawScore, academicYear);

        if (scale != null) {
            // 线性转换赋分
            BigDecimal range = scale.getRawScoreMax().subtract(scale.getRawScoreMin());
            if (range.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = rawScore.subtract(scale.getRawScoreMin())
                        .divide(range, 4, RoundingMode.HALF_UP);
                BigDecimal weightedScore = BigDecimal.valueOf(scale.getAssignedScoreMin())
                        .add(ratio.multiply(BigDecimal.valueOf(scale.getAssignedScoreMax() - scale.getAssignedScoreMin())));
                return weightedScore.setScale(2, RoundingMode.HALF_UP);
            }
        }

        return rawScore;
    }

    private void calculateWeightedScores(StudentCourseSelection selection) {
        String academicYear = selection.getAcademicYear();

        if (selection.getFirstSubjectScore() != null && selection.getFirstSubjectId() != null) {
            BigDecimal weightedFirst = calculateWeightedScore(
                    selection.getFirstSubjectId(),
                    selection.getFirstSubjectScore(),
                    academicYear
            );
            selection.setFirstSubjectLevel(getLevelByScore(weightedFirst));
        }

        if (selection.getSecondSubject1Score() != null && selection.getSecondSubject1Id() != null) {
            BigDecimal weightedSecond1 = calculateWeightedScore(
                    selection.getSecondSubject1Id(),
                    selection.getSecondSubject1Score(),
                    academicYear
            );
            selection.setSecondSubject1Level(getLevelByScore(weightedSecond1));
        }

        if (selection.getSecondSubject2Score() != null && selection.getSecondSubject2Id() != null) {
            BigDecimal weightedSecond2 = calculateWeightedScore(
                    selection.getSecondSubject2Id(),
                    selection.getSecondSubject2Score(),
                    academicYear
            );
            selection.setSecondSubject2Level(getLevelByScore(weightedSecond2));
        }
    }

    // ==================== 排名生成 ====================

    @Override
    @Transactional
    public void generateRank(String grade, String academicYear, String semester) {
        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getGrade, grade)
                .eq(StudentCourseSelection::getAcademicYear, academicYear)
                .eq(StudentCourseSelection::getSemester, semester)
                .eq(StudentCourseSelection::getIsConfirmed, true)
                .orderByDesc(StudentCourseSelection::getTotalScoreWeighted);

        List<StudentCourseSelection> selections = this.list(wrapper);

        if (selections.isEmpty()) {
            return;
        }

        // 年级排名
        for (int i = 0; i < selections.size(); i++) {
            selections.get(i).setGradeRank(i + 1);
        }

        // 班级排名
        Map<String, List<StudentCourseSelection>> classGroups = selections.stream()
                .filter(s -> s.getClassName() != null)
                .collect(Collectors.groupingBy(StudentCourseSelection::getClassName));

        for (Map.Entry<String, List<StudentCourseSelection>> entry : classGroups.entrySet()) {
            List<StudentCourseSelection> classSelections = entry.getValue();
            classSelections.sort((a, b) -> {
                BigDecimal scoreA = a.getTotalScoreWeighted() != null ? a.getTotalScoreWeighted() : BigDecimal.ZERO;
                BigDecimal scoreB = b.getTotalScoreWeighted() != null ? b.getTotalScoreWeighted() : BigDecimal.ZERO;
                return scoreB.compareTo(scoreA);
            });
            for (int i = 0; i < classSelections.size(); i++) {
                classSelections.get(i).setClassRank(i + 1);
            }
        }

        // 批量更新
        this.updateBatchById(selections);
    }

    // ==================== 高级查询 ====================

    @Override
    public List<StudentCourseSelection> querySelections(SelectionQueryDTO queryDTO) {
        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getStudentId() != null) {
            wrapper.eq(StudentCourseSelection::getStudentId, queryDTO.getStudentId());
        }
        if (StringUtils.hasText(queryDTO.getGrade())) {
            wrapper.eq(StudentCourseSelection::getGrade, queryDTO.getGrade());
        }
        if (StringUtils.hasText(queryDTO.getClassName())) {
            wrapper.eq(StudentCourseSelection::getClassName, queryDTO.getClassName());
        }
        if (StringUtils.hasText(queryDTO.getAcademicYear())) {
            wrapper.eq(StudentCourseSelection::getAcademicYear, queryDTO.getAcademicYear());
        }
        if (StringUtils.hasText(queryDTO.getSemester())) {
            wrapper.eq(StudentCourseSelection::getSemester, queryDTO.getSemester());
        }
        if (queryDTO.getIsConfirmed() != null) {
            wrapper.eq(StudentCourseSelection::getIsConfirmed, queryDTO.getIsConfirmed());
        }

        // 隐私控制：非本人只能看到公开的记录
        if (queryDTO.getCurrentUserId() != null && !queryDTO.getCurrentUserId().equals(queryDTO.getStudentId())) {
            wrapper.eq(StudentCourseSelection::getIsPublic, true);
        }

        wrapper.orderByDesc(StudentCourseSelection::getCreateTime);

        // 分页
        Page<StudentCourseSelection> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<StudentCourseSelection> resultPage = this.page(page, wrapper);

        return resultPage.getRecords();
    }

    @Override
    public StudentCourseSelection getStudentSelectionDetail(Long studentId) {
        StudentCourseSelection selection = selectionMapper.getLatestSelection(studentId);
        if (selection != null) {
            // 填充组合详细信息
            if (selection.getCombinationId() != null) {
                SubjectCombination combination = combinationMapper.selectById(selection.getCombinationId());
                selection.setCombination(combination);
            }
        }
        return selection;
    }

    @Override
    public List<StudentCourseSelection> getStudentsByCombination(Long combinationId) {
        return selectionMapper.getStudentsByCombination(combinationId);
    }

    // ==================== 数据导出 ====================

    @Override
    public List<Map<String, Object>> exportSelectionData(String grade, String academicYear) {
        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getGrade, grade)
                .eq(StudentCourseSelection::getAcademicYear, academicYear)
                .eq(StudentCourseSelection::getIsConfirmed, true)
                .orderByDesc(StudentCourseSelection::getTotalScoreWeighted);

        List<StudentCourseSelection> selections = this.list(wrapper);

        // 转换为导出格式
        return selections.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("学生姓名", s.getStudentName());
            map.put("班级", s.getClassName());
            map.put("首选科目", s.getFirstSubjectName());
            map.put("再选科目1", s.getSecondSubject1Name());
            map.put("再选科目2", s.getSecondSubject2Name());
            map.put("组合", s.getCombinationName());
            map.put("语文", s.getChineseScore());
            map.put("数学", s.getMathScore());
            map.put("英语", s.getEnglishScore());
            map.put("首选成绩", s.getFirstSubjectScore());
            map.put("再选1成绩", s.getSecondSubject1Score());
            map.put("再选2成绩", s.getSecondSubject2Score());
            map.put("原始总分", s.getTotalScore());
            map.put("赋分总分", s.getTotalScoreWeighted());
            map.put("班级排名", s.getClassRank());
            map.put("年级排名", s.getGradeRank());
            map.put("选课理由", s.getSelectionReason());
            map.put("是否确认", s.getIsConfirmed() ? "是" : "否");
            map.put("是否公开", s.getIsPublic() ? "是" : "否");
            return map;
        }).collect(Collectors.toList());
    }

    // ==================== 选课建议 ====================

    @Override
    public Map<String, Object> getSelectionAdvice(Long studentId) {
        Map<String, Object> advice = new HashMap<>();

        // 获取学生最近的选课记录
        StudentCourseSelection selection = selectionMapper.getLatestSelection(studentId);
        if (selection == null) {
            advice.put("hasSelection", false);
            advice.put("message", "尚未选课，请先进行选课");
            return advice;
        }

        advice.put("hasSelection", true);
        advice.put("currentCombination", selection.getCombinationName());

        // 根据当前组合推荐专业
        List<MajorRequirement> majors = majorRequirementMapper.matchMajorByCombination(
                selection.getFirstSubjectName(),
                selection.getFirstSubjectId(),
                selection.getSecondSubject1Id(),
                selection.getSecondSubject2Id(),
                5
        );
        advice.put("recommendedMajors", majors);

        // 成绩分析
        List<Map<String, Object>> scoreAnalysis = new ArrayList<>();
        if (selection.getChineseScore() != null) {
            Map<String, Object> chinese = new HashMap<>();
            chinese.put("subject", "语文");
            chinese.put("score", selection.getChineseScore());
            chinese.put("level", getLevelByScore(selection.getChineseScore()));
            scoreAnalysis.add(chinese);
        }
        if (selection.getMathScore() != null) {
            Map<String, Object> math = new HashMap<>();
            math.put("subject", "数学");
            math.put("score", selection.getMathScore());
            math.put("level", getLevelByScore(selection.getMathScore()));
            scoreAnalysis.add(math);
        }
        if (selection.getEnglishScore() != null) {
            Map<String, Object> english = new HashMap<>();
            english.put("subject", "英语");
            english.put("score", selection.getEnglishScore());
            english.put("level", getLevelByScore(selection.getEnglishScore()));
            scoreAnalysis.add(english);
        }
        advice.put("scoreAnalysis", scoreAnalysis);

        // 提升建议
        List<String> suggestions = new ArrayList<>();
        if (selection.getMathScore() != null && selection.getMathScore().compareTo(BigDecimal.valueOf(120)) < 0) {
            suggestions.add("数学成绩有待提升，建议加强数学练习");
        }
        if (selection.getEnglishScore() != null && selection.getEnglishScore().compareTo(BigDecimal.valueOf(120)) < 0) {
            suggestions.add("英语成绩有待提升，建议增加词汇量和阅读训练");
        }
        advice.put("suggestions", suggestions);

        return advice;
    }

    // ==================== 冲突检查 ====================

    @Override
    public Map<String, Object> checkSelectionConflict(StudentCourseSelection selection) {
        Map<String, Object> result = new HashMap<>();
        List<String> conflicts = new ArrayList<>();

        // 检查是否重复选课
        LambdaQueryWrapper<StudentCourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentCourseSelection::getStudentId, selection.getStudentId())
                .eq(StudentCourseSelection::getAcademicYear, selection.getAcademicYear())
                .eq(StudentCourseSelection::getSemester, selection.getSemester())
                .ne(StudentCourseSelection::getStatus, 4);
        long existingCount = this.count(wrapper);
        if (existingCount > 0) {
            conflicts.add("该学期已经存在选课记录，不能重复选课");
        }

        // 检查首选科目是否重复
        if (selection.getFirstSubjectId() != null && selection.getSecondSubject1Id() != null) {
            if (selection.getFirstSubjectId().equals(selection.getSecondSubject1Id())) {
                conflicts.add("首选科目与再选科目1不能重复");
            }
        }
        if (selection.getFirstSubjectId() != null && selection.getSecondSubject2Id() != null) {
            if (selection.getFirstSubjectId().equals(selection.getSecondSubject2Id())) {
                conflicts.add("首选科目与再选科目2不能重复");
            }
        }
        if (selection.getSecondSubject1Id() != null && selection.getSecondSubject2Id() != null) {
            if (selection.getSecondSubject1Id().equals(selection.getSecondSubject2Id())) {
                conflicts.add("再选科目1与再选科目2不能重复");
            }
        }

        result.put("hasConflict", !conflicts.isEmpty());
        result.put("conflicts", conflicts);

        return result;
    }

    // ==================== 批量导入 ====================

    @Override
    @Transactional
    public Map<String, Object> batchImportSelections(List<StudentCourseSelection> selections) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < selections.size(); i++) {
            StudentCourseSelection selection = selections.get(i);
            try {
                // 检查冲突
                Map<String, Object> conflictCheck = checkSelectionConflict(selection);
                if ((Boolean) conflictCheck.get("hasConflict")) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行: " + String.join(", ", (List<String>) conflictCheck.get("conflicts")));
                    continue;
                }

                // 保存选课
                selection.setCreateTime(LocalDateTime.now());
                selection.setUpdateTime(LocalDateTime.now());
                selection.setStatus(1);
                selection.setIsConfirmed(false);
                selection.setIsPublic(false);

                calculateTotalScore(selection);
                calculateWeightedScores(selection);

                boolean success = this.save(selection);
                if (success) {
                    successCount++;
                    recordHistory(selection, "1", null, "批量导入");
                } else {
                    failCount++;
                    errors.add("第" + (i + 1) + "行: 保存失败");
                }
            } catch (Exception e) {
                failCount++;
                errors.add("第" + (i + 1) + "行: " + e.getMessage());
            }
        }

        result.put("total", selections.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算总分
     */
    private void calculateTotalScore(StudentCourseSelection selection) {
        BigDecimal total = BigDecimal.ZERO;
        if (selection.getChineseScore() != null) total = total.add(selection.getChineseScore());
        if (selection.getMathScore() != null) total = total.add(selection.getMathScore());
        if (selection.getEnglishScore() != null) total = total.add(selection.getEnglishScore());
        if (selection.getFirstSubjectScore() != null) total = total.add(selection.getFirstSubjectScore());
        if (selection.getSecondSubject1Score() != null) total = total.add(selection.getSecondSubject1Score());
        if (selection.getSecondSubject2Score() != null) total = total.add(selection.getSecondSubject2Score());
        selection.setTotalScore(total);

        // 如果有赋分成绩，计算赋分总分
        BigDecimal weightedTotal = BigDecimal.ZERO;
        if (selection.getChineseScore() != null) weightedTotal = weightedTotal.add(selection.getChineseScore());
        if (selection.getMathScore() != null) weightedTotal = weightedTotal.add(selection.getMathScore());
        if (selection.getEnglishScore() != null) weightedTotal = weightedTotal.add(selection.getEnglishScore());
        if (selection.getFirstSubjectScore() != null) {
            BigDecimal weightedFirst = calculateWeightedScore(
                    selection.getFirstSubjectId(),
                    selection.getFirstSubjectScore(),
                    selection.getAcademicYear()
            );
            weightedTotal = weightedTotal.add(weightedFirst != null ? weightedFirst : selection.getFirstSubjectScore());
        }
        if (selection.getSecondSubject1Score() != null) {
            BigDecimal weightedSecond1 = calculateWeightedScore(
                    selection.getSecondSubject1Id(),
                    selection.getSecondSubject1Score(),
                    selection.getAcademicYear()
            );
            weightedTotal = weightedTotal.add(weightedSecond1 != null ? weightedSecond1 : selection.getSecondSubject1Score());
        }
        if (selection.getSecondSubject2Score() != null) {
            BigDecimal weightedSecond2 = calculateWeightedScore(
                    selection.getSecondSubject2Id(),
                    selection.getSecondSubject2Score(),
                    selection.getAcademicYear()
            );
            weightedTotal = weightedTotal.add(weightedSecond2 != null ? weightedSecond2 : selection.getSecondSubject2Score());
        }
        selection.setTotalScoreWeighted(weightedTotal);
    }

    /**
     * 记录历史
     */
    private void recordHistory(StudentCourseSelection selection, String changeType,
                               StudentCourseSelection newSelection, String reason) {
        CourseSelectionHistory history = new CourseSelectionHistory();
        history.setSelectionId(selection.getId());
        history.setStudentId(selection.getStudentId());
        history.setStudentName(selection.getStudentName());
        history.setChangeType(changeType);
        history.setChangeReason(reason);
        history.setChangeTime(LocalDateTime.now());

        if ("2".equals(changeType) && newSelection != null) {
            // 修改
            if (!Objects.equals(selection.getFirstSubjectName(), newSelection.getFirstSubjectName())) {
                history.setOldFirstSubject(selection.getFirstSubjectName());
                history.setNewFirstSubject(newSelection.getFirstSubjectName());
            }
            if (!Objects.equals(selection.getSecondSubject1Name(), newSelection.getSecondSubject1Name())) {
                history.setOldSecondSubject1(selection.getSecondSubject1Name());
                history.setNewSecondSubject1(newSelection.getSecondSubject1Name());
            }
            if (!Objects.equals(selection.getSecondSubject2Name(), newSelection.getSecondSubject2Name())) {
                history.setOldSecondSubject2(selection.getSecondSubject2Name());
                history.setNewSecondSubject2(newSelection.getSecondSubject2Name());
            }
        } else if ("3".equals(changeType)) {
            // 退选
            history.setOldFirstSubject(selection.getFirstSubjectName());
            history.setOldSecondSubject1(selection.getSecondSubject1Name());
            history.setOldSecondSubject2(selection.getSecondSubject2Name());
        } else {
            // 新增或确认
            history.setNewFirstSubject(selection.getFirstSubjectName());
            history.setNewSecondSubject1(selection.getSecondSubject1Name());
            history.setNewSecondSubject2(selection.getSecondSubject2Name());
        }

        historyMapper.insert(history);
    }

    /**
     * 根据分数获取等级
     */
    private String getLevelByScore(BigDecimal score) {
        if (score == null) return "E";
        if (score.compareTo(BigDecimal.valueOf(90)) >= 0) return "A";
        if (score.compareTo(BigDecimal.valueOf(75)) >= 0) return "B";
        if (score.compareTo(BigDecimal.valueOf(60)) >= 0) return "C";
        return "D";
    }
}