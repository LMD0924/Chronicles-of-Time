package org.example.generalservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.generalservice.dto.growth.GrowthDTO;
import org.example.generalservice.dto.growth.GrowthQueryDTO;
import org.example.generalservice.dto.growth.GrowthStatsDTO;
import org.example.generalservice.entity.Growth;
import org.example.generalservice.vo.GrowthVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 高中成长记录 Service 接口
 */
public interface GrowthService extends IService<Growth> {

    /**
     * 新增成长记录
     */
    boolean addRecord(GrowthDTO dto);

    /**
     * 更新成长记录
     */
    boolean updateRecord(GrowthDTO dto);

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
    GrowthVO getRecordById(Long id, Long userId);

    /**
     * 分页查询
     */
    List<GrowthVO> queryRecords(GrowthQueryDTO queryDTO);

    /**
     * 获取统计数据
     */
    GrowthStatsDTO getStats(Long userId);

    /**
     * 获取成长趋势
     */
    List<Map<String, Object>> getGrowthTrend(Long userId);

    /**
     * 获取里程碑记录
     */
    List<GrowthVO> getMilestones(Long userId);

    /**
     * 按阶段统计
     */
    List<Map<String, Object>> countByStage(Long userId);

    // 在calculateWeightedScore方法中，使用注入的gradingScaleMapper
    BigDecimal calculateWeightedScore(Long subjectId, BigDecimal rawScore, String academicYear);
}