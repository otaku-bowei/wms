package com.wms.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 盘点明细（只读聚合模型，用于账实准确率）。
 */
@Data
@TableName("count_item")
public class ReportCountItem implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long countId;
    private Integer diffQty;
    private String status;
    private Integer deleted;
}
