package com.wms.outbound.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 拣货确认 DTO。
 */
@Data
public class PickConfirmVO implements Serializable {

    /** 拣货任务 ID */
    private Long taskId;

    /** 实际拣货数量 */
    private Integer actualQty;

    /** 拣货库位 ID */
    private Long locationId;

    /** 拣货人 */
    private String picker;

    /** 备注 */
    private String remark;
}
