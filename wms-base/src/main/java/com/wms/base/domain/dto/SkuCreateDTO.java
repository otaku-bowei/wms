package com.wms.base.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU 新增参数
 *
 * @author WMS
 */
@Data
public class SkuCreateDTO {

    /** SKU 编码（留空由系统生成） */
    private String skuCode;

    /** SPU 编码 */
    @NotBlank(message = "SPU 编码不能为空")
    private String spuCode;

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    private String skuName;

    /** 颜色 */
    @NotBlank(message = "颜色不能为空")
    private String color;

    /** 尺码 */
    @NotBlank(message = "尺码不能为空")
    private String size;

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

    /** 条形码（支持一品多码） */
    private List<String> barcodes;

    /** 状态：1 启用 0 停用 */
    private Integer status = 1;
}
