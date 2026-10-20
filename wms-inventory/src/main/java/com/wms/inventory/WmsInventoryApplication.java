package com.wms.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 库存核心服务启动类。
 */
@SpringBootApplication
@MapperScan("com.wms.inventory.mapper")
@EnableDiscoveryClient
public class WmsInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsInventoryApplication.class, args);
    }
}
