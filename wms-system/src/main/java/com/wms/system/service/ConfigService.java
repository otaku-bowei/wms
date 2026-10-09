package com.wms.system.service;

import com.wms.system.domain.entity.SysConfig;

import java.util.List;

/**
 * 系统参数服务接口
 *
 * @author WMS
 */
public interface ConfigService {

    /**
     * 查询系统参数列表
     *
     * @param paramGroup 分组（可空）
     * @return 参数列表
     */
    List<SysConfig> listParams(String paramGroup);

    /**
     * 批量修改系统参数
     *
     * @param params 参数键值对（paramKey -> paramValue）
     */
    void updateParams(java.util.Map<String, String> params);

    /**
     * 查询参数值
     *
     * @param paramKey 参数键
     * @return 参数值
     */
    String getParamValue(String paramKey);
}
