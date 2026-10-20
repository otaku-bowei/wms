package com.wms.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入库单（只读聚合模型）。
 */
@Data
@TableName("inbound_order")
public class ReportInboundOrder implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long warehouseId;
    private String status;
    private LocalDateTime createTime;
    private Integer deleted;
}
