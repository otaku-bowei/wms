package com.wms.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 *
 * <p>标注在 Controller 方法上，由 {@link com.wms.common.security.aspect.PermissionAspect} 进行鉴权。
 *
 * @author WMS
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 所需权限标识，如 sku:create
     *
     * @return 权限标识
     */
    String value();
}
