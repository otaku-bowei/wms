package com.wms.outbound.service;

import com.wms.outbound.domain.dto.PackVO;
import com.wms.outbound.domain.dto.ShipVO;

/**
 * 复核打包与装车服务：打包(进入 PACKING)/发货(扣减库存并置 SHIPPED)。
 */
public interface PackageService {

    /** 复核打包：生成包裹并置订单 PACKING */
    Long pack(PackVO vo);

    /** 装车发货：扣减库存并置 SHIPPED，生成装车记录 */
    void ship(ShipVO vo);
}
