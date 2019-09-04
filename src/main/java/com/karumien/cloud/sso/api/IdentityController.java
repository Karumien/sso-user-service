/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.karumien.cloud.sso.api.handler.IdentitiesApi;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.api.model.RoleInfo;
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
@Api(value = "Identity Service", description = "Management of Identities (Users/Contacts)", tags = { "Identity Service" })
public class IdentityController implements IdentitiesApi {

    @Autowired
    private IdentityService identityService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> createIdentity(@Valid IdentityInfo Identity) {
        return new ResponseEntity<>(identityService.createIdentity(Identity), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteIdentity(String crmContactId) {
        identityService.deleteIdentity(crmContactId);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Policy> getPasswordPolicy() {
        return new ResponseEntity<>(identityService.getPasswordPolicy(), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Policy> createIdentityCredentials(String id, @Valid Credentials credentials) {
        try {
            identityService.createIdentityCredentials(id, credentials);
        } catch (PolicyPasswordException e) {
            return new ResponseEntity<>(identityService.getPasswordPolicy(), HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getIdentity(String crmContactId) {
        return new ResponseEntity<>(identityService.getIdentity(crmContactId), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> impersonateIdentity(String crmContactId) {
        identityService.impersonateIdentity(crmContactId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> logoutIdentity(String crmContactId) {
        identityService.logoutIdentity(crmContactId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignIdentityRole(String crmContactId, String roleId) {
        // TODO viliam.litavec: Impl
        return IdentitiesApi.super.assignIdentityRole(crmContactId, roleId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignIdentityRole(String crmContactId, String roleId) {
        // TODO viliam.litavec: Impl
        return IdentitiesApi.super.unassignIdentityRole(crmContactId, roleId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignIdentityRoles(String crmContactId, @Valid List<String> roles) {
        // TODO viliam.litavec: Impl
        return IdentitiesApi.super.assignIdentityRoles(crmContactId, roles);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignIdentityRoles(String crmContactId, @Valid List<String> roles) {
        // TODO viliam.litavec: Impl
        return IdentitiesApi.super.unassignIdentityRoles(crmContactId, roles);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getIdentityRole(String crmContactId, String roleId) {
        // TODO viliam.litavec: Impl
        return IdentitiesApi.super.getIdentityRole(crmContactId, roleId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<RoleInfo>> getIdentityRoles(String crmContactId) {
        // TODO viliam.litavec: Impl
        return IdentitiesApi.super.getIdentityRoles(crmContactId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createDriverPin(String id, DriverPin pin) {
        // TODO viliam.litavec: Impl
        return IdentitiesApi.super.createDriverPin(id, pin);
    }
    
}
