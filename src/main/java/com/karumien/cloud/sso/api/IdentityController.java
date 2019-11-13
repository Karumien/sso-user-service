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
    public ResponseEntity<Void> deleteIdentity(String contactNumber) {
        identityService.deleteIdentity(contactNumber);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
    public ResponseEntity<IdentityInfo> getIdentity(String contactNumber) {
        return new ResponseEntity<>(identityService.getIdentity(contactNumber), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> impersonateIdentity(String contactNumber) {
        identityService.impersonateIdentity(contactNumber);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> logoutIdentity(String contactNumber) {
        identityService.logoutIdentity(contactNumber);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignIdentityRole(String contactNumber, String roleId) {
        identityService.assignRolesToIdentity(contactNumber, Arrays.asList(roleId));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignIdentityRole(String contactNumber, String roleId) {
        identityService.unassignRolesToIdentity(contactNumber, Arrays.asList(roleId));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignIdentityRoles(String contactNumber, @Valid List<String> roles) {
        identityService.assignRolesToIdentity(contactNumber, roles);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignIdentityRoles(String contactNumber, @Valid List<String> roles) {
        identityService.unassignRolesToIdentity(contactNumber, roles);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getIdentityRole(String contactNumber, String roleId) {
        // TODO viliam.litavec: Impl
        return IdentitiesApi.super.getIdentityRole(contactNumber, roleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<RoleInfo>> getIdentityRoles(String contactNumber) {
        return new ResponseEntity<List<RoleInfo>>(identityService.getAllIdentityRoles(contactNumber), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> resetIdentityCredentials(String contactNumber) {
        identityService.resetPasswordByEmail(contactNumber);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> blockIdentity(String contactNumber) {
        identityService.blockIdentity(contactNumber, true);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unblockIdentity(String contactNumber) {
        identityService.blockIdentity(contactNumber, false);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createDriverPin(String contactNumber, DriverPin pin) {
        identityService.savePinOfIdentityDriver(contactNumber, pin);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDriverPin(String contactNumber) {
        identityService.removePinOfIdentityDriver(contactNumber);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<DriverPin> getDriverPin(String contactNumber) {
        return new ResponseEntity<>(identityService.getPinOfIdentityDriver(contactNumber), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ResponseEntity<Void> getIdentityRolesBinary(String contactNumber) {
        String binaryRoles = roleService
                .getRolesBinary(identityService.findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber)));
        return new ResponseEntity(binaryRoles, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> exists(@Valid String username, @Valid String contactNumber, @Valid String nav4Id) {
        // TODO viliam.litavec: Need implementation
        return IdentitiesApi.super.exists(username, contactNumber, nav4Id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createIdentityNav4Credentials(String contactNumber, @Valid Credentials credentials) {
        // TODO viliam.litavec: Need implementation
        return IdentitiesApi.super.createIdentityNav4Credentials(contactNumber, credentials);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getIdentityNav4(String nav4Id) {
    	IdentityInfo identity = identityService.getIdentityByNav4(nav4Id);
    	return new ResponseEntity<IdentityInfo>(identity, identity != null ? HttpStatus.OK : HttpStatus.GONE);
    }

}
