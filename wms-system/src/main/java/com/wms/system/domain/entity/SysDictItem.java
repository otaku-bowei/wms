package com.wms.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据字典项实体（sys_dict_item）
 *
 * @author WMS
 */
@Data
@TableName("sys_dict_item")
public class SysDictItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典类型 */
    private String dictType;

    /** 枚举值 */
    private String itemValue;

    /** 枚举名称 */
    private String itemLabel;

    /** 排序 */
    private Integer sort;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer deleted;
}
