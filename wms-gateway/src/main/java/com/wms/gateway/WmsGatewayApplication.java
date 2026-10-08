package com.wms.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * WMS 网关服务启动类
 *
 * @author WMS
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.wms.gateway", "com.wms.common"})
public class WmsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsGatewayApplication.class, args);
    }
}
