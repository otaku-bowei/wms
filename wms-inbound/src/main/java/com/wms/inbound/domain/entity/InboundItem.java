package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库明细实体（计划/实收/合格/不合格/已上架）。
 */
@Data
@TableName("inbound_item")
public class InboundItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 入库单 ID */
    private Long inboundId;

    /** SKU ID */
    private Long skuId;

    /** 计划数量 */
    private Integer planQty;

    /** 实收数量 */
    private Integer receivedQty;

    /** 合格数量 */
    private Integer qualifiedQty;

    /** 不合格数量 */
    private Integer defectQty;

    /** 已上架数量 */
    private Integer putawayQty;

    /** 批次号 */
    private String batchNo;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 到期日期 */
    private LocalDate expiryDate;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
