package com.wms.system.service;

import com.wms.api.system.dto.LoginLogDTO;
import com.wms.common.core.result.PageResult;
import com.wms.common.log.event.OperationLogEvent;
import com.wms.system.domain.entity.SysLoginLog;
import com.wms.system.domain.entity.SysOperationLog;
import com.wms.system.domain.query.LogQuery;

/**
 * 日志服务接口
 *
 * @author WMS
 */
public interface LogService {

    /**
     * 分页查询操作日志
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SysOperationLog> pageOperationLogs(LogQuery query);

    /**
     * 分页查询登录日志
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<SysLoginLog> pageLoginLogs(LogQuery query);

    /**
     * 保存操作日志
     *
     * @param event 日志事件
     */
    void saveOperationLog(OperationLogEvent event);

    /**
     * 保存登录日志
     *
     * @param dto 登录日志参数
     */
    void saveLoginLog(LoginLogDTO dto);
}
