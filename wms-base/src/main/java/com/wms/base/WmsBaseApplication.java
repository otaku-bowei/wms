package com.wms.base;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * WMS 基础数据服务启动类
 *
 * @author WMS
 */
@EnableFeignClients(basePackages = "com.wms.api.system.client")
@EnableDiscoveryClient
@MapperScan("com.wms.base.mapper")
@SpringBootApplication(scanBasePackages = {"com.wms.base", "com.wms.common"})
public class WmsBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsBaseApplication.class, args);
    }
}
