package com.wms.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存调整单实体。
 */
@Data
@TableName("inv_adjust")
public class InvAdjust implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 调整单号（唯一） */
    private String adjustNo;

    /** 仓库 ID */
    private Long warehouseId;

    /** 调整原因 PROFIT/LOSS/CHECK/OTHER */
    private String reason;

    /** 状态 DRAFT/PENDING/APPROVED/REJECTED */
    private String status;

    /** 制单人 */
    private String operator;

    /** 审批人 */
    private String auditor;

    /** 审批时间 */
    private LocalDateTime auditTime;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
