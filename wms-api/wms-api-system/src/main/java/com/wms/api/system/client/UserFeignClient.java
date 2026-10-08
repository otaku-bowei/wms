package com.wms.api.system.client;

import com.wms.api.system.dto.ChangePasswordDTO;
import com.wms.api.system.dto.LoginInfoDTO;
import com.wms.api.system.dto.UserAuthDTO;
import com.wms.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * system 服务 - 用户内部接口
 *
 * @author WMS
 */
@FeignClient(name = "wms-system", path = "/internal/users")
public interface UserFeignClient {

    /**
     * 根据用户名查询用户（含密码密文）
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/by-username/{username}")
    R<UserAuthDTO> getByUsername(@PathVariable("username") String username);

    /**
     * 更新最后登录信息
     *
     * @param userId 用户 ID
     * @param dto    登录信息
     * @return 空响应
     */
    @PutMapping("/{userId}/login-info")
    R<Void> updateLoginInfo(@PathVariable("userId") Long userId, @RequestBody LoginInfoDTO dto);

    /**
     * 修改密码（由 system 服务校验原密码并加密新密码）
     *
     * @param userId 用户 ID
     * @param dto    修改密码参数
     * @return 空响应
     */
    @PostMapping("/{userId}/password")
    R<Void> changePassword(@PathVariable("userId") Long userId, @RequestBody ChangePasswordDTO dto);
}
