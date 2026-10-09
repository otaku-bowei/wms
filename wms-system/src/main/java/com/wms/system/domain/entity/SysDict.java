package com.wms.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据字典类型实体（sys_dict）
 *
 * @author WMS
 */
@Data
@TableName("sys_dict")
public class SysDict {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典类型 */
    private String dictType;

    /** 字典名称 */
    private String dictName;

    /** 描述 */
    private String description;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer deleted;
}
