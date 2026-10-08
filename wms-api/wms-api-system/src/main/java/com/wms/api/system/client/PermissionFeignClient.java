package com.wms.api.system.client;

import com.wms.api.system.dto.PermissionNodeDTO;
import com.wms.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Set;

/**
 * system 服务 - 权限内部接口
 *
 * @author WMS
 */
@FeignClient(name = "wms-system", path = "/internal/permissions")
public interface PermissionFeignClient {

    /**
     * 查询用户权限标识集合
     *
     * @param userId 用户 ID
     * @return 权限标识集合
     */
    @GetMapping("/user/{userId}")
    R<Set<String>> getUserPermissionCodes(@PathVariable("userId") Long userId);

    /**
     * 查询用户菜单树
     *
     * @param userId 用户 ID
     * @return 菜单树
     */
    @GetMapping("/menu/{userId}")
    R<List<PermissionNodeDTO>> getUserMenus(@PathVariable("userId") Long userId);
}
