package com.wms.returned.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退货单实体。
 */
@Data
@TableName("return_order")
public class ReturnOrder implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 退货单号 */
    private String returnNo;

    /** 关联原出库单号 */
    private String relatedOrderNo;

    /** 仓库 ID */
    private Long warehouseId;

    /** 退货原因 */
    private String returnReason;

    /** CREATED/RECEIVED/INSPECTED/RESHELVED/CLOSED */
    private String status;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
