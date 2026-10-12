package com.wms.outbound.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 装车发货 DTO。
 */
@Data
public class ShipVO implements Serializable {

    /** 出库单 ID */
    private Long outboundId;

    /** 物流商 ID */
    private Long carrierId;

    /** 运单号 */
    private String trackingNo;

    /** 车辆号 */
    private String vehicleNo;

    /** 装车人 */
    private String loader;

    /** 备注 */
    private String remark;
}
