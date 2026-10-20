package com.wms.inventory.domain.dto;

import com.wms.inventory.domain.entity.CountItem;
import com.wms.inventory.domain.entity.CountOrder;
import lombok.Data;

import java.util.List;

/**
 * 盘点单创建 VO（主单 + 明细）。
 */
@Data
public class CountCreateVO {

    /** 盘点单主单 */
    private CountOrder order;

    /** 盘点明细 */
    private List<CountItem> items;
}
