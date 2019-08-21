/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.karumien.cloud.sso.api.handler.DriversApi;
import com.karumien.cloud.sso.api.model.DriverBaseInfo;
import com.karumien.cloud.sso.api.model.DriverPIN;
import com.karumien.cloud.sso.exceptions.UnsupportedApiOperationException;

import io.swagger.annotations.Api;

/**
 * REST Controller for Customer Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
@RestController
@Api(value = "Driver Service", description = "REST API for Driver Service", tags = { "Driver Service" })
public class DriverController implements DriversApi {   
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DriverBaseInfo> createDriver(@Valid DriverBaseInfo driver) {
        throw new UnsupportedApiOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createDriverPIN(String id, @Valid DriverPIN pin) {
        throw new UnsupportedApiOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDriver(String id) {
        throw new UnsupportedApiOperationException();
    }
    
}
