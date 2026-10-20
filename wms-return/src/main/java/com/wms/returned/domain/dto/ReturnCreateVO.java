package com.wms.returned.domain.dto;

import com.wms.returned.domain.entity.ReturnItem;
import com.wms.returned.domain.entity.ReturnOrder;
import lombok.Data;

import java.util.List;

/**
 * 退货单创建 VO（主单 + 明细）。
 */
@Data
public class ReturnCreateVO {

    /** 退货单主单 */
    private ReturnOrder order;

    /** 退货明细 */
    private List<ReturnItem> items;
}
