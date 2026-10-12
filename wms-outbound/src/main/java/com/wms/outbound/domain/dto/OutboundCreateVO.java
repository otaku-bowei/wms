package com.wms.outbound.domain.dto;

import com.wms.outbound.domain.entity.OutboundItem;
import com.wms.outbound.domain.entity.OutboundOrder;
import lombok.Data;

import java.util.List;

/**
 * 出库单创建 VO（主单 + 明细）。
 */
@Data
public class OutboundCreateVO {

    /** 出库单主单 */
    private OutboundOrder order;

    /** 出库明细 */
    private List<OutboundItem> items;
}
