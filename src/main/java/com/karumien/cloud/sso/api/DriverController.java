/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.karumien.cloud.sso.api.handler.DriversApi;
import com.karumien.cloud.sso.api.model.DriverBaseInfo;
import com.karumien.cloud.sso.api.model.DriverPIN;
import com.karumien.cloud.sso.service.DriverService;

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
    
	@Autowired
	DriverService driverService;
	
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DriverBaseInfo> createDriver(@Valid DriverBaseInfo driver) {
        return new ResponseEntity<>(driverService.createDriver(driver), HttpStatus.CREATED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createDriverPIN(String id, @Valid DriverPIN pin) {
        driverService.createPinForTheDriver(id, pin);
    	return new ResponseEntity<Void>(HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDriver(String id) {
    	driverService.deleteDriverUser(id);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
    
}
