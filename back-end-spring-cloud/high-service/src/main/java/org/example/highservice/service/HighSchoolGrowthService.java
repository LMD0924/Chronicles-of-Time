package org.example.highservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.highservice.entity.HighSchoolGrowth;
import org.example.highservice.dto.HighSchoolGrowthDTO;
import org.example.highservice.dto.HighSchoolGrowthQueryDTO;
import org.example.highservice.dto.HighSchoolGrowthStatsDTO;
import org.example.highservice.vo.HighSchoolGrowthVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 高中成长记录 Service 接口
 */
public interface HighSchoolGrowthService extends IService<HighSchoolGrowth> {

    /**
     * 新增成长记录
     */
    boolean addRecord(HighSchoolGrowthDTO dto);

    /**
     * 更新成长记录
     */
    boolean updateRecord(HighSchoolGrowthDTO dto);

    /**
     * 删除成长记录
     */
    boolean deleteRecord(Long id, Long userId);

    /**
     * 批量删除
     */
    boolean batchDelete(List<Long> ids, Long userId);

    /**
     * 根据ID查询
     */
    HighSchoolGrowthVO getRecordById(Long id, Long userId);

    /**
     * 分页查询
     */
    List<HighSchoolGrowthVO> queryRecords(HighSchoolGrowthQueryDTO queryDTO);

    /**
     * 获取统计数据
     */
    HighSchoolGrowthStatsDTO getStats(Long userId);

    /**
     * 获取成长趋势
     */
    List<Map<String, Object>> getGrowthTrend(Long userId);

    /**
     * 获取里程碑记录
     */
    List<HighSchoolGrowthVO> getMilestones(Long userId);

    /**
     * 按年级统计
     */
    List<Map<String, Object>> countByGrade(Long userId);

    // 在calculateWeightedScore方法中，使用注入的gradingScaleMapper
    BigDecimal calculateWeightedScore(Long subjectId, BigDecimal rawScore, String academicYear);
}