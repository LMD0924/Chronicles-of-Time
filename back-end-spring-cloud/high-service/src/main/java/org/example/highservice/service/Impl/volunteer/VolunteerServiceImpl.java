package org.example.highservice.service.Impl.volunteer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.highservice.entity.volunteer.*;
import org.example.highservice.mapper.volunteer.*;
import org.example.highservice.service.volunteer.VolunteerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VolunteerServiceImpl extends ServiceImpl<UserVolunteerMapper, UserVolunteer>
        implements VolunteerService {

    private final UserVolunteerMapper userVolunteerMapper;
    private final VolunteerDetailMapper volunteerDetailMapper;
    private final AdmissionSimulationMapper admissionSimulationMapper;
    private final UniversityMapper universityMapper;
    private final MajorMapper majorMapper;
    private final AdmissionPlanMapper admissionPlanMapper;

    // ==================== 志愿方案管理 ====================

    @Override
    @Transactional
    public boolean saveVolunteerPlan(UserVolunteer userVolunteer) {
        log.info("保存志愿方案: userId={}, year={}", userVolunteer.getUserId(), userVolunteer.getYear());
        userVolunteer.setSubmitTime(LocalDateTime.now());
        userVolunteer.setIsFinal(false);
        return this.save(userVolunteer);
    }

    @Override
    public boolean updateVolunteerPlan(UserVolunteer userVolunteer) {
        log.info("更新志愿方案: id={}", userVolunteer.getId());
        return this.updateById(userVolunteer);
    }

    @Override
    @Transactional
    public boolean deleteVolunteerPlan(Integer id) {
        log.info("删除志愿方案: id={}", id);
        LambdaQueryWrapper<VolunteerDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VolunteerDetail::getVolunteerId, id);
        volunteerDetailMapper.delete(wrapper);
        return this.removeById(id);
    }

    @Override
    public UserVolunteer getVolunteerPlanById(Integer id) {
        return this.getById(id);
    }

    @Override
    public List<UserVolunteer> getUserVolunteerPlans(Long userId, Integer year) {
        LambdaQueryWrapper<UserVolunteer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVolunteer::getUserId, userId);
        if (year != null) {
            wrapper.eq(UserVolunteer::getYear, year);
        }
        wrapper.orderByDesc(UserVolunteer::getSubmitTime);
        return this.list(wrapper);
    }

    // ==================== 志愿详情管理 ====================

    @Override
    public boolean addVolunteerDetail(VolunteerDetail volunteerDetail) {
        log.info("添加志愿详情: volunteerId={}, priority={}",
                volunteerDetail.getVolunteerId(), volunteerDetail.getPriority());
        return volunteerDetailMapper.insert(volunteerDetail) > 0;
    }

    @Override
    public boolean updateVolunteerDetail(VolunteerDetail volunteerDetail) {
        log.info("更新志愿详情: id={}", volunteerDetail.getId());
        return volunteerDetailMapper.updateById(volunteerDetail) > 0;
    }

    @Override
    public boolean deleteVolunteerDetail(Integer id) {
        log.info("删除志愿详情: id={}", id);
        return volunteerDetailMapper.deleteById(id) > 0;
    }

    @Override
    public List<VolunteerDetail> getVolunteerDetails(Integer volunteerId) {
        LambdaQueryWrapper<VolunteerDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VolunteerDetail::getVolunteerId, volunteerId)
                .orderByAsc(VolunteerDetail::getPriority);
        return volunteerDetailMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean batchAddVolunteerDetails(List<VolunteerDetail> details) {
        log.info("批量添加志愿详情: size={}", details.size());
        for (VolunteerDetail detail : details) {
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
        }
        return volunteerDetailMapper.batchInsert(details) > 0;
    }

    // ==================== 智能推荐 ====================

    @Override
    public List<Map<String, Object>> recommendUniversities(Long userId, Integer year, String province,
                                                           Integer score, Integer rank, List<String> subjects) {
        log.info("智能推荐大学: userId={}, year={}, province={}, score={}, rank={}",
                userId, year, province, score, rank);

        List<Map<String, Object>> recommendations = new ArrayList<>();

        // 先检查数据是否存在
        LambdaQueryWrapper<AdmissionPlan> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AdmissionPlan::getYear, year)
                .eq(AdmissionPlan::getProvince, province);
        Long totalCount = admissionPlanMapper.selectCount(checkWrapper);
        log.info("year={}, province={} 总记录数: {}", year, province, totalCount);

        if (totalCount == 0) {
            log.warn("没有找到招生计划数据");
            return recommendations;
        }

        // 查询分数范围内的数据 - 使用更宽松的范围
        Integer minScore = Math.max(0, score - 50);  // 降低50分
        Integer maxScore = score + 30;                // 提高30分
        log.info("查询分数范围: {} - {}", minScore, maxScore);

        LambdaQueryWrapper<AdmissionPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdmissionPlan::getYear, year)
                .eq(AdmissionPlan::getProvince, province)
                .ge(AdmissionPlan::getMinScore, minScore)
                .le(AdmissionPlan::getMinScore, maxScore)
                .orderByDesc(AdmissionPlan::getMinScore);

        List<AdmissionPlan> plans = admissionPlanMapper.selectList(wrapper);
        log.info("查询到的招生计划数量: {}", plans.size());

        // 如果没有数据，放宽条件
        if (plans.isEmpty()) {
            log.info("未查询到数据，尝试放宽分数范围...");
            minScore = Math.max(0, score - 100);
            maxScore = score + 50;

            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdmissionPlan::getYear, year)
                    .eq(AdmissionPlan::getProvince, province)
                    .ge(AdmissionPlan::getMinScore, minScore)
                    .le(AdmissionPlan::getMinScore, maxScore)
                    .orderByDesc(AdmissionPlan::getMinScore);

            plans = admissionPlanMapper.selectList(wrapper);
            log.info("放宽后查询到的招生计划数量: {}", plans.size());
        }

        // 如果还是没有数据，去掉省份限制
        if (plans.isEmpty()) {
            log.info("仍未查询到数据，尝试去掉省份限制...");
            minScore = Math.max(0, score - 100);
            maxScore = score + 50;

            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AdmissionPlan::getYear, year)
                    .ge(AdmissionPlan::getMinScore, minScore)
                    .le(AdmissionPlan::getMinScore, maxScore)
                    .orderByDesc(AdmissionPlan::getMinScore);

            plans = admissionPlanMapper.selectList(wrapper);
            log.info("去掉省份限制后查询到的招生计划数量: {}", plans.size());
        }

        // 去重
        Set<String> processed = new HashSet<>();

        for (AdmissionPlan plan : plans) {
            String pairKey = plan.getUniversityId() + "_" + plan.getMajorId();
            if (processed.contains(pairKey)) {
                continue;
            }
            processed.add(pairKey);

            // 获取大学信息
            University university = universityMapper.selectById(plan.getUniversityId());
            // 获取专业信息
            Major major = majorMapper.selectById(plan.getMajorId());

            if (university == null) {
                log.warn("大学信息不存在: universityId={}", plan.getUniversityId());
                continue;
            }
            if (major == null) {
                log.warn("专业信息不存在: majorId={}", plan.getMajorId());
                continue;
            }

            Map<String, Object> rec = new HashMap<>();
            rec.put("universityId", university.getId());
            rec.put("universityName", university.getName());
            rec.put("universityLevel", university.getLevel());
            rec.put("majorId", major.getId());
            rec.put("majorName", major.getName());
            rec.put("minScore", plan.getMinScore());
            rec.put("minRank", plan.getMinRank());
            rec.put("year", plan.getYear());
            rec.put("province", plan.getProvince());

            // 计算录取概率和建议策略
            int scoreDiff = score - plan.getMinScore();
            if (scoreDiff >= 10) {
                rec.put("strategy", "保底");
                rec.put("probability", 0.85);
                rec.put("probabilityLevel", "高");
                rec.put("color", "#10b981");
            } else if (scoreDiff >= -5) {
                rec.put("strategy", "稳妥");
                rec.put("probability", 0.65);
                rec.put("probabilityLevel", "中");
                rec.put("color", "#3b82f6");
            } else if (scoreDiff >= -20) {
                rec.put("strategy", "冲刺");
                rec.put("probability", 0.35);
                rec.put("probabilityLevel", "低");
                rec.put("color", "#f59e0b");
            } else {
                rec.put("strategy", "梦想");
                rec.put("probability", 0.15);
                rec.put("probabilityLevel", "极低");
                rec.put("color", "#ef4444");
            }

            rec.put("scoreDiff", scoreDiff);
            rec.put("suggestion", getSuggestionByStrategy((String) rec.get("strategy")));

            recommendations.add(rec);

            if (recommendations.size() >= 30) break;
        }

        log.info("最终推荐了 {} 条志愿", recommendations.size());
        return recommendations;
    }

    private String getSuggestionByStrategy(String strategy) {
        switch (strategy) {
            case "保底":
                return "录取概率较高，可以作为保底志愿";
            case "稳妥":
                return "录取概率中等，建议作为稳妥志愿";
            case "冲刺":
                return "录取概率较低，可以尝试冲刺";
            case "梦想":
                return "录取概率很低，可以作为梦想志愿";
            default:
                return "请结合自身情况慎重考虑";
        }
    }
    @Override
    public List<Map<String, Object>> recommendByMajor(Long userId, String majorCode,
                                                      Integer score, String province) {
        log.info("按专业推荐: userId={}, majorCode={}, score={}, province={}",
                userId, majorCode, score, province);

        List<Map<String, Object>> recommendations = new ArrayList<>();

        // 1. 查询专业信息
        LambdaQueryWrapper<Major> majorWrapper = new LambdaQueryWrapper<>();
        majorWrapper.eq(Major::getCode, majorCode);
        Major major = majorMapper.selectOne(majorWrapper);
        if (major == null) {
            return recommendations;
        }

        // 2. 查询招生计划（现在可以直接用 university_id 了）
        LambdaQueryWrapper<AdmissionPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(AdmissionPlan::getMajorId, major.getId())
                .eq(AdmissionPlan::getProvince, province)
                .orderByDesc(AdmissionPlan::getMinScore);

        List<AdmissionPlan> plans = admissionPlanMapper.selectList(planWrapper);

        for (AdmissionPlan plan : plans) {
            // 直接通过 university_id 查询大学信息
            University university = universityMapper.selectById(plan.getUniversityId());
            if (university == null) continue;

            Map<String, Object> rec = new HashMap<>();
            rec.put("universityId", university.getId());
            rec.put("universityName", university.getName());
            rec.put("universityLevel", university.getLevel());
            rec.put("majorId", major.getId());
            rec.put("majorName", major.getName());
            rec.put("minScore", plan.getMinScore());
            rec.put("minRank", plan.getMinRank());

            int scoreDiff = score - plan.getMinScore();
            if (scoreDiff >= 10) {
                rec.put("strategy", "保底");
                rec.put("probability", 0.85);
            } else if (scoreDiff >= -5) {
                rec.put("strategy", "稳妥");
                rec.put("probability", 0.65);
            } else if (scoreDiff >= -20) {
                rec.put("strategy", "冲刺");
                rec.put("probability", 0.35);
            } else {
                rec.put("strategy", "梦想");
                rec.put("probability", 0.15);
            }

            recommendations.add(rec);
            if (recommendations.size() >= 20) break;
        }

        return recommendations;
    }

    // ==================== 匹配度分析 ====================

    @Override
    public Map<String, Object> checkSubjectMatching(Integer volunteerDetailId, List<String> selectedSubjects) {
        log.info("检查选科匹配: volunteerDetailId={}, subjects={}", volunteerDetailId, selectedSubjects);

        VolunteerDetail detail = volunteerDetailMapper.selectById(volunteerDetailId);
        if (detail == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("isMatch", false);
            result.put("matchScore", 0);
            result.put("details", "志愿详情不存在");
            return result;
        }

        // 获取专业的选科要求（这里需要关联 major_requirement 表）
        // 简化逻辑：假设选科包含物理就算匹配
        boolean isMatch = selectedSubjects != null && selectedSubjects.contains("物理");
        int matchScore = isMatch ? 85 : 30;

        Map<String, Object> result = new HashMap<>();
        result.put("isMatch", isMatch);
        result.put("matchScore", matchScore);
        result.put("details", isMatch ? "选科符合要求" : "选科不符合要求，建议更换专业");
        result.put("requiredSubjects", "物理");
        result.put("selectedSubjects", selectedSubjects);

        return result;
    }

    @Override
    public List<Map<String, Object>> getVolunteerMatchingReport(Integer volunteerId, List<String> selectedSubjects) {
        log.info("获取志愿匹配报告: volunteerId={}", volunteerId);

        List<VolunteerDetail> details = getVolunteerDetails(volunteerId);
        List<Map<String, Object>> report = new ArrayList<>();

        for (VolunteerDetail detail : details) {
            University university = universityMapper.selectById(detail.getUniversityId());
            Major major = majorMapper.selectById(detail.getMajorId());

            Map<String, Object> item = new HashMap<>();
            item.put("priority", detail.getPriority());
            item.put("universityName", university != null ? university.getName() : "未知");
            item.put("majorName", major != null ? major.getName() : "未知");
            item.put("matchingCheck", detail.getMatchingCheck());
            item.put("matchingScore", detail.getMatchingScore());

            // 简单匹配度计算
            if (selectedSubjects != null && selectedSubjects.contains("物理")) {
                item.put("suggested", true);
                item.put("suggestion", "选科匹配，可以填报");
            } else {
                item.put("suggested", false);
                item.put("suggestion", "选科不匹配，建议更换");
            }

            report.add(item);
        }

        return report;
    }

    // ==================== 模拟录取 ====================

    @Override
    @Transactional
    public AdmissionSimulation simulateAdmission(Integer volunteerDetailId) {
        log.info("模拟录取: volunteerDetailId={}", volunteerDetailId);

        VolunteerDetail detail = volunteerDetailMapper.selectById(volunteerDetailId);
        if (detail == null) {
            return null;
        }

        UserVolunteer volunteer = this.getById(detail.getVolunteerId());
        if (volunteer == null) {
            return null;
        }

        AdmissionPlan latestPlan = admissionPlanMapper.getLatestAdmission(detail.getMajorId(), detail.getUniversityId());

        AdmissionSimulation simulation = new AdmissionSimulation();
        simulation.setVolunteerDetailId(volunteerDetailId);

        if (latestPlan != null && volunteer.getScore() != null) {
            int scoreDiff = volunteer.getScore() - latestPlan.getMinScore();
            simulation.setScoreDiff(scoreDiff);

            if (scoreDiff >= 0) {
                simulation.setSimulationStatus("admitted");
                simulation.setPredictedProbability(0.85 + (scoreDiff / 100.0));
                simulation.setRejectReason(null);
            } else if (scoreDiff >= -20) {
                simulation.setSimulationStatus("waiting");
                simulation.setPredictedProbability(0.4);
                simulation.setRejectReason("分数略低于录取线，等待补录");
            } else {
                simulation.setSimulationStatus("rejected");
                simulation.setPredictedProbability(0.1);
                simulation.setRejectReason("分数差距较大，建议更换志愿");
            }
        } else {
            simulation.setSimulationStatus("pending");
            simulation.setPredictedProbability(0.5);
            simulation.setRejectReason("暂无历史数据参考");
        }

        simulation.setAdmittedTime(LocalDateTime.now());
        admissionSimulationMapper.insert(simulation);

        return simulation;
    }

    @Override
    public List<Map<String, Object>> batchSimulateAdmission(Integer volunteerId) {
        log.info("批量模拟录取: volunteerId={}", volunteerId);

        List<VolunteerDetail> details = getVolunteerDetails(volunteerId);
        List<Map<String, Object>> results = new ArrayList<>();

        UserVolunteer volunteer = this.getById(volunteerId);

        for (VolunteerDetail detail : details) {
            Map<String, Object> result = new HashMap<>();
            result.put("priority", detail.getPriority());

            University university = universityMapper.selectById(detail.getUniversityId());
            Major major = majorMapper.selectById(detail.getMajorId());
            result.put("universityName", university != null ? university.getName() : "未知");
            result.put("majorName", major != null ? major.getName() : "未知");

            AdmissionPlan latestPlan = admissionPlanMapper.getLatestAdmission(detail.getMajorId(), detail.getUniversityId());

            if (latestPlan != null && volunteer != null && volunteer.getScore() != null) {
                int scoreDiff = volunteer.getScore() - latestPlan.getMinScore();
                result.put("scoreDiff", scoreDiff);
                result.put("targetScore", latestPlan.getMinScore());

                if (scoreDiff >= 0) {
                    result.put("status", "录取");
                    result.put("probability", "高");
                } else if (scoreDiff >= -20) {
                    result.put("status", "待定");
                    result.put("probability", "中");
                } else {
                    result.put("status", "未录取");
                    result.put("probability", "低");
                }
            } else {
                result.put("status", "无数据");
                result.put("probability", "未知");
            }

            results.add(result);
        }

        return results;
    }

    @Override
    public Map<String, Object> getAdmissionAnalysis(Integer volunteerId) {
        log.info("获取录取分析: volunteerId={}", volunteerId);

        List<Map<String, Object>> simulationResults = batchSimulateAdmission(volunteerId);

        long admittedCount = simulationResults.stream()
                .filter(r -> "录取".equals(r.get("status")))
                .count();
        long waitingCount = simulationResults.stream()
                .filter(r -> "待定".equals(r.get("status")))
                .count();

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalVolunteers", simulationResults.size());
        analysis.put("admittedCount", admittedCount);
        analysis.put("waitingCount", waitingCount);
        analysis.put("rejectedCount", simulationResults.size() - admittedCount - waitingCount);
        analysis.put("admissionRate", simulationResults.size() > 0 ?
                (double) admittedCount / simulationResults.size() : 0.0);
        analysis.put("suggestion", getAnalysisSuggestion(admittedCount, simulationResults.size()));

        return analysis;
    }

    private String getAnalysisSuggestion(long admittedCount, int total) {
        if (total == 0) return "请先添加志愿";
        double rate = (double) admittedCount / total;
        if (rate >= 0.7) return "志愿填报较为合理，录取希望较大";
        if (rate >= 0.4) return "志愿填报基本合理，建议增加保底志愿";
        return "志愿填报风险较高，建议增加更多保底志愿";
    }

    // ==================== 统计分析 ====================

    @Override
    public List<Map<String, Object>> getVolunteerStatistics(Integer userId) {
        log.info("获取志愿统计: userId={}", userId);

        List<Map<String, Object>> statistics = new ArrayList<>();

        LambdaQueryWrapper<UserVolunteer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVolunteer::getUserId, userId)
                .orderByDesc(UserVolunteer::getYear);

        List<UserVolunteer> plans = this.list(wrapper);

        // 按年份分组统计
        Map<Integer, List<UserVolunteer>> yearGroup = plans.stream()
                .collect(Collectors.groupingBy(UserVolunteer::getYear));

        for (Map.Entry<Integer, List<UserVolunteer>> entry : yearGroup.entrySet()) {
            Map<String, Object> yearStat = new HashMap<>();
            yearStat.put("year", entry.getKey());
            yearStat.put("count", entry.getValue().size());
            yearStat.put("maxScore", entry.getValue().stream()
                    .mapToInt(p -> p.getScore() != null ? p.getScore() : 0)
                    .max().orElse(0));
            yearStat.put("minScore", entry.getValue().stream()
                    .mapToInt(p -> p.getScore() != null ? p.getScore() : 0)
                    .min().orElse(0));
            statistics.add(yearStat);
        }

        return statistics;
    }

    @Override
    public Map<String, Object> getApplicationChance(Integer userId, Integer universityId, Integer majorId) {
        log.info("获取录取机会分析: userId={}, universityId={}, majorId={}", userId, universityId, majorId);

        // 获取用户最新的志愿方案
        LambdaQueryWrapper<UserVolunteer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVolunteer::getUserId, userId)
                .orderByDesc(UserVolunteer::getSubmitTime)
                .last("LIMIT 1");
        UserVolunteer latest = this.getOne(wrapper);

        Map<String, Object> chance = new HashMap<>();

        if (latest == null || latest.getScore() == null) {
            chance.put("probability", 0.0);
            chance.put("level", "无数据");
            chance.put("suggestion", "请先填写高考成绩");
            return chance;
        }

        AdmissionPlan latestPlan = admissionPlanMapper.getLatestAdmission(majorId, universityId);

        if (latestPlan == null) {
            chance.put("probability", 0.0);
            chance.put("level", "无数据");
            chance.put("suggestion", "暂无该专业招生数据");
            return chance;
        }

        int scoreDiff = latest.getScore() - latestPlan.getMinScore();

        if (scoreDiff >= 20) {
            chance.put("probability", 0.95);
            chance.put("level", "非常稳妥");
            chance.put("suggestion", "录取希望极大，可作为保底志愿");
        } else if (scoreDiff >= 10) {
            chance.put("probability", 0.85);
            chance.put("level", "稳妥");
            chance.put("suggestion", "录取希望很大，建议填报");
        } else if (scoreDiff >= 0) {
            chance.put("probability", 0.70);
            chance.put("level", "较稳妥");
            chance.put("suggestion", "有一定希望，可以尝试");
        } else if (scoreDiff >= -10) {
            chance.put("probability", 0.45);
            chance.put("level", "冲刺");
            chance.put("suggestion", "分数略低，可作为冲刺志愿");
        } else if (scoreDiff >= -20) {
            chance.put("probability", 0.25);
            chance.put("level", "较难");
            chance.put("suggestion", "分数差距较大，谨慎填报");
        } else {
            chance.put("probability", 0.10);
            chance.put("level", "困难");
            chance.put("suggestion", "分数差距过大，建议更换志愿");
        }

        chance.put("scoreDiff", scoreDiff);
        chance.put("targetScore", latestPlan.getMinScore());
        chance.put("yourScore", latest.getScore());

        return chance;
    }

    // ==================== 数据查询 ====================

    @Override
    public List<University> searchUniversities(String keyword, String province, String level) {
        log.info("搜索大学: keyword={}, province={}, level={}", keyword, province, level);
        return universityMapper.searchUniversities(keyword, province, level);
    }

    @Override
    public List<Major> searchMajors(String keyword, String category, Integer universityId) {
        log.info("搜索专业: keyword={}, category={}, universityId={}", keyword, category, universityId);
        return majorMapper.searchMajors(keyword, category, universityId);
    }

    @Override
    public List<AdmissionPlan> getAdmissionHistory(Integer majorId, Integer universityId, Integer year) {
        log.info("获取招生历史: majorId={}, universityId={}, year={}", majorId, universityId, year);
        return admissionPlanMapper.getAdmissionHistory(majorId, universityId, year);
    }
    
    @Override
    public List<String> getAllProvinces() {
        log.info("获取所有省份列表");
        return universityMapper.selectList(new LambdaQueryWrapper<University>()
                .select(University::getProvince)
                .groupBy(University::getProvince))
                .stream()
                .map(University::getProvince)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getAllUniversityLevels() {
        log.info("获取所有大学层次列表");
        return universityMapper.selectList(new LambdaQueryWrapper<University>()
                .select(University::getLevel)
                .groupBy(University::getLevel))
                .stream()
                .map(University::getLevel)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getAllCategories() {
        log.info("获取所有学科门类列表");
        return majorMapper.selectList(new LambdaQueryWrapper<Major>()
                .select(Major::getCategory)
                .groupBy(Major::getCategory))
                .stream()
                .map(Major::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}