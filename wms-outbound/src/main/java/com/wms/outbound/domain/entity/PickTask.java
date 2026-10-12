package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 拣货任务实体。
 */
@Data
@TableName("pick_task")
public class PickTask implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 拣货任务号 */
    private String taskNo;

    /** 波次 ID */
    private Long waveId;

    /** 出库单 ID */
    private Long outboundId;

    /** 出库明细 ID */
    private Long itemId;

    /** SKU ID */
    private Long skuId;

    /** 仓库 ID */
    private Long warehouseId;

    /** 拣货库位 */
    private Long locationId;

    /** 计划拣货数量 */
    private Integer planQty;

    /** 实际拣货数量 */
    private Integer actualQty;

    /** PENDING/PICKING/PICKED/CANCELLED */
    private String status;

    /** 拣货人 */
    private String picker;

    /** 拣货时间 */
    private LocalDateTime pickTime;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
