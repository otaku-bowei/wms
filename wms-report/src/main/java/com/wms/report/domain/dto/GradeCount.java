package com.wms.report.domain.dto;

import lombok.Data;

/**
 * 退货等级分布统计。
 */
@Data
public class GradeCount {

    private String grade;
    private Long count;

    public GradeCount(String grade, Long count) {
        this.grade = grade;
        this.count = count;
    }
}
