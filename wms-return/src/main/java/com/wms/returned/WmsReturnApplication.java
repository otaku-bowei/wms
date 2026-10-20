package com.wms.returned;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 退货管理服务启动类。
 */
@SpringBootApplication
@MapperScan("com.wms.returned.mapper")
@EnableFeignClients(basePackages = "com.wms.api.inventory")
@EnableDiscoveryClient
public class WmsReturnApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsReturnApplication.class, args);
    }
}
