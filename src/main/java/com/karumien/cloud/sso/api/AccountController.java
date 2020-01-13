/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.karumien.cloud.sso.api.handler.AccountsApi;
import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.AccountPropertyType;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.ErrorCode;
import com.karumien.cloud.sso.api.model.ErrorData;
import com.karumien.cloud.sso.api.model.ErrorDataCodeCredentials;
import com.karumien.cloud.sso.api.model.ErrorMessage;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityRoleInfo;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.OnBoardingInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;
import com.karumien.cloud.sso.exceptions.PasswordPolicyException;
import com.karumien.cloud.sso.service.AccountService;
import com.karumien.cloud.sso.service.AuthService;
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
    
    @Autowired
    private AuthService authService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AccountInfo> createAccount(AccountInfo account) {
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
    
    @Override
    public ResponseEntity<Void> exists(String compRegNo, String accountNumber) {
                 
        if (StringUtils.hasText(accountNumber)) {
            accountService.getAccount(accountNumber);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        if (StringUtils.hasText(compRegNo)) {
            accountService.getAccountByCompRegNo(compRegNo);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);    
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
    public ResponseEntity<Void> activateAccountModule(String accountNumber, String moduleId) {
        moduleService.activateModules(Arrays.asList(moduleId), Arrays.asList(accountNumber));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> activateAccountModules(String accountNumber, List<String> modules) {
        moduleService.activateModules(modules, Arrays.asList(accountNumber));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deactivateAccountModules(String accountNumber, List<String> modules) {
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
    public ResponseEntity<IdentityInfo> createAccountIdentity(String accountNumber, IdentityInfo identity) {
        //TODO: Access denied when accountNumber != identity.accountNuber
        return new ResponseEntity<>(identityService.createIdentity(identity), HttpStatus.CREATED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityInfo>> getAccountIdentities(String accountNumber, String roleId, List<String> contactNumbers) {
    	return new ResponseEntity<>(accountService.getAccountIdentities(accountNumber, roleId, contactNumbers), HttpStatus.OK);
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
    public ResponseEntity<Void> assignAccountIdentityRole(String accountNumber, String contactNumber, String roleId) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber).getIdentityId(), 
            Arrays.asList(roleId), UpdateType.ADD, null);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignAccountIdentityRoles(String accountNumber, String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber).getIdentityId(), 
            roles, UpdateType.ADD, null);
        return new ResponseEntity<>(HttpStatus.PARTIAL_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> updateAccountIdentityRoles(String accountNumber, String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber).getIdentityId(), 
            roles, UpdateType.UPDATE, accountService.getAccountRolesRepresentation(accountNumber));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignAccountIdentityRoles(String accountNumber, String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber).getIdentityId(), 
            roles, UpdateType.DELETE, null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignAccountIdentityRole(String accountNumber, String contactNumber, String roleId) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber).getIdentityId(), 
            Arrays.asList(roleId), UpdateType.DELETE, null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> getAccountIdentityRole(String accountNumber, String contactNumber, String roleId) {
        return new ResponseEntity<>(identityService.isActiveRole(roleId, contactNumber) ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
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
    public ResponseEntity<List<IdentityRoleInfo>> getAccountIdentitiesRoles(String accountNumber, List<String> contactNumbers) {
        return new ResponseEntity<>(accountService.getAccountIdentitiesRoles(accountNumber, contactNumbers), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getAccountIdentityRoleIds(String accountNumber, String contactNumber) {
        getAccountIdentity(accountNumber, contactNumber);
        
        // TODO: https://jira.eurowag.com/browse/P572-313
        return new ResponseEntity<>(roleService.getIdentityRoles(contactNumber).stream()
                .filter(k -> accountService.getAccountRoles(accountNumber).stream()
                        .map(r -> r.getRoleId()).collect(Collectors.toSet())
                            .contains(k)).collect(Collectors.toList()), HttpStatus.OK);
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ResponseEntity<Void> createIdentityCredentials(String accountNumber, String contactNumber, Credentials credentials) {
        getAccountIdentity(accountNumber, contactNumber);
        try {
            identityService.createIdentityCredentials(contactNumber, credentials);
        } catch (PasswordPolicyException e) {            
            return new ResponseEntity(new ErrorMessage().errcode(ErrorCode.ERROR).errno(300)
                .errmsg("Password is not accepted by Password Policy")
                .errdata(Arrays.asList(new ErrorData()
                    .description(authService.getPasswordPolicy().getTranslation())
                    //.description(messageSource.getMessage("error.credentials." + ErrorDataCodeCredentials.PASSWORD.toString(), null, LocaleContextHolder.getLocale()))
                    .code(ErrorDataCodeCredentials.PASSWORD.toString()))), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> updateAccountIdentity(String accountNumber, String contactNumber, IdentityInfo identity) {
        getAccountIdentity(accountNumber, contactNumber);
        return new ResponseEntity<>(identityService.updateIdentity(contactNumber, identity), HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<AccountInfo>> search(String compRegNo, String accountNumber, String name, String contactEmail) {

        Map<AccountPropertyType, String> searchFilter = new HashMap<>();
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_COMP_REG_NO, compRegNo);
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_ACCOUNT_NAME, name);
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_CONTACT_EMAIL, contactEmail);
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_ACCOUNT_NUMBER, accountNumber);
        
        if (searchFilter.isEmpty()) {
            new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        
        List<AccountInfo> found = accountService.search(searchFilter);
        return CollectionUtils.isEmpty(found) ? new ResponseEntity<>(HttpStatus.GONE) : new ResponseEntity<>(found, HttpStatus.OK);
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<OnBoardingInfo>> getOnboardingExport(@Valid List<String> contactNumbers) {
        
        List<OnBoardingInfo> found = new ArrayList<>();        
        for (IdentityInfo identity : identityService.getIdentities(contactNumbers)) {
            OnBoardingInfo info = new OnBoardingInfo();
            info.setIdentity(identity);
            if (StringUtils.hasText(identity.getAccountNumber())) {
                try {
                    // TODO: assigned not effective
                    info.setRoles(roleService.getIdentityRoles(identity.getContactNumber()));
                    info.setAccount(accountService.getAccount(identity.getAccountNumber()));
                } catch (AccountNotFoundException e) {
                }
            }
            found.add(info);
        }

        return CollectionUtils.isEmpty(found) ? new ResponseEntity<>(HttpStatus.GONE) : new ResponseEntity<>(found, HttpStatus.OK); 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityInfo>> onboarding(@Valid List<OnBoardingInfo> onBoardingInfos) {

        List<IdentityInfo> found = new ArrayList<>();
        
        for (OnBoardingInfo onBoardingInfo : onBoardingInfos) {
        
            try {
                // account
                if (onBoardingInfo.getAccount() != null) {
                    if (accountService.findGroup(onBoardingInfo.getAccount().getAccountNumber()).isPresent()) {                        
                        // TODO: accountService.update
                        if (onBoardingInfo.isOverwriteAccount()) {
                        }
                    } else {
                        accountService.createAccount(onBoardingInfo.getAccount());
                    }
                }
                
                // identity
                if (onBoardingInfo.getIdentity() != null) {
    
                    Optional<UserRepresentation> identity = Optional.empty();
                    
                    if (StringUtils.hasText(onBoardingInfo.getIdentity().getNav4Id())) {
                        identity = identityService.findIdentityNav4(onBoardingInfo.getIdentity().getNav4Id());
                    } else {
                        identity = identityService.findIdentity(onBoardingInfo.getIdentity().getContactNumber());
                    }
                    
                    IdentityInfo identityInfo = null;
                    
                    if (identity.isPresent()) {
                        
                        if (onBoardingInfo.isOverwriteIdentity()) {
                            identityInfo = identityService.updateIdentity(onBoardingInfo.getIdentity().getContactNumber(), onBoardingInfo.getIdentity());
                        }
                        
                        if (!CollectionUtils.isEmpty(onBoardingInfo.getRoles()) && onBoardingInfo.isOverwriteRoles()) {
                            identityService.updateRolesOfIdentity(
                                identityInfo.getIdentityId(), onBoardingInfo.getRoles(), UpdateType.UPDATE, null);
                        }
                        
                    } else {
                        identityInfo = identityService.createIdentity(onBoardingInfo.getIdentity());
                        if (!CollectionUtils.isEmpty(onBoardingInfo.getRoles())) {
                            identityService.updateRolesOfIdentity(
                                identityInfo.getIdentityId(), onBoardingInfo.getRoles(), UpdateType.ADD, null);
                        }
                    }   
                    
                    if (identityInfo != null && onBoardingInfo.getCredentials() != null) {
                        if (identityInfo.getNav4Id() != null) {
                            identityService.createIdentityCredentialsNav4(identityInfo.getNav4Id(), onBoardingInfo.getCredentials());
                        } else {
                            identityService.createIdentityCredentials(identityInfo.getContactNumber(), onBoardingInfo.getCredentials());
                        }
                    }
                    
                    found.add(identityService.getIdentity(onBoardingInfo.getIdentity().getContactNumber()));
                                    
                }
                
            } catch (Exception e) {
                log.warn("Error import " + onBoardingInfo, e);
            }
        }
        
        return CollectionUtils.isEmpty(found) ? new ResponseEntity<>(HttpStatus.GONE) : new ResponseEntity<>(found, HttpStatus.OK); 
    }
    
}