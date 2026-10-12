package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出库单实体（状态机）。
 */
@Data
@TableName("outbound_order")
public class OutboundOrder implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 出库单号 */
    private String outboundNo;

    /** 仓库 ID */
    private Long warehouseId;

    /** 来源 PLATFORM/OMS/MANUAL */
    private String orderSource;

    /** 来源订单号 */
    private String relatedOrderNo;

    /** 物流商 ID */
    private Long carrierId;

    /** DRAFT/ALLOCATED/PICKING/PICKED/PACKING/SHIPPED/CANCELLED */
    private String status;

    /** 发货时间 */
    private LocalDateTime shipTime;

    /** 备注 */
    private String remark;

    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
