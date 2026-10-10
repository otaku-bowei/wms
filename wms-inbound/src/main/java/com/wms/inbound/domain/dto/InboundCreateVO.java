package com.wms.inbound.domain.dto;

import com.wms.inbound.domain.entity.InboundItem;
import com.wms.inbound.domain.entity.InboundOrder;
import lombok.Data;

import java.util.List;

/**
 * 入库单创建 VO（预约单 + 明细）。
 */
@Data
public class InboundCreateVO {

    /** 入库预约单主单 */
    private InboundOrder order;

    /** 入库明细 */
    private List<InboundItem> items;
}
