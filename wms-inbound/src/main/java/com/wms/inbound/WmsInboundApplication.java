package com.wms.inbound;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 入库管理服务启动类。
 */
@SpringBootApplication
@MapperScan("com.wms.inbound.mapper")
@EnableFeignClients(basePackages = "com.wms.api.inventory")
@EnableDiscoveryClient
public class WmsInboundApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsInboundApplication.class, args);
    }
}
