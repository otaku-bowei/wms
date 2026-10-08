package com.wms.common.mybatis.audit;

/**
 * 审计用户提供者（SPI）
 *
 * <p>用于审计字段自动填充时获取当前操作人。默认实现返回 system，
 * 接入安全模块后由服务提供基于登录上下文的实现。
 *
 * @author WMS
 */
public interface AuditUserProvider {

    /**
     * 获取当前操作人用户名
     *
     * @return 用户名，未获取到返回 null
     */
    String currentUsername();
}
