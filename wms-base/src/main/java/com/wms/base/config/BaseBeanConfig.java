package com.wms.base.config;

import com.wms.common.mybatis.audit.AuditUserProvider;
import com.wms.common.security.context.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * base 服务 Bean 配置
 *
 * @author WMS
 */
@Configuration
@EnableAsync
public class BaseBeanConfig {

    /**
     * 审计用户提供者：取当前登录用户名
     *
     * @return 审计用户提供者
     */
    @Bean
    public AuditUserProvider auditUserProvider() {
        return UserContext::getUsername;
    }
}
