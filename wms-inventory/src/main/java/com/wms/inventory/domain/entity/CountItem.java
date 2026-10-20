package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 盘点明细实体（系统库存 vs 实盘）。
 */
@Data
@TableName("count_item")
public class CountItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 盘点单 ID */
    private Long countId;

    /** SKU ID */
    private Long skuId;

    /** 库位 ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 系统库存 */
    private Integer systemQty;

    /** 实盘数量 */
    private Integer countedQty;

    /** 差异数量 */
    private Integer diffQty;

    /** PENDING/COUNTED/ADJUSTED */
    private String status;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
