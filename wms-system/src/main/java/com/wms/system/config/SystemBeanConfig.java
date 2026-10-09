package com.wms.system.config;

import com.wms.common.mybatis.audit.AuditUserProvider;
import com.wms.common.security.context.UserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * system 服务 Bean 配置
 *
 * @author WMS
 */
@Configuration
@EnableAsync
public class SystemBeanConfig {

    /**
     * 密码编码器（委派模式，支持 {noop} / {bcrypt} 前缀）
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

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
