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

import com.karumien.cloud.sso.api.handler.IdentitiesApi;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.exceptions.PolicyPasswordException;
import com.karumien.cloud.sso.service.IdentityService;

import io.swagger.annotations.Api;

/**
 * REST Controller for Identity Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
@RestController
@Api(value = "Identity Service", description = "REST API for Identity Service", tags = { "Identity Service" })
public class IdentityController implements IdentitiesApi {

    @Autowired
    private IdentityService IdentityService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> createIdentity(@Valid IdentityInfo Identity) {
        return new ResponseEntity<>(IdentityService.createIdentity(Identity), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteIdentity(String crmContactId) {
        IdentityService.deleteIdentity(crmContactId);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Policy> getPasswordPolicy() {
        return new ResponseEntity<>(IdentityService.getPasswordPolicy(), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Policy> createIdentityCredentials(String id, @Valid Credentials credentials) {
        try {
            IdentityService.createIdentityCredentials(id, credentials);
        } catch (PolicyPasswordException e) {
            return new ResponseEntity<>(IdentityService.getPasswordPolicy(), HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getIdentity(String crmContactId) {
        return new ResponseEntity<>(IdentityService.getIdentity(crmContactId), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> impersonateIdentity(String crmContactId) {
        IdentityService.impersonateIdentity(crmContactId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> logoutIdentity(String crmContactId) {
        IdentityService.logoutIdentity(crmContactId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
}
