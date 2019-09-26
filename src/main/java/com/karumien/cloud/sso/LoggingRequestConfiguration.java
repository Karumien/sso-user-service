/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Apply {@link LoggingRequestInterceptor} to MVC Configuration.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 25. 9. 2019 23:03:38 
 */
@Configuration
public class LoggingRequestConfiguration implements WebMvcConfigurer {

    @Autowired
    private LoggingRequestInterceptor loggingRequestInterceptor;
 
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingRequestInterceptor)
          .addPathPatterns("/users/**", "/accounts/**", "/auth/**", 
                "/modules/**", "/roles/**", "/locale/**");
    }
    
}
