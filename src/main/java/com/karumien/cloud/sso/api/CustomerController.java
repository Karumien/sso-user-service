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

import com.karumien.cloud.sso.api.handler.CustomersApi;
import com.karumien.cloud.sso.api.model.CustomerBaseInfo;
import com.karumien.cloud.sso.exceptions.UnsupportedApiOperationException;

import io.swagger.annotations.Api;

/**
 * REST Controller for Customer Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
@RestController
@Api(value = "Customer Service", description = "REST API for Customer Service", tags = { "Customer Service" })
public class CustomerController implements CustomersApi {    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CustomerBaseInfo> createCustomer(@Valid CustomerBaseInfo customer) {
        throw new UnsupportedApiOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteCustomer(String id) {
        throw new UnsupportedApiOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CustomerBaseInfo> getCustomer(String id) {
        throw new UnsupportedApiOperationException();
    }
}
