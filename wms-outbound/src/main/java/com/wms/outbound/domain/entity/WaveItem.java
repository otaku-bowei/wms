package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 波次明细（关联出库单）。
 */
@Data
@TableName("wave_item")
public class WaveItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 波次 ID */
    private Long waveId;

    /** 出库单 ID */
    private Long outboundId;

    /** PENDING/PICKING/PICKED/PACKED/SHIPPED */
    private String status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
