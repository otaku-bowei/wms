package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 波次单实体。
 */
@Data
@TableName("wave_order")
public class WaveOrder implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 波次号 */
    private String waveNo;

    /** 仓库 ID */
    private Long warehouseId;

    /** 策略 LOGISTICS/ZONE/URGENT/TIME */
    private String waveStrategy;

    /** 最大订单数 */
    private Integer maxOrders;

    /** PENDING/GENERATING/GENERATED/CANCELLED */
    private String status;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
