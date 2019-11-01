/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso;

import java.util.Collections;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger/SpringFox API documentation Configuration.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 9. 6. 2019 0:07:58
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Value("${server.servlet.swaggerPath}")
    private String swaggerPath;
    /**
     * Specification of REST Microservice API v1.0
     * 
     * @return {@link Docket} REST API specification
     */
    @Bean
    public Docket api10(ServletContext servletContext) {
        return new Docket(DocumentationType.SWAGGER_2).groupName("ew-sso-api-1.0")
            .pathProvider(new RelativePathProvider(servletContext) {
                @Override
                public String getApplicationBasePath() {
                    return swaggerPath;
                }
            })
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.karumien.cloud.sso.api"))
            .paths(PathSelectors.any())
            .build()
            .useDefaultResponseMessages(false)
            .globalOperationParameters(Collections.singletonList(new ParameterBuilder()
                .name("x-locale")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .description("User's locale for apply, i.e. en, cs")
                .required(false)
                .build()))
//            .globalResponseMessage(RequestMethod.GET, getCustomizedResponseMessages())                    
//            .globalResponseMessage(RequestMethod.DELETE, getCustomizedResponseMessages())                    
//            .globalResponseMessage(RequestMethod.POST, getCustomizedResponseMessages())                    
//            .globalResponseMessage(RequestMethod.PUT, getCustomizedResponseMessages())                    
            .produces(Collections.singleton(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .apiInfo(new ApiInfoBuilder().version("1.0").title("Eurowag SSO API (IAM) Documentation")
            .description("Services of EW SSO API (IAM).")
            .build());
    }
    
//    private List<ResponseMessage> getCustomizedResponseMessages(){
//        List<ResponseMessage> responseMessages = new ArrayList<>();
//        responseMessages.add(new ResponseMessageBuilder().code(501).message("Not Implemented Now").responseModel(new ModelRef("Error")).build());
//        return responseMessages;
//    }

}
