package com.wms.report.domain.dto;

import lombok.Data;

/**
 * 状态分布统计。
 */
@Data
public class StatusCount {

    private String status;
    private Long count;

    public StatusCount(String status, Long count) {
        this.status = status;
        this.count = count;
    }
}
