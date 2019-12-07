/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.karumien.cloud.sso.api.handler.IdentitiesApi;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.ErrorCode;
import com.karumien.cloud.sso.api.model.ErrorData;
import com.karumien.cloud.sso.api.model.ErrorDataCodeCredentials;
import com.karumien.cloud.sso.api.model.ErrorMessage;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityPropertyType;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;
import com.karumien.cloud.sso.exceptions.PasswordPolicyException;
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

    @Autowired
    private MessageSource messageSource;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> createIdentity(IdentityInfo Identity) {
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ResponseEntity<Void> createIdentityCredentials(String contactNumber, Credentials credentials) {
        try {
            identityService.createIdentityCredentials(contactNumber, credentials);
        } catch (PasswordPolicyException e) {            
            return new ResponseEntity(new ErrorMessage().errcode(ErrorCode.ERROR).errno(300)
                .errmsg(e.getMessage())
                .errdata(Arrays.asList(new ErrorData()
                    .description(messageSource.getMessage("error.credentials." + ErrorDataCodeCredentials.PASSWORD.toString(), null, LocaleContextHolder.getLocale()))
                    .code(ErrorDataCodeCredentials.PASSWORD.toString()))), HttpStatus.UNPROCESSABLE_ENTITY);
        }
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
        identityService.updateRolesOfIdentity(identityService.getIdentity(contactNumber).getIdentityId(), Arrays.asList(roleId), UpdateType.ADD);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignIdentityRoles(String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(identityService.getIdentity(contactNumber).getIdentityId(), roles, UpdateType.ADD);
        return new ResponseEntity<>(HttpStatus.PARTIAL_CONTENT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> updateIdentityRoles(String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(identityService.getIdentity(contactNumber).getIdentityId(), roles, UpdateType.UPDATE);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignIdentityRole(String contactNumber, String roleId) {
        identityService.updateRolesOfIdentity(identityService.getIdentity(contactNumber).getIdentityId(), Arrays.asList(roleId), UpdateType.DELETE);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignIdentityRoles(String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(identityService.getIdentity(contactNumber).getIdentityId(), roles, UpdateType.DELETE);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getIdentityRole(String contactNumber, String roleId) {
        return new ResponseEntity<>(identityService.isActiveRole(roleId, contactNumber) ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
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
    public ResponseEntity<Void> exists(String username, String contactNumber, String nav4Id) {
        
        if (StringUtils.hasText(username)) {
            return new ResponseEntity<>(identityService.isIdentityExists(username) ? HttpStatus.OK : HttpStatus.GONE);
        }

        if (StringUtils.hasText(contactNumber)) {
            identityService.getIdentity(contactNumber);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        if (StringUtils.hasText(nav4Id)) {
            identityService.getIdentityByNav4(nav4Id);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);    
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityInfo>> search(String identityId, String username, String accountNumber, String contactNumber,
            String nav4Id, String email, String globalEmail, String phone) {

        Map<IdentityPropertyType, String> searchFilter = new HashMap<>();
        identityService.putIfPresent(searchFilter, IdentityPropertyType.ID, identityId);
        identityService.putIfPresent(searchFilter, IdentityPropertyType.USERNAME, username);
        identityService.putIfPresent(searchFilter, IdentityPropertyType.EMAIL, email);
        identityService.putIfPresent(searchFilter, IdentityPropertyType.ATTR_ACCOUNT_NUMBER, accountNumber);
        identityService.putIfPresent(searchFilter, IdentityPropertyType.ATTR_CONTACT_NUMBER, contactNumber);
        identityService.putIfPresent(searchFilter, IdentityPropertyType.ATTR_NAV4ID, nav4Id);
        identityService.putIfPresent(searchFilter, IdentityPropertyType.ATTR_GLOBAL_EMAIL, globalEmail);
        identityService.putIfPresent(searchFilter, IdentityPropertyType.ATTR_PHONE, phone);
        
        if (searchFilter.isEmpty()) {
            new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        List<IdentityInfo> found = identityService.search(searchFilter);
        return CollectionUtils.isEmpty(found) ? new ResponseEntity<>(HttpStatus.GONE) : new ResponseEntity<>(found, HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ResponseEntity<Void> createIdentityNav4Credentials(String nav4Id, Credentials credentials) {
        try {
            identityService.createIdentityCredentialsNav4(nav4Id, credentials);
        } catch (PasswordPolicyException e) {            
            return new ResponseEntity(new ErrorMessage().errcode(ErrorCode.ERROR).errno(300)
                .errmsg(e.getMessage())
                .errdata(Arrays.asList(new ErrorData()
                    .description(messageSource.getMessage("error.credentials." + ErrorDataCodeCredentials.PASSWORD.toString(), null, LocaleContextHolder.getLocale()))
                    .code(ErrorDataCodeCredentials.PASSWORD.toString()))), HttpStatus.UNPROCESSABLE_ENTITY);
        }        
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getNav4Identity(String nav4Id) {
    	IdentityInfo identity = identityService.getIdentityByNav4(nav4Id);
    	return new ResponseEntity<IdentityInfo>(identity, identity != null ? HttpStatus.OK : HttpStatus.GONE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getIdentityRoleIds(String contactNumber) {
        return new ResponseEntity<>(roleService.getIdentityRoles(contactNumber), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> updateIdentity(String contactNumber, IdentityInfo identity) {
        return new ResponseEntity<>( identityService.updateIdentity(contactNumber, identity), HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getNav4IdentityRole(String nav4Id, String roleId) {
        return new ResponseEntity<>(identityService.isActiveRoleNav4(roleId, nav4Id) ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getNav4IdentityRoleIds(String nav4Id) {
        return new ResponseEntity<>(roleService.getIdentityRolesNav4(nav4Id), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignNav4IdentityRole(String nav4Id, String roleId) {
        identityService.updateRolesOfIdentity(identityService.getIdentityByNav4(nav4Id).getIdentityId(), Arrays.asList(roleId), UpdateType.ADD);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignNav4IdentityRole(String nav4Id, String roleId) {
        identityService.updateRolesOfIdentity(identityService.getIdentityByNav4(nav4Id).getIdentityId(), Arrays.asList(roleId), UpdateType.DELETE);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
