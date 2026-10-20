package com.wms.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.inventory.domain.entity.InvStock;
import com.wms.inventory.mapper.InvStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存查询接口。
 */
@RestController
@RequestMapping("/api/v1/inventory/stocks")
@RequiredArgsConstructor
public class StockController {

    private final InvStockMapper stockMapper;

    @GetMapping
    public R<PageResult<InvStock>> page(@RequestParam(required = false) Long warehouseId,
                                        @RequestParam(required = false) Long locationId,
                                        @RequestParam(required = false) Long skuId,
                                        @RequestParam(defaultValue = "1") long pageNum,
                                        @RequestParam(defaultValue = "20") long pageSize) {
        IPage<InvStock> page = new Page<>(pageNum, pageSize);
        stockMapper.selectPage(page, new LambdaQueryWrapper<InvStock>()
                .eq(warehouseId != null, InvStock::getWarehouseId, warehouseId)
                .eq(locationId != null, InvStock::getLocationId, locationId)
                .eq(skuId != null, InvStock::getSkuId, skuId)
                .eq(InvStock::getDeleted, 0)
                .orderByDesc(InvStock::getUpdateTime));
        return R.ok(PageConvert.toResult(page));
    }
}
