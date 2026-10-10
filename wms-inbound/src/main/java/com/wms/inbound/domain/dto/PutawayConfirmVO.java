package com.wms.inbound.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 上架确认 DTO。
 */
@Data
public class PutawayConfirmVO implements Serializable {

    /** 上架任务 ID */
    private Long taskId;

    /** 实际上架数量 */
    private Integer actualQty;

    /** 上架人 */
    private String operator;

    /** 备注 */
    private String remark;
}
