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

import com.karumien.cloud.sso.api.handler.AccountsApi;
import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.exceptions.UnsupportedApiOperationException;

import io.swagger.annotations.Api;

/**
 * REST Controller for Account Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
@RestController
@Api(value = "Account Service", description = "REST API for Account Service", tags = { "Account Service" })
public class AccountController implements AccountsApi {    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AccountInfo> createAccount(@Valid AccountInfo account) {
        throw new UnsupportedApiOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteAccount(String crmAccountId) {
        throw new UnsupportedApiOperationException();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AccountInfo> getAccount(String crmAccountId) {
        throw new UnsupportedApiOperationException();
    }
}
