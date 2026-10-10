package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入库预约单实体（状态机）。
 */
@Data
@TableName("inbound_order")
public class InboundOrder implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 入库单号 */
    private String inboundNo;

    /** 仓库 ID */
    private Long warehouseId;

    /** 供应商 ID */
    private Long supplierId;

    /** 入库类型 PURCHASE/RETURN/TRANSFER */
    private String inboundType;

    /** 预计到货 */
    private LocalDateTime expectedTime;

    /** 实际到货 */
    private LocalDateTime arriveTime;

    /** 状态 DRAFT/CONFIRMED/ARRIVED/PENDING_QC/PUTTING_AWAY/COMPLETED/CANCELLED */
    private String status;

    /** 备注 */
    private String remark;

    private String createBy;
    private String updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
