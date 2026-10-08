package com.wms.api.system.client;

import com.wms.api.system.dto.LoginLogDTO;
import com.wms.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * system 服务 - 登录日志内部接口
 *
 * @author WMS
 */
@FeignClient(name = "wms-system", path = "/internal/logs")
public interface LoginLogFeignClient {

    /**
     * 记录登录日志
     *
     * @param dto 登录日志参数
     * @return 空响应
     */
    @PostMapping("/login")
    R<Void> record(@RequestBody LoginLogDTO dto);
}
