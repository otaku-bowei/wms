package com.wms.outbound.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 波次生成 DTO。
 */
@Data
public class WaveGenerateVO implements Serializable {

    /** 波次 ID */
    private Long waveId;

    /** 待纳入波次的出库单 ID 列表 */
    private List<Long> orderIds;
}
