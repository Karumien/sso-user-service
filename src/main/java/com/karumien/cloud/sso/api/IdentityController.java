/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import java.util.Arrays;
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
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;
import com.karumien.cloud.sso.service.IdentityService;
import com.karumien.cloud.sso.service.RoleService;

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

    @Autowired
    private RoleService roleService;

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
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createIdentityCredentials(String id, @Valid Credentials credentials) {
        identityService.createIdentityCredentials(id, credentials);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
        return new ResponseEntity<>(identityService.assignRolesToIdentity(crmContactId, Arrays.asList(roleId)) ? HttpStatus.ACCEPTED : HttpStatus.NOT_EXTENDED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignIdentityRole(String crmContactId, String roleId) {
        return new ResponseEntity<>(
                identityService.unassignRolesToIdentity(crmContactId, Arrays.asList(roleId)) ? HttpStatus.ACCEPTED : HttpStatus.NOT_EXTENDED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignIdentityRoles(String crmContactId, @Valid List<String> roles) {
        return new ResponseEntity<>(identityService.assignRolesToIdentity(crmContactId, roles) ? HttpStatus.ACCEPTED : HttpStatus.NOT_EXTENDED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignIdentityRoles(String crmContactId, @Valid List<String> roles) {
        return new ResponseEntity<>(identityService.unassignRolesToIdentity(crmContactId, roles) ? HttpStatus.ACCEPTED : HttpStatus.NOT_EXTENDED);
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
        return new ResponseEntity<List<RoleInfo>>(identityService.getAllIdentityRoles(crmContactId), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> resetIdentityCredentials(String crmContactId) {
        identityService.resetPasswordByEmail(crmContactId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> blockIdentity(String crmContactId) {
        identityService.blockIdentity(crmContactId, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unblockIdentity(String crmContactId) {
        identityService.blockIdentity(crmContactId, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createDriverPin(String crmContactId, DriverPin pin) {
        identityService.savePinOfIdentityDriver(crmContactId, pin);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDriverPin(String crmContactId) {
        identityService.removePinOfIdentityDriver(crmContactId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DriverPin> getDriverPin(String crmContactId) {
        return new ResponseEntity<>(identityService.getPinOfIdentityDriver(crmContactId), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ResponseEntity<Void> getIdentityRolesBinary(String crmContactId) {
        String binaryRoles = roleService
                .getRolesBinary(identityService.findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId)));
        return new ResponseEntity(binaryRoles, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> exists(@Valid String username, @Valid String crmContactId, @Valid String nav4Id) {
        // TODO viliam.litavec: Need implementation
        return IdentitiesApi.super.exists(username, crmContactId, nav4Id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createIdentityNav4Credentials(String crmContactId, @Valid Credentials credentials) {
        // TODO viliam.litavec: Need implementation
        return IdentitiesApi.super.createIdentityNav4Credentials(crmContactId, credentials);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getIdentityNav4(String nav4Id) {
        // TODO viliam.litavec: Need implementation
        return IdentitiesApi.super.getIdentityNav4(nav4Id);
    }

}
