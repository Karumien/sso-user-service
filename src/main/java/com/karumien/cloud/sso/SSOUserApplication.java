/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso;

import org.jboss.logging.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import lombok.extern.slf4j.Slf4j;

/**
 * Performance Service Application.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 10. 7. 2019 13:53:43
 */
@SpringBootApplication
//@EnableDiscoveryClient
//@EnableHystrix
//@EnableHystrixDashboard
//@EnableFeignClients
@EnableAspectJAutoProxy
@EnableAutoConfiguration
@Slf4j
public class SSOUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SSOUserApplication.class, args);
        MDC.put("group", "EW SSO API");
        log.info("EW SSO API started");
    }
    
}
