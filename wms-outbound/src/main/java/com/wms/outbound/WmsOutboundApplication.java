package com.wms.outbound;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 出库管理服务启动类。
 */
@SpringBootApplication
@MapperScan("com.wms.outbound.mapper")
@EnableFeignClients(basePackages = "com.wms.api.inventory")
@EnableDiscoveryClient
public class WmsOutboundApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsOutboundApplication.class, args);
    }
}
