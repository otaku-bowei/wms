package com.wms.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * WMS 认证授权服务启动类
 *
 * @author WMS
 */
@EnableFeignClients(basePackages = "com.wms.api.system.client")
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.wms.auth", "com.wms.common"})
public class WmsAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsAuthApplication.class, args);
    }
}
