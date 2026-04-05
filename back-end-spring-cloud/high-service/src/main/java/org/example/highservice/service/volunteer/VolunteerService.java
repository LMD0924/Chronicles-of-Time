package org.example.highservice.service.volunteer;

import org.example.highservice.entity.volunteer.*;
import java.util.List;
import java.util.Map;

/*
 * @Author:总会落叶
 * @Date:2026/4/4
 * @Description: 志愿填报服务接口
 */
public interface VolunteerService {

    // 志愿方案管理
    boolean saveVolunteerPlan(UserVolunteer userVolunteer);
    boolean updateVolunteerPlan(UserVolunteer userVolunteer);
    boolean deleteVolunteerPlan(Integer id);
    UserVolunteer getVolunteerPlanById(Integer id);
    List<UserVolunteer> getUserVolunteerPlans(Long userId, Integer year);

    // 志愿详情管理
    boolean addVolunteerDetail(VolunteerDetail volunteerDetail);
    boolean updateVolunteerDetail(VolunteerDetail volunteerDetail);
    boolean deleteVolunteerDetail(Integer id);
    List<VolunteerDetail> getVolunteerDetails(Integer volunteerId);
    boolean batchAddVolunteerDetails(List<VolunteerDetail> details);

    // 智能推荐
    List<Map<String, Object>> recommendUniversities(Long userId, Integer year, String province,
                                                    Integer score, Integer rank, List<String> subjects);
    List<Map<String, Object>> recommendByMajor(Long userId, String majorCode, Integer score, String province);

    // 匹配度分析
    Map<String, Object> checkSubjectMatching(Integer volunteerDetailId, List<String> selectedSubjects);
    List<Map<String, Object>> getVolunteerMatchingReport(Integer volunteerId, List<String> selectedSubjects);

    // 模拟录取
    AdmissionSimulation simulateAdmission(Integer volunteerDetailId);
    List<Map<String, Object>> batchSimulateAdmission(Integer volunteerId);
    Map<String, Object> getAdmissionAnalysis(Integer volunteerId);

    // 统计分析
    List<Map<String, Object>> getVolunteerStatistics(Integer userId);
    Map<String, Object> getApplicationChance(Integer userId, Integer universityId, Integer majorId);

    // 数据查询
    List<University> searchUniversities(String keyword, String province, String level);
    List<Major> searchMajors(String keyword, String category, Integer universityId);
    List<AdmissionPlan> getAdmissionHistory(Integer majorId, Integer universityId, Integer year);
    
    // 获取筛选条件
    List<String> getAllProvinces();
    List<String> getAllUniversityLevels();
    List<String> getAllCategories();
}