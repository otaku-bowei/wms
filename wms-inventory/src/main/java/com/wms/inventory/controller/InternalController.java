package com.wms.inventory.controller;

import com.wms.api.inventory.dto.StockChangeDTO;
import com.wms.api.inventory.dto.StockVO;
import com.wms.common.core.result.R;
import com.wms.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存内部接口（供入库/出库等服务通过 Feign 调用）。
 */
@RestController
@RequestMapping("/internal/stock")
@RequiredArgsConstructor
public class InternalController {

    private final StockService stockService;

    @PostMapping("/increase")
    public R<Void> increase(@RequestBody StockChangeDTO dto) {
        stockService.increase(dto);
        return R.ok();
    }

    @PostMapping("/decrease")
    public R<Void> decrease(@RequestBody StockChangeDTO dto) {
        stockService.decrease(dto);
        return R.ok();
    }

    @PostMapping("/lock")
    public R<Void> lock(@RequestBody StockChangeDTO dto) {
        stockService.lock(dto);
        return R.ok();
    }

    @PostMapping("/unlock")
    public R<Void> unlock(@RequestBody StockChangeDTO dto) {
        stockService.unlock(dto);
        return R.ok();
    }

    @GetMapping("/location/{locationId}")
    public R<StockVO> queryByLocation(@PathVariable("locationId") Long locationId) {
        return R.ok(stockService.getLocationSummary(locationId));
    }
}
