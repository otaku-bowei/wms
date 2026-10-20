package com.wms.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 库存（只读聚合模型）。
 */
@Data
@TableName("inv_stock")
public class ReportInvStock implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long warehouseId;
    private Integer totalQty;
    private Integer availableQty;
    private Integer lockedQty;
    private Integer frozenQty;
    private Integer deleted;
}
