/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<Void> deleteAccount(String crmAccountId) {
        accountService.deleteAccount(crmAccountId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AccountInfo> getAccount(String crmAccountId) {
        return new ResponseEntity<>(accountService.getAccount(crmAccountId), HttpStatus.OK);
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
    public ResponseEntity<Void> activateAccountModule(String crmAccountId, String moduleId) {
        moduleService.activateModules(Arrays.asList(moduleId), Arrays.asList(crmAccountId));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> activateAccountModules(String crmAccountId, @Valid List<String> modules) {
        moduleService.activateModules(modules, Arrays.asList(crmAccountId));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deactivateAccountModules(String crmAccountId, @Valid List<String> modules) {
        moduleService.deactivateModules(modules, Arrays.asList(crmAccountId));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ModuleInfo>> getAccountModules(String crmAccountId) {
        return new ResponseEntity<>(moduleService.getAccountModules(crmAccountId), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deactivateAccountModule(String crmAccountId, String moduleId) {
        moduleService.deactivateModules(Arrays.asList(moduleId), Arrays.asList(crmAccountId));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getAccountModule(String crmAccountId, String moduleId) {
        return new ResponseEntity<>(moduleService.isActiveModule(moduleId, crmAccountId) ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> createAccountIdentity(String crmAccountId, @Valid IdentityInfo identity) {
        return new ResponseEntity<>(identityService.createIdentity(identity), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityInfo>> getAccountIdentities(String crmAccountId, @Valid List<String> crmContactIds) {
    	return new ResponseEntity<>(accountService.getAccountIdentities(crmAccountId, crmContactIds), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getAccountIdentity(String crmAccountId, String crmContactId) {
        return new ResponseEntity<>(accountService.getAccountIdentityBaseOnCrmContractId(crmAccountId, crmContactId), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteAccountIdentity(String crmAccountId, String crmContactId) {
		return new ResponseEntity<>(accountService.deleteAccountIdentityBaseOnCrmContractId(crmAccountId, crmContactId)? HttpStatus.OK : HttpStatus.GONE);
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignAccountIdentityRoles(String crmAccountId, String crmContactId, @Valid List<String> roles) {
        return new ResponseEntity<>(identityService.assignRolesToIdentity(crmContactId, roles) ? HttpStatus.OK : HttpStatus.NOT_EXTENDED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getAccountIdentityRoleIds(String crmAccountId, String crmContactId) {
        return new ResponseEntity<>(
            identityService.getAllIdentityRoles(crmContactId).stream().map(r -> r.getRoleId()).collect(Collectors.toList()), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignAccountIdentityRoles(String crmAccountId, String crmContactId, @Valid List<String> roles) {
        return new ResponseEntity<>(identityService.unassignRolesToIdentity(crmContactId, roles) ? HttpStatus.OK : HttpStatus.NOT_EXTENDED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignAccountIdentityRole(String crmAccountId, String crmContactId, String roleId) {
        return new ResponseEntity<>(identityService.assignRolesToIdentity(crmContactId, Arrays.asList(roleId)) ? HttpStatus.OK : HttpStatus.NOT_EXTENDED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignAccountIdentityRole(String crmAccountId, String crmContactId, String roleId) {
        return new ResponseEntity<>(identityService.unassignRolesToIdentity(crmContactId, Arrays.asList(roleId)) ? HttpStatus.OK : HttpStatus.NOT_EXTENDED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getAccountIdentityRole(String crmAccountId, String crmContactId, String roleId) {
        return new ResponseEntity<>(identityService.isActiveRole(roleId, crmContactId) ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
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
//    public ResponseEntity<Void> createIdentityCredentials(String crmAccountId, String crmContactId, @Valid Credentials credentials) {
//        identityService.createIdentityCredentials(crmContactId, credentials);
//        return new ResponseEntity<>(HttpStatus.CREATED);
//    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ModuleInfo>> getAccountHierarchy(String crmAccountId) {
        // TODO viliam.litavec: Need implementation
        return AccountsApi.super.getAccountHierarchy(crmAccountId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityRoleInfo>> getAccountIdentitiesRoles(String crmAccountId, @Valid List<String> crmContactIds) {
        // TODO viliam.litavec: Need implementation
        return AccountsApi.super.getAccountIdentitiesRoles(crmAccountId, crmContactIds);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getAccountIdentityRightIds(String crmAccountId, String crmContactId) {
        // TODO viliam.litavec: Need implementation
        return AccountsApi.super.getAccountIdentityRightIds(crmAccountId, crmContactId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<RoleInfo>> getAccountRoles(String crmAccountId) {
        // TODO viliam.litavec: Need implementation
        return AccountsApi.super.getAccountRoles(crmAccountId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createIdentityCredentials(String crmAccountId, String crmContactId, Credentials credentials) {
    	return new ResponseEntity<Void>(accountService.updateCredentialsForIdentity(crmAccountId,crmContactId, credentials) ? HttpStatus.CREATED : HttpStatus.GONE);
    }
}
