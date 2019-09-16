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
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.ModuleInfo;
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
        return new ResponseEntity<>(moduleService.isActiveModule(moduleId, crmAccountId) ? 
                HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> createAccountIdentity(String crmContactId) {
    	List<IdentityInfo> identityInfo = accountService.getAccountIdentitys(crmContactId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> createAccountIdentity(IdentityInfo identity) {
		return new ResponseEntity<>(identityService.createIdentity(identity),HttpStatus.OK);
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getAccountIdentity(String crmAccountId, String crmContactId) {
    	return new ResponseEntity<IdentityInfo>(accountService.getAccountIdentityBaseOnCrmContractId(crmAccountId, crmContactId),HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteAccountIdentity(String crmAccountId, String crmContactId) {
		return new ResponseEntity<Void>(accountService.deleteAccountIdentityBaseOnCrmContractId(crmAccountId, crmContactId)? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
	}
    
}
