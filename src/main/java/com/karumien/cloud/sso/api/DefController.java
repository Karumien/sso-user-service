/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.karumien.cloud.sso.api.handler.DefApi;
import com.karumien.cloud.sso.api.model.ErrorDataCodeCredentials;
import com.karumien.cloud.sso.api.model.ErrorDataCodeCredentialsSpecification;
import com.karumien.cloud.sso.api.model.ErrorDataCodeLogin;
import com.karumien.cloud.sso.api.model.ErrorDataCodeLoginSpecification;

import io.swagger.annotations.Api;

/**
 * REST Controller for Definitions (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 13. 8. 2019 11:15:51 
 */
@RestController
@Api(value = "Definitions", description = "Specification of Definitions", tags = { "Definitions" })
public class DefController implements DefApi  {

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ErrorDataCodeLoginSpecification> getErrorDataCodeLogin() {
        ErrorDataCodeLoginSpecification specification = new ErrorDataCodeLoginSpecification();
        specification.setEnum(Arrays.asList(ErrorDataCodeLogin.values()));
        return new ResponseEntity<>(specification, HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ErrorDataCodeCredentialsSpecification> getErrorDataCodeCredentials() {
        ErrorDataCodeCredentialsSpecification specification = new ErrorDataCodeCredentialsSpecification();
        specification.setEnum(Arrays.asList(ErrorDataCodeCredentials.values()));
        return new ResponseEntity<>(specification, HttpStatus.OK);
    }
}
