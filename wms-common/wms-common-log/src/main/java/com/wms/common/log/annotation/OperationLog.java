package com.wms.common.log.annotation;

import com.wms.common.log.enums.OperationType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 *
 * <p>由 {@link com.wms.common.log.aspect.OperationLogAspect} 采集并发布
 * {@link com.wms.common.log.event.OperationLogEvent}，由业务服务监听落库。
 *
 * @author WMS
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 操作模块，如 SKU / WAREHOUSE / LOCATION / USER */
    String module() default "";

    /** 操作类型 */
    OperationType type() default OperationType.OTHER;

    /** 操作描述，支持占位符，如 创建SKU #{#dto.skuName} */
    String description() default "";
}
