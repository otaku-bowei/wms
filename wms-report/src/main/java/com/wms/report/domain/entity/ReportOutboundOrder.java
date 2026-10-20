package com.wms.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出库单（只读聚合模型）。
 */
@Data
@TableName("outbound_order")
public class ReportOutboundOrder implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long warehouseId;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime shipTime;
    private Integer deleted;
}
