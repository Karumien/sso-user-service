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
import com.karumien.cloud.sso.api.model.ArrayErrorDataCodeCredentials;
import com.karumien.cloud.sso.api.model.ArrayErrorDataCodeLogin;
import com.karumien.cloud.sso.api.model.ErrorDataCodeCredentials;
import com.karumien.cloud.sso.api.model.ErrorDataCodeLogin;

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
    public ResponseEntity<ArrayErrorDataCodeLogin> getErrorDataCodeLogin() {
        ArrayErrorDataCodeLogin array = new ArrayErrorDataCodeLogin();
        array.addAll(Arrays.asList(ErrorDataCodeLogin.values()));
        return new ResponseEntity<>(array, HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ArrayErrorDataCodeCredentials> getErrorDataCodeCredentials() {
        ArrayErrorDataCodeCredentials array = new ArrayErrorDataCodeCredentials();
        array.addAll(Arrays.asList(ErrorDataCodeCredentials.values()));
        return new ResponseEntity<>(array, HttpStatus.OK);
    }
}
