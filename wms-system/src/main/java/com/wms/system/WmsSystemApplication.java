package com.wms.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * WMS 系统管理服务启动类
 *
 * @author WMS
 */
@EnableFeignClients(basePackages = "com.wms.api.system.client")
@EnableDiscoveryClient
@MapperScan("com.wms.system.mapper")
@SpringBootApplication(scanBasePackages = {"com.wms.system", "com.wms.common"})
public class WmsSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsSystemApplication.class, args);
    }
}
