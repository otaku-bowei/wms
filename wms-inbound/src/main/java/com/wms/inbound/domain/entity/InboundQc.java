package com.wms.inbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入库质检记录实体。
 */
@Data
@TableName("inbound_qc")
public class InboundQc implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 入库单 ID */
    private Long inboundId;

    /** 入库明细 ID */
    private Long itemId;

    /** SKU ID */
    private Long skuId;

    /** 批次号 */
    private String batchNo;

    /** 送检数量 */
    private Integer inspectQty;

    /** 合格数量 */
    private Integer qualifiedQty;

    /** 不合格数量 */
    private Integer defectQty;

    /** 等级 A/B/C */
    private String grade;

    /** 不合格原因 */
    private String defectReason;

    /** 处理方式 REJECT/DEFECT_LOCATION */
    private String handleWay;

    /** 质检人 */
    private String inspector;

    /** 质检时间 */
    private LocalDateTime inspectTime;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
