package com.wms.inventory.domain.dto;

import com.wms.inventory.domain.entity.InvAdjust;
import com.wms.inventory.domain.entity.InvAdjustItem;
import lombok.Data;

import java.util.List;

/**
 * 调整单创建 VO（主单 + 明细）。
 */
@Data
public class AdjustCreateVO {

    /** 调整单主单 */
    private InvAdjust adjust;

    /** 调整单明细 */
    private List<InvAdjustItem> items;
}
