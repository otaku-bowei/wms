package com.wms.base.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU 修改参数
 *
 * @author WMS
 */
@Data
public class SkuUpdateDTO {

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    private String skuName;

    /** 类目 ID */
    private Long categoryId;

    /** 商品图片 */
    private String imageUrl;

    /** 重量（克） */
    private BigDecimal weight;

    /** 体积（毫升） */
    private BigDecimal volume;

    /** 长（mm） */
    private Integer lengthMm;

    /** 宽（mm） */
    private Integer widthMm;

    /** 高（mm） */
    private Integer heightMm;

    /** 保质期（天） */
    private Integer shelfLifeDays;

    /** 条形码（全量覆盖） */
    private List<String> barcodes;
}
