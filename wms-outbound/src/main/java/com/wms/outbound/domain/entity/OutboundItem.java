package com.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 出库明细实体（需求/已拣/已打包/已发）。
 */
@Data
@TableName("outbound_item")
public class OutboundItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 出库单 ID */
    private Long outboundId;

    /** SKU ID */
    private Long skuId;

    /** 需求数量 */
    private Integer planQty;

    /** 已拣数量 */
    private Integer pickedQty;

    /** 已打包数量 */
    private Integer packedQty;

    /** 已发数量 */
    private Integer shippedQty;

    /** 批次号 */
    private String batchNo;

    /** 推荐/实际拣货库位 */
    private Long locationId;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
