package com.wms.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 报表分析服务启动类（读取共享库的各业务表做聚合）。
 */
@SpringBootApplication
@MapperScan("com.wms.report.mapper")
@EnableDiscoveryClient
public class WmsReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsReportApplication.class, args);
    }
}
