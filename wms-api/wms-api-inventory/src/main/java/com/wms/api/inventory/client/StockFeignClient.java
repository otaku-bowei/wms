package com.wms.api.inventory.client;

import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.api.inventory.dto.StockVO;
import com.wms.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务 Feign 客户端，供入库、出库等服务调用。
 */
@FeignClient(name = "wms-inventory")
public interface StockFeignClient {

    /**
     * 增加库存（上架、入库）。
     */
    @PostMapping("/internal/stock/increase")
    R<Void> increase(@RequestBody StockChangeDTO dto);

    /**
     * 扣减库存（出库）。
     */
    @PostMapping("/internal/stock/decrease")
    R<Void> decrease(@RequestBody StockChangeDTO dto);

    /**
     * 锁定库存（出库预占）。
     */
    @PostMapping("/internal/stock/lock")
    R<Void> lock(@RequestBody StockChangeDTO dto);

    /**
     * 解锁库存（取消预占）。
     */
    @PostMapping("/internal/stock/unlock")
    R<Void> unlock(@RequestBody StockChangeDTO dto);

    /**
     * 按库位查询库存汇总。
     */
    @GetMapping("/internal/stock/location/{locationId}")
    R<StockVO> queryByLocation(@PathVariable("locationId") Long locationId);
}
