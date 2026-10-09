package com.wms.system.service;

import com.wms.system.domain.entity.SysDict;
import com.wms.system.domain.entity.SysDictItem;

import java.util.List;

/**
 * 数据字典服务接口
 *
 * @author WMS
 */
public interface DictService {

    /**
     * 查询字典类型列表
     *
     * @param dictType 字典类型（可空，空表示全部）
     * @return 字典类型列表
     */
    List<SysDict> listDictTypes(String dictType);

    /**
     * 查询字典项
     *
     * @param dictType 字典类型
     * @return 字典项列表
     */
    List<SysDictItem> listDictItems(String dictType);

    /**
     * 刷新字典缓存
     */
    void refreshCache();
}
