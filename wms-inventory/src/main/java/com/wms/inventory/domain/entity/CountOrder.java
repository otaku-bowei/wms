package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 盘点单实体。
 */
@Data
@TableName("count_order")
public class CountOrder implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 盘点单号 */
    private String countNo;

    /** 仓库 ID */
    private Long warehouseId;

    /** 盘点类型 FULL/CYCLE/SPOT */
    private String countType;

    /** PENDING/COUNTING/COMPLETED/CANCELLED */
    private String status;

    /** 盘点原因 */
    private String reason;

    /** 备注 */
    private String remark;

    private String createBy;
    private String operator;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
