package com.wms.outbound.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 复核打包 DTO。
 */
@Data
public class PackVO implements Serializable {

    /** 出库单 ID */
    private Long outboundId;

    /** 物流商 ID */
    private Long carrierId;

    /** 运单号 */
    private String trackingNo;

    /** 包裹重量(kg) */
    private BigDecimal weight;

    /** 打包人 */
    private String packer;

    /** 备注 */
    private String remark;
}
