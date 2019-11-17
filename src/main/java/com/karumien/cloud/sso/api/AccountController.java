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

import com.karumien.cloud.sso.api.handler.AccountsApi;
import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityRoleInfo;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.service.AccountService;
import com.karumien.cloud.sso.service.IdentityService;
import com.karumien.cloud.sso.service.ModuleService;
import com.karumien.cloud.sso.service.RoleService;

import io.swagger.annotations.Api;

/**
 * REST Controller for Account Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51
 */
@RestController
@Api(value = "Account Service", description = "Management of Accounts (Customers)", tags = { "Account Service" })
public class AccountController implements AccountsApi {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private IdentityService identityService;
    
    @Autowired
    private RoleService roleService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AccountInfo> createAccount(@Valid AccountInfo account) {
        return new ResponseEntity<>(accountService.createAccount(account), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteAccount(String accountNumber) {
        accountService.deleteAccount(accountNumber);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AccountInfo> getAccount(String accountNumber) {
        return new ResponseEntity<>(accountService.getAccount(accountNumber), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<AccountInfo>> getAccounts() {
        // Locale locale = LocaleContextHolder.getLocale();
        return new ResponseEntity<>(accountService.getAccounts(), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> activateAccountModule(String accountNumber, String moduleId) {
        moduleService.activateModules(Arrays.asList(moduleId), Arrays.asList(accountNumber));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> activateAccountModules(String accountNumber, @Valid List<String> modules) {
        moduleService.activateModules(modules, Arrays.asList(accountNumber));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deactivateAccountModules(String accountNumber, @Valid List<String> modules) {
        moduleService.deactivateModules(modules, Arrays.asList(accountNumber));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ModuleInfo>> getAccountModules(String accountNumber) {
        return new ResponseEntity<>(moduleService.getAccountModules(accountNumber), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deactivateAccountModule(String accountNumber, String moduleId) {
        moduleService.deactivateModules(Arrays.asList(moduleId), Arrays.asList(accountNumber));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getAccountModule(String accountNumber, String moduleId) {
        return new ResponseEntity<>(moduleService.isActiveModule(moduleId, accountNumber) ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> createAccountIdentity(String accountNumber, @Valid IdentityInfo identity) {
        return new ResponseEntity<>(identityService.createIdentity(identity), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityInfo>> getAccountIdentities(String accountNumber, @Valid List<String> contactNumbers) {
    	return new ResponseEntity<>(accountService.getAccountIdentities(accountNumber, contactNumbers), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getAccountIdentity(String accountNumber, String contactNumber) {
        return new ResponseEntity<>(accountService.getAccountIdentity(accountNumber, contactNumber), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteAccountIdentity(String accountNumber, String contactNumber) {
		return new ResponseEntity<>(accountService.deleteAccountIdentity(accountNumber, contactNumber)? HttpStatus.NO_CONTENT : HttpStatus.GONE);
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignAccountIdentityRoles(String accountNumber, String contactNumber, @Valid List<String> roles) {
        identityService.assignRolesToIdentity(contactNumber, roles);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignAccountIdentityRoles(String accountNumber, String contactNumber, @Valid List<String> roles) {
        identityService.unassignRolesToIdentity(contactNumber, roles);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignAccountIdentityRole(String accountNumber, String contactNumber, String roleId) {
        identityService.assignRolesToIdentity(contactNumber, Arrays.asList(roleId));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignAccountIdentityRole(String accountNumber, String contactNumber, String roleId) {
        identityService.unassignRolesToIdentity(contactNumber, Arrays.asList(roleId));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getAccountIdentityRole(String accountNumber, String contactNumber, String roleId) {
        return new ResponseEntity<>(identityService.isActiveRole(roleId, contactNumber) ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public ResponseEntity<Void> checkUserNameExist(String username) {    	
//    	return new ResponseEntity<Void>(accountService.checkIfUserNameExist(username) ? HttpStatus.OK : HttpStatus.NOT_ACCEPTABLE);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public ResponseEntity<Void> createIdentityCredentials(String accountNumber, String contactNumber, @Valid Credentials credentials) {
//        identityService.createIdentityCredentials(contactNumber, credentials);
//        return new ResponseEntity<>(HttpStatus.CREATED);
//    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ModuleInfo>> getAccountHierarchy(String accountNumber) {
        return new ResponseEntity<>(accountService.getAccountHierarchy(accountNumber), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityRoleInfo>> getAccountIdentitiesRoles(String accountNumber, @Valid List<String> contactNumbers) {
        return new ResponseEntity<>(accountService.getAccountIdentitiesRoles(accountNumber, contactNumbers), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getAccountIdentityRoleIds(String accountNumber, String contactNumber) {
        getAccountIdentity(accountNumber, contactNumber);
        return new ResponseEntity<>(roleService.getIdentityRoles(contactNumber), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getAccountIdentityRightIds(String accountNumber, String contactNumber) {
        getAccountIdentity(accountNumber, contactNumber);
        return new ResponseEntity<>(accountService.getAccountRightsOfIdentity(contactNumber), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<RoleInfo>> getAccountRoles(String accountNumber) {
        return new ResponseEntity<>(accountService.getAccountRoles(accountNumber), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createIdentityCredentials(String accountNumber, String contactNumber, Credentials credentials) {
        getAccountIdentity(accountNumber, contactNumber);
        identityService.createIdentityCredentials(contactNumber, credentials);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    
}