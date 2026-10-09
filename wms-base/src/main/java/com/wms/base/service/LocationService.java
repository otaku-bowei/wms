package com.wms.base.service;

import com.wms.base.domain.dto.LocationBatchDTO;
import com.wms.base.domain.dto.LocationCreateDTO;
import com.wms.base.domain.entity.BaseLocation;
import com.wms.base.domain.query.LocationQuery;
import com.wms.base.domain.vo.LocationLayoutVO;
import com.wms.common.core.result.PageResult;

/**
 * 库位服务接口
 *
 * @author WMS
 */
public interface LocationService {

    /**
     * 新增库位
     *
     * @param dto 参数
     * @return 库位 ID
     */
    Long createLocation(LocationCreateDTO dto);

    /**
     * 批量创建库位
     *
     * @param dto 参数
     * @return 实际创建数量
     */
    int batchCreate(LocationBatchDTO dto);

    /**
     * 修改库位属性
     *
     * @param id        库位 ID
     * @param type      库位类型
     * @param maxWeight 最大承重
     * @param maxVolume 最大体积
     */
    void updateLocation(Long id, String type, java.math.BigDecimal maxWeight, java.math.BigDecimal maxVolume);

    /**
     * 变更库位状态
     *
     * @param id     库位 ID
     * @param status 状态：FREE / LOCKED / MAINTENANCE
     * @param reason 原因
     */
    void changeStatus(Long id, String status, String reason);

    /**
     * 查询库位详情
     *
     * @param id 库位 ID
     * @return 库位
     */
    BaseLocation getById(Long id);

    /**
     * 分页查询库位
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<BaseLocation> page(LocationQuery query);

    /**
     * 查询库位 2D 平面图
     *
     * @param warehouseId 仓库 ID
     * @param zoneId      区域 ID
     * @param shelf       货架号（可空）
     * @return 平面图数据
     */
    LocationLayoutVO layout(Long warehouseId, Long zoneId, Integer shelf);
}
