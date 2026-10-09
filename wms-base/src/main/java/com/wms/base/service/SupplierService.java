package com.wms.base.service;

import com.wms.base.domain.dto.SupplierDTO;
import com.wms.base.domain.entity.BaseSupplier;
import com.wms.base.domain.query.SupplierQuery;
import com.wms.common.core.result.PageResult;

/**
 * 供应商服务接口
 *
 * @author WMS
 */
public interface SupplierService {

    /**
     * 新增供应商
     *
     * @param dto 参数
     * @return 供应商 ID
     */
    Long createSupplier(SupplierDTO dto);

    /**
     * 修改供应商
     *
     * @param id  供应商 ID
     * @param dto 参数
     */
    void updateSupplier(Long id, SupplierDTO dto);

    /**
     * 启用 / 停用供应商
     *
     * @param id     供应商 ID
     * @param status 1 启用 0 停用
     */
    void changeStatus(Long id, Integer status);

    /**
     * 查询供应商详情
     *
     * @param id 供应商 ID
     * @return 供应商
     */
    BaseSupplier getById(Long id);

    /**
     * 分页查询供应商
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<BaseSupplier> page(SupplierQuery query);
}
