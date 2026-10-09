package com.wms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.api.system.dto.LoginLogDTO;
import com.wms.common.core.result.PageResult;
import com.wms.common.log.event.OperationLogEvent;
import com.wms.common.mybatis.convert.PageConvert;
import com.wms.system.domain.entity.SysLoginLog;
import com.wms.system.domain.entity.SysOperationLog;
import com.wms.system.domain.query.LogQuery;
import com.wms.system.mapper.SysLoginLogMapper;
import com.wms.system.mapper.SysOperationLogMapper;
import com.wms.system.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 日志服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements LogService {

    private final SysLoginLogMapper loginLogMapper;

    @Override
    public PageResult<SysOperationLog> pageOperationLogs(LogQuery query) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .like(StringUtils.hasText(query.getUsername()), SysOperationLog::getUsername, query.getUsername())
                .eq(StringUtils.hasText(query.getModule()), SysOperationLog::getModule, query.getModule())
                .eq(StringUtils.hasText(query.getOperationType()), SysOperationLog::getOperationType, query.getOperationType())
                .ge(query.getStartTime() != null, SysOperationLog::getOperateTime, query.getStartTime())
                .le(query.getEndTime() != null, SysOperationLog::getOperateTime, query.getEndTime())
                .orderByDesc(SysOperationLog::getOperateTime);
        IPage<SysOperationLog> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageConvert.toResult(page);
    }

    @Override
    public PageResult<SysLoginLog> pageLoginLogs(LogQuery query) {
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
                .like(StringUtils.hasText(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
                .eq(StringUtils.hasText(query.getLoginResult()), SysLoginLog::getLoginResult, query.getLoginResult())
                .ge(query.getStartTime() != null, SysLoginLog::getLoginTime, query.getStartTime())
                .le(query.getEndTime() != null, SysLoginLog::getLoginTime, query.getEndTime())
                .orderByDesc(SysLoginLog::getLoginTime);
        IPage<SysLoginLog> page = loginLogMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageConvert.toResult(page);
    }

    @Override
    @Async
    @EventListener
    public void saveOperationLog(OperationLogEvent event) {
        try {
            SysOperationLog entity = new SysOperationLog();
            entity.setUserId(event.getUserId());
            entity.setUsername(event.getUsername());
            entity.setModule(event.getModule());
            entity.setOperationType(event.getOperationType());
            entity.setDescription(event.getDescription());
            entity.setRequestParams(event.getRequestParams());
            entity.setIpAddress(event.getIpAddress());
            entity.setSuccess(event.isSuccess() ? 1 : 0);
            entity.setErrorMsg(event.getErrorMsg());
            entity.setOperateTime(LocalDateTime.now());
            this.save(entity);
        } catch (Exception e) {
            log.warn("保存操作日志失败：{}", e.getMessage());
        }
    }

    @Override
    public void saveLoginLog(LoginLogDTO dto) {
        try {
            SysLoginLog entity = new SysLoginLog();
            entity.setUsername(dto.getUsername());
            entity.setIpAddress(dto.getIpAddress());
            entity.setBrowser(dto.getBrowser());
            entity.setOs(dto.getOs());
            entity.setLoginResult(dto.getLoginResult());
            entity.setFailReason(dto.getFailReason());
            entity.setLoginTime(LocalDateTime.now());
            loginLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("保存登录日志失败：username={}", dto.getUsername());
        }
    }
}
