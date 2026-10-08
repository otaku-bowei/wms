package com.wms.common.security.aspect;

import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.security.annotation.RequirePermission;
import com.wms.common.security.context.LoginUser;
import com.wms.common.security.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 权限校验切面
 *
 * @author WMS
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        String permissionCode = requirePermission.value();
        if (!loginUser.hasPermission(permissionCode)) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            log.warn("用户[{}]缺少权限[{}]，拒绝访问 {}", loginUser.getUsername(), permissionCode, signature.toShortString());
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return joinPoint.proceed();
    }
}
