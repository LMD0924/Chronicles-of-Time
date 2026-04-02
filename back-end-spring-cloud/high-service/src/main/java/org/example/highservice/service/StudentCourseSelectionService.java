/*
 * @Author: 总会落叶
 * @Date: 2026/4/1
 * @Description: 学生选课记录Service接口（完整版）
 */
package org.example.highservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.highservice.dto.SelectionQueryDTO;
import org.example.highservice.entity.StudentCourseSelection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface StudentCourseSelectionService extends IService<StudentCourseSelection> {

    /**
     * 学生选课
     * @param selection 选课信息
     * @return 是否成功
     */
    boolean selectCourse(StudentCourseSelection selection);

    /**
     * 确认选课
     * @param id 选课记录ID
     * @return 是否成功
     */
    boolean confirmSelection(Long id);

    /**
     * 修改选课
     * @param selection 新的选课信息
     * @return 是否成功
     */
    boolean modifySelection(StudentCourseSelection selection);

    /**
     * 退选
     * @param id 选课记录ID
     * @param reason 退选原因
     * @return 是否成功
     */
    boolean cancelSelection(Long id, String reason);

    /**
     * 获取学生的选课记录
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    List<StudentCourseSelection> getStudentSelections(Long studentId);

    /**
     * 获取年级选课统计
     * @param grade 年级
     * @param academicYear 学年
     * @return 统计数据
     */
    Map<String, Object> getGradeStatistics(String grade, String academicYear);

    /**
     * 获取班级选课统计
     * @param grade 年级
     * @param className 班级名称
     * @return 统计数据
     */
    Map<String, Object> getClassStatistics(String grade, String className);

    /**
     * 更新公开状态
     * @param id 选课记录ID
     * @param isPublic 是否公开
     * @return 是否成功
     */
    boolean updatePublicStatus(Long id, Boolean isPublic);

    /**
     * 批量更新公开状态
     * @param ids 选课记录ID列表
     * @param isPublic 是否公开
     * @return 是否成功
     */
    boolean batchUpdatePublicStatus(List<Long> ids, Boolean isPublic);

    /**
     * 获取公开的选课记录
     * @return 公开的选课记录列表
     */
    List<StudentCourseSelection> getPublicSelections();

    /**
     * 根据专业推荐选课组合
     * @param majorName 专业名称
     * @return 推荐的组合列表
     */
    List<Map<String, Object>> recommendByMajor(String majorName);

    /**
     * 计算赋分成绩
     * @param subjectId 科目ID
     * @param rawScore 原始分
     * @param academicYear 学年
     * @return 赋分后的成绩
     */
    BigDecimal calculateWeightedScore(Long subjectId, BigDecimal rawScore, String academicYear);

    /**
     * 生成排名
     * @param grade 年级
     * @param academicYear 学年
     * @param semester 学期
     */
    void generateRank(String grade, String academicYear, String semester);

    /**
     * 分页查询选课记录（含公开/隐私控制）
     * @param queryDTO 查询条件
     * @return 选课记录列表
     */
    List<StudentCourseSelection> querySelections(SelectionQueryDTO queryDTO);

    /**
     * 获取热门组合排名
     * @return 热门组合排名列表
     */
    List<Map<String, Object>> getHotCombinations();

    /**
     * 获取年级排名前N的学生
     * @param grade 年级
     * @param limit 数量限制
     * @return 学生列表
     */
    List<StudentCourseSelection> getTopStudents(String grade, int limit);

    /**
     * 获取班级成绩统计
     * @param grade 年级
     * @return 班级成绩统计列表
     */
    List<Map<String, Object>> getClassScoreStatistics(String grade);

    /**
     * 获取各组合的平均分排名
     * @param grade 年级
     * @return 组合排名列表
     */
    List<Map<String, Object>> getCombinationScoreRanking(String grade);

    /**
     * 获取选课趋势
     * @return 选课趋势数据
     */
    List<Map<String, Object>> getSelectionTrend();

    /**
     * 获取学生的详细选课信息（包含科目详情）
     * @param studentId 学生ID
     * @return 详细选课信息
     */
    StudentCourseSelection getStudentSelectionDetail(Long studentId);

    /**
     * 获取某组合的所有学生
     * @param combinationId 组合ID
     * @return 学生列表
     */
    List<StudentCourseSelection> getStudentsByCombination(Long combinationId);

    /**
     * 导出选课数据
     * @param grade 年级
     * @param academicYear 学年
     * @return 导出数据列表
     */
    List<Map<String, Object>> exportSelectionData(String grade, String academicYear);

    /**
     * 获取学生选课建议
     * @param studentId 学生ID
     * @return 选课建议
     */
    Map<String, Object> getSelectionAdvice(Long studentId);

    /**
     * 检查选课冲突
     * @param selection 选课信息
     * @return 冲突检查结果
     */
    Map<String, Object> checkSelectionConflict(StudentCourseSelection selection);

    /**
     * 批量导入选课数据
     * @param selections 选课数据列表
     * @return 导入结果
     */
    Map<String, Object> batchImportSelections(List<StudentCourseSelection> selections);
}