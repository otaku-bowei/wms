package com.wms.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 认证服务 Bean 配置
 *
 * @author WMS
 */
@Configuration
public class BeanConfig {

    /**
     * 密码编码器（委派模式，支持 {noop} / {bcrypt} 前缀）
     *
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
