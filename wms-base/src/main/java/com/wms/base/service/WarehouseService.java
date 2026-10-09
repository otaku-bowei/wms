package com.wms.base.service;

import com.wms.base.domain.dto.WarehouseDTO;
import com.wms.base.domain.dto.ZoneDTO;
import com.wms.base.domain.entity.BaseWarehouse;
import com.wms.base.domain.entity.BaseZone;
import com.wms.base.domain.query.WarehouseQuery;
import com.wms.common.core.result.PageResult;

import java.util.List;

/**
 * 仓库与区域服务接口
 *
 * @author WMS
 */
public interface WarehouseService {

    /**
     * 新增仓库
     *
     * @param dto 参数
     * @return 仓库 ID
     */
    Long createWarehouse(WarehouseDTO dto);

    /**
     * 修改仓库
     *
     * @param id  仓库 ID
     * @param dto 参数
     */
    void updateWarehouse(Long id, WarehouseDTO dto);

    /**
     * 启用 / 停用仓库
     *
     * @param id     仓库 ID
     * @param status 1 启用 0 停用
     */
    void changeStatus(Long id, Integer status);

    /**
     * 查询仓库详情
     *
     * @param id 仓库 ID
     * @return 仓库
     */
    BaseWarehouse getById(Long id);

    /**
     * 分页查询仓库
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<BaseWarehouse> page(WarehouseQuery query);

    /**
     * 查询启用状态的仓库（下拉选项）
     *
     * @return 仓库列表
     */
    List<BaseWarehouse> listEnabled();

    /**
     * 新增区域
     *
     * @param warehouseId 仓库 ID
     * @param dto         参数
     * @return 区域 ID
     */
    Long createZone(Long warehouseId, ZoneDTO dto);

    /**
     * 修改区域
     *
     * @param zoneId 区域 ID
     * @param dto    参数
     */
    void updateZone(Long zoneId, ZoneDTO dto);

    /**
     * 查询仓库下的区域列表
     *
     * @param warehouseId 仓库 ID
     * @return 区域列表
     */
    List<BaseZone> listZones(Long warehouseId);
}
