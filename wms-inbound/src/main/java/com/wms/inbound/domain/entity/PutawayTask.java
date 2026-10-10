package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入库上架任务实体（推荐库位/批量上架）。
 */
@Data
@TableName("putaway_task")
public class PutawayTask implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 上架任务号 */
    private String taskNo;

    /** 入库单 ID */
    private Long inboundId;

    /** 入库明细 ID */
    private Long itemId;

    /** SKU ID */
    private Long skuId;

    /** 仓库 ID */
    private Long warehouseId;

    /** 目标库位 ID */
    private Long locationId;

    /** 计划上架数量 */
    private Integer planQty;

    /** 实际上架数量 */
    private Integer actualQty;

    /** 状态 PENDING/PUTTING/COMPLETED/CANCELLED */
    private String status;

    /** 上架人 */
    private String operator;

    /** 上架时间 */
    private LocalDateTime operateTime;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
