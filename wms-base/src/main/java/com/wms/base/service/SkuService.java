package com.wms.base.service;

import com.wms.base.domain.dto.SkuCreateDTO;
import com.wms.base.domain.dto.SkuUpdateDTO;
import com.wms.base.domain.query.SkuQuery;
import com.wms.base.domain.vo.SkuVO;
import com.wms.common.core.result.PageResult;

import java.io.InputStream;
import java.util.List;

/**
 * SKU 服务接口
 *
 * @author WMS
 */
public interface SkuService {

    /**
     * 新增 SKU
     *
     * @param dto 参数
     * @return SKU ID
     */
    Long createSku(SkuCreateDTO dto);

    /**
     * 修改 SKU
     *
     * @param id  SKU ID
     * @param dto 参数
     */
    void updateSku(Long id, SkuUpdateDTO dto);

    /**
     * 启用 / 停用 SKU
     *
     * @param id     SKU ID
     * @param status 1 启用 0 停用
     */
    void changeStatus(Long id, Integer status);

    /**
     * 删除 SKU（仅未产生业务记录的 SKU 可删除）
     *
     * @param id SKU ID
     */
    void deleteSku(Long id);

    /**
     * 查询 SKU 详情
     *
     * @param id SKU ID
     * @return SKU 视图
     */
    SkuVO getSkuDetail(Long id);

    /**
     * 分页查询 SKU
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SkuVO> page(SkuQuery query);

    /**
     * 按条件查询 SKU（用于导出）
     *
     * @param query 查询条件
     * @return SKU 列表
     */
    List<SkuVO> listForExport(SkuQuery query);

    /**
     * 批量导入 SKU
     *
     * @param inputStream Excel 文件流
     * @return 导入结果：{totalCount, successCount, failCount, failFileUrl, errors}
     */
    java.util.Map<String, Object> importSkus(InputStream inputStream);
}
