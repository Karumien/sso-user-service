/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karumien.cloud.sso.api.entity.RebirthEntity;
import com.karumien.cloud.sso.api.handler.AccountsApi;
import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.AccountPropertyType;
import com.karumien.cloud.sso.api.model.AccountState;
import com.karumien.cloud.sso.api.model.ClientRedirect;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.ErrorCode;
import com.karumien.cloud.sso.api.model.ErrorData;
import com.karumien.cloud.sso.api.model.ErrorDataCodeCredentials;
import com.karumien.cloud.sso.api.model.ErrorMessage;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityPropertyType;
import com.karumien.cloud.sso.api.model.IdentityRoleInfo;
import com.karumien.cloud.sso.api.model.IdentityState;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.OnBoardingInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.IdNotFoundException;
import com.karumien.cloud.sso.exceptions.PasswordPolicyException;
import com.karumien.cloud.sso.exceptions.RebirthNotFoundException;
import com.karumien.cloud.sso.exceptions.UnsupportedApiOperationException;
import com.karumien.cloud.sso.service.AccountService;
import com.karumien.cloud.sso.service.AuthService;
import com.karumien.cloud.sso.service.IdentityService;
import com.karumien.cloud.sso.service.ModuleService;
import com.karumien.cloud.sso.service.RebirthService;
import com.karumien.cloud.sso.service.RoleService;
import com.karumien.cloud.sso.service.SearchService;
import com.karumien.cloud.sso.util.PageableUtils;
import com.karumien.cloud.sso.util.TrippleDes;

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

    private static final List<String> DEFAULT_PROPERTIES = 
        Arrays.asList("accountNumber", "name", "compRegNo", "contactEmail", "note", "locale");
    
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
    
    @Autowired
    private RebirthService rebirthService;
    
    @Autowired
    private SearchService searchService;

    @Autowired
    private ObjectMapper mapper;

    @Value("${DB_PASSWORD:admMe123}")
    private String secret;
    
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
    public ResponseEntity<AccountInfo> updateAccount(String accountNumber, AccountInfo accountInfo) {
        return new ResponseEntity<>(accountService.updateAccount(accountNumber, accountInfo, UpdateType.UPDATE), HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AccountInfo> patchAccount(String accountNumber, AccountInfo accountInfo, Boolean cascade) {
    	AccountInfo account = accountService.updateAccount(accountNumber, accountInfo, UpdateType.ADD);

    	// cascade update identity with contactNumber = accountNumber
    	if (Boolean.TRUE.equals(cascade)) {
    		IdentityInfo identity = new IdentityInfo();
    		
    		if (StringUtils.hasText(accountInfo.getContactEmail())) {
    			identity.setEmail(accountInfo.getContactEmail());
    		}


    		if (StringUtils.hasText(accountInfo.getName())) {
    			identity.setFirstName(accountInfo.getName());
    			identity.setLastName("");
    		}
    		
    		// identity.setLocale(accountInfo.getLocale());
			// identity.setPhone(accountInfo.getContactPhone());
    		
    		patchAccountIdentity(accountNumber, accountNumber, cascade, identity);
    	}
          		      		
    	return new ResponseEntity<>(account, HttpStatus.ACCEPTED);
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
    public ResponseEntity<Void> existsAccount(String compRegNo, String accountNumber) {
                 
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
    public ResponseEntity<IdentityInfo> rebirthIdentityNav4(String nav4Id) {
        
        OnBoardingInfo onboarding;
        try {
            onboarding = getOnboarding(nav4Id, false);
        } catch (IOException e) {
            return new ResponseEntity<IdentityInfo>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ResponseEntity<List<IdentityInfo>> onboardingResponse = onboarding(Arrays.asList(onboarding));
        if (onboardingResponse.getStatusCode() == HttpStatus.CREATED) {
            return new ResponseEntity<IdentityInfo>(onboardingResponse.getBody().get(0), HttpStatus.CREATED);
        }
        
//        "overwriteAccount" : false,
//        "overwriteIdentity" : false,
//        "overwriteRoles" : true,
//        "overwritePassword" : false

        return new ResponseEntity<IdentityInfo>(onboardingResponse.getStatusCode());
    }

    private OnBoardingInfo getOnboarding(String nav4Id, boolean maskPassword) throws IOException {
        RebirthEntity rebirth = rebirthService.getRebirth(nav4Id);
        
        OnBoardingInfo onboarding = mapper.readValue(rebirth.getValue(), OnBoardingInfo.class);
        
        if (StringUtils.hasText(rebirth.getPassword())) {
            Credentials credentials = onboarding.getCredentials();
            if (credentials == null) {
                credentials = new Credentials();
            }
            
            String password;
            try {
                password = new TrippleDes(secret).decrypt(rebirth.getPassword());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            if (StringUtils.hasText(password)) {
                credentials.setPassword(maskPassword ? "*****" : password);
            }
            onboarding.setCredentials(credentials);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        onboarding.setNote((StringUtils.hasText(onboarding.getNote()) ? onboarding.getNote() + ", " : "")
    		+ LocalDateTime.now().format(formatter) + "-RBRTH");
        
        return onboarding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> searchSupportIdentityNav4(String nav4Id) {
        return new ResponseEntity<>(supportIdentityNav4(nav4Id, false), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> applySupportIdentityNav4(String nav4Id) {
        return new ResponseEntity<>(supportIdentityNav4(nav4Id, true), HttpStatus.OK);
    }
    
    
    private IdentityInfo supportIdentityNav4(String nav4Id, boolean apply) {
        
        switch (identityService.getIdentityStateByNav4(nav4Id)) {
            case ACTIVE: 
                IdentityInfo identityA = identityService.getIdentityByNav4(nav4Id, true);
                identityA.setNote(addNote("Identity is " + identityA.getState() + " - no action needed - last successfully customer's login at " 
                        + searchService.getValueByAttributeOfUserId(IdentityPropertyType.ATTR_LAST_LOGIN, identityA.getIdentityId())  
                            + ", customer can use forgotten password flow with username = " + identityA.getUsername(), identityA.getNote()));
                return identityA;

            case CREATED: 
            case CREDENTIALS_CREATED: 
                IdentityInfo identityC = identityService.getIdentityByNav4(nav4Id, false);
                if (apply) {
                    ClientRedirect clientRedirect = new ClientRedirect();
                    clientRedirect.setClientId("clientzone");
                    clientRedirect.setRedirectUri("http://clients.eurowag.com/");
                    identityService.resetPasswordUserActionNav4(nav4Id, clientRedirect);
                    identityC.setNote(addNote("Identity is " + identityC.getState() + " - new reset email sent to "
                        + identityC.getEmail() + ", username = " + identityC.getUsername(), identityC.getNote()));
                } else {
                    identityC.setNote(addNote("Identity is " + identityC.getState() + " - ready for new reset email send to "
                        + identityC.getEmail() + ", username = " + identityC.getUsername(), identityC.getNote()));
                }
                return identityC;

            case NOT_EXISTS:

                try {
                    IdentityInfo identityO = getOnboarding(nav4Id, false).getIdentity();
                    
                    if (apply) {
                    
                        ResponseEntity<IdentityInfo> response = rebirthIdentityNav4(nav4Id);
                        IdentityInfo created = response.getBody();
                        
                        if (created.getState() == IdentityState.CREATED) {

                            IdentityInfo identityCX = identityService.getIdentityByNav4(nav4Id, false);
                            ClientRedirect clientRedirect = new ClientRedirect();
                            clientRedirect.setClientId("clientzone");
                            clientRedirect.setRedirectUri("http://clients.eurowag.com/");
                            identityService.resetPasswordUserActionNav4(nav4Id, clientRedirect);
                            identityCX.setNote(addNote("Identity created by /rebirth function from full NAV4 export, state " + identityCX.getState() + " - new reset email sent to "
                                + identityCX.getEmail() + ", username = " + identityCX.getUsername(), identityCX.getNote()));
                            return identityCX;
                        } 
                        
                        created.setNote(addNote("Identity created by /rebirth function from full NAV4 export - customer can use old known password or use forgotten password flow with username = " + created.getUsername(), created.getNote()));
                        return created;

                    } else {
                        identityO.setNote(addNote("Identity ready for /rebirth from full NAV4 export", identityO.getNote()));
                        return identityO;
                    }

                } catch (IOException e) {
                    
                    IdentityInfo newCustomer = new IdentityInfo();
                    newCustomer.setNav4Id(nav4Id);
                    newCustomer.setState(IdentityState.NOT_EXISTS);
                    newCustomer.setNote("IOException: Problem with /rebirth function - please contact SSO team (@SvobodaMiroslav) for investigate: nav4Id = "+nav4Id);
                    return newCustomer;
                    
                    
                } catch (RebirthNotFoundException e) {
                    
                    IdentityInfo newCustomer = new IdentityInfo();
                    newCustomer.setNav4Id(nav4Id);
                    newCustomer.setState(IdentityState.NOT_EXISTS);
                    newCustomer.setNote("New customer - please contact CRM team (@StedryAdam) for migrate from CRM: nav4Id = "+nav4Id);
                    return newCustomer;
                }
                
            default:
                throw new UnsupportedApiOperationException("Unknown type" + identityService.getIdentityStateByNav4(nav4Id));
        }
        
    }

    private String addNote(String message, String note) {
        return message + (note == null ? "" : "\n" + note);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> undoRebirthIdentityNav4(String nav4Id) {
        // TODO Auto-generated method stub
        return AccountsApi.super.undoRebirthIdentityNav4(nav4Id);
    }
       
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<AccountInfo>> getAccounts(Integer page, Integer size, List<String> sort, String search) {
        return new ResponseEntity<>(
            accountService.getAccounts(search, PageableUtils.getRequest(page, size, sort, DEFAULT_PROPERTIES)), 
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> activateAccountModule(String accountNumber, String moduleId, Boolean applyRoles) {
        moduleService.activateModules(Arrays.asList(moduleId), Arrays.asList(accountNumber), applyRoles);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> hasAccountIdentityCredentials(String accountNumber, String contactNumber) {
    	accountService.getAccount(accountNumber);
    	IdentityState state = identityService.getIdentityState(contactNumber);
        
        return new ResponseEntity<>(
            state == IdentityState.CREDENTIALS_CREATED || state == IdentityState.ACTIVE ?
                HttpStatus.OK : HttpStatus.GONE);  
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> createRebirth(OnBoardingInfo onBoardingInfos) {
        if (!StringUtils.hasText(onBoardingInfos.getIdentity().getNav4Id())) {
            throw new IdNotFoundException("NAV4 ID");
        }

        String password = null;
        
        if (onBoardingInfos.getCredentials() != null) {
            password = onBoardingInfos.getCredentials().getPassword();    
            if (StringUtils.hasText(password)) {
                onBoardingInfos.getCredentials().setPassword(null);
                try {
                    password = new TrippleDes(secret).encrypt(password);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        
        try {
            rebirthService.createRebirth(RebirthEntity.builder()
                .nav4Id(onBoardingInfos.getIdentity().getNav4Id())
                .password(password)
                .value(mapper.writeValueAsString(onBoardingInfos))
                .build());
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);        
        }
        return new ResponseEntity<>(HttpStatus.CREATED);        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<OnBoardingInfo> getRebirthIdentityNav4(String nav4Id) {
        
        OnBoardingInfo onboarding;
        try {
            onboarding = getOnboarding(nav4Id, true);
        } catch (IOException e) {
            return new ResponseEntity<OnBoardingInfo>(HttpStatus.UNPROCESSABLE_ENTITY);
        }
        
        return new ResponseEntity<OnBoardingInfo>(onboarding, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> activateAccountModules(String accountNumber, @Valid List<String> modules, @Valid Boolean applyRoles) {
        moduleService.activateModules(modules, Arrays.asList(accountNumber), applyRoles);
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
    public ResponseEntity<List<IdentityInfo>> getAccountIdentities(String accountNumber, String roleId, List<String> contactNumbers, Boolean loginInfo, Boolean driver) {
    	return new ResponseEntity<>(accountService.getAccountIdentities(accountNumber, roleId, contactNumbers, Boolean.TRUE.equals(loginInfo), driver), HttpStatus.OK);
    }
    
        
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> getAccountIdentity(String accountNumber, String contactNumber, Boolean loginInfo) {
        return new ResponseEntity<>(accountService.getAccountIdentity(accountNumber, contactNumber, Boolean.TRUE.equals(loginInfo)), HttpStatus.OK);
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
            accountService.getAccountIdentity(accountNumber, contactNumber, false).getIdentityId(), 
            Arrays.asList(roleId), UpdateType.ADD, null);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getAccountIdentityLocales(String accountNumber) {
        return new ResponseEntity<>(accountService.getAccountIdentitiesLocales(accountNumber), HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> assignAccountIdentityRoles(String accountNumber, String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber, false).getIdentityId(), 
            roles, UpdateType.ADD, null);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> updateAccountIdentityRoles(String accountNumber, String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber, false).getIdentityId(), 
            roles, UpdateType.UPDATE, accountService.getAccountRolesRepresentation(accountNumber));
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignAccountIdentityRoles(String accountNumber, String contactNumber, List<String> roles) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber, false).getIdentityId(), 
            roles, UpdateType.DELETE, null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> unassignAccountIdentityRole(String accountNumber, String contactNumber, String roleId) {
        identityService.updateRolesOfIdentity(
            accountService.getAccountIdentity(accountNumber, contactNumber, false).getIdentityId(), 
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
     * @deprecated use {@link #getFilteredAccountIdentitiesRoles(String, List)}
     */
    @Deprecated
    @Override
    public ResponseEntity<List<IdentityRoleInfo>> getAccountIdentitiesRoles(String accountNumber, List<String> contactNumbers) {
        return new ResponseEntity<>(accountService.getAccountIdentitiesRoles(accountNumber, contactNumbers), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityRoleInfo>> getFilteredAccountIdentitiesRoles(String accountNumber, List<String> contactNumbers) {
        return new ResponseEntity<>(accountService.getAccountIdentitiesRoles(accountNumber, contactNumbers), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<String>> getAccountIdentityRoleIds(String accountNumber, String contactNumber) {
        getAccountIdentity(accountNumber, contactNumber, false);
        
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
        getAccountIdentity(accountNumber, contactNumber, false);
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
        getAccountIdentity(accountNumber, contactNumber, false);
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
        getAccountIdentity(accountNumber, contactNumber, false);
        return new ResponseEntity<>(identityService.updateIdentity(contactNumber, identity, UpdateType.UPDATE), HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityInfo> patchAccountIdentity(String accountNumber, String contactNumber, Boolean cascade, IdentityInfo identity) {
        getAccountIdentity(accountNumber, contactNumber, false);
        return new ResponseEntity<>(identityService.updateIdentity(contactNumber, identity, 
    		Boolean.TRUE.equals(cascade) ? UpdateType.ADD_CASCADE : UpdateType.ADD), 
    		HttpStatus.ACCEPTED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<AccountInfo>> searchAccount(String accountNumber, String compRegNo, String name, String contactEmail, String note) {

        Map<AccountPropertyType, String> searchFilter = new HashMap<>();
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_COMP_REG_NO, compRegNo);
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_ACCOUNT_NAME, name);
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_CONTACT_EMAIL, contactEmail);
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_ACCOUNT_NUMBER, accountNumber);
        accountService.putIfPresent(searchFilter, AccountPropertyType.ATTR_NOTE, note);
        
        if (searchFilter.isEmpty()) {
           return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        
        List<AccountInfo> found = accountService.search(searchFilter);
        return CollectionUtils.isEmpty(found) ? new ResponseEntity<>(HttpStatus.GONE) : new ResponseEntity<>(found, HttpStatus.OK);
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<IdentityState> getIdentityState(String accountNumber, String contactNumber) {
        return new ResponseEntity<>(accountService.getIdentityState(accountNumber, contactNumber), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<IdentityInfo>> onboarding(@Valid List<OnBoardingInfo> onBoardingInfos) {

        List<IdentityInfo> found = new ArrayList<>();
        
        for (OnBoardingInfo onBoardingInfo : onBoardingInfos) {
        
            try {
                // note
                if (!StringUtils.isEmpty(onBoardingInfo.getNote())) {
                    MDC.put("note_full", onBoardingInfo.getNote());
                }
                
                // account
                if (onBoardingInfo.getAccount() != null) {

                    MDC.put("accountNumber", onBoardingInfo.getAccount().getAccountNumber());

                    // notes
                    if (StringUtils.isEmpty(onBoardingInfo.getAccount().getNote())) {
                        onBoardingInfo.getAccount().setNote(onBoardingInfo.getNote());
                    }
                    
                    if (accountService.findAccount(onBoardingInfo.getAccount().getAccountNumber()).isPresent()) {                        
                        // TODO: accountService.update
                        if (onBoardingInfo.isOverwriteAccount()) {
                        }
                    } else {
                        accountService.createAccount(onBoardingInfo.getAccount());
                    }
                }
                
                // identity
                if (onBoardingInfo.getIdentity() != null) {
                    
                    if (onBoardingInfo.getIdentity().getAccountNumber() == null && onBoardingInfo.getAccount() != null) {
                        onBoardingInfo.getIdentity().setAccountNumber(onBoardingInfo.getAccount().getAccountNumber());
                    }
                    
                    MDC.put("accountNumber", onBoardingInfo.getIdentity().getAccountNumber());
                    MDC.put("contactNumber", onBoardingInfo.getIdentity().getContactNumber());

                    // notes
                    if (StringUtils.isEmpty(onBoardingInfo.getIdentity().getNote())) {
                        onBoardingInfo.getIdentity().setNote(onBoardingInfo.getNote());
                    }

                    Optional<UserRepresentation> identity = Optional.empty();
                    
                    if (StringUtils.hasText(onBoardingInfo.getIdentity().getNav4Id())) {
                        MDC.put("nav4Id", onBoardingInfo.getIdentity().getNav4Id());                        
                        identity = identityService.findIdentityNav4(onBoardingInfo.getIdentity().getNav4Id());
                    } else {
                        identity = identityService.findIdentity(onBoardingInfo.getIdentity().getContactNumber());
                    }
                    
                    IdentityInfo identityInfo = null;
                    
                    if (identity.isPresent()) {
                        MDC.put("identityId", identity.get().getId());

                        if (onBoardingInfo.isOverwriteIdentity()) {
                            identityInfo = identityService.updateIdentity(onBoardingInfo.getIdentity().getContactNumber(), 
                                onBoardingInfo.getIdentity(), UpdateType.UPDATE);
                        } else {
                            identityInfo = identityService.mapping(identity.get(), false);
                        }
                        
                        if (!CollectionUtils.isEmpty(onBoardingInfo.getRoles()) && onBoardingInfo.isOverwriteRoles()) {
                            identityService.updateRolesOfIdentity(
                                identityInfo.getIdentityId(), onBoardingInfo.getRoles(), UpdateType.ADD, null);
                        }
                        
                    } else {
                        identityInfo = identityService.createIdentity(onBoardingInfo.getIdentity());
                        if (!CollectionUtils.isEmpty(onBoardingInfo.getRoles())) {
                            identityService.updateRolesOfIdentity(
                                identityInfo.getIdentityId(), onBoardingInfo.getRoles(), UpdateType.ADD, null);
                        }
                    }   
                    
                    try {
                        if ((!identity.isPresent() || identity.isPresent() && onBoardingInfo.isOverwritePassword()) 
                                && identityInfo != null && onBoardingInfo.getCredentials() != null) {
                            if (StringUtils.hasText(onBoardingInfo.getIdentity().getNav4Id())) {
                                identityService.createIdentityCredentialsNav4(onBoardingInfo.getIdentity().getNav4Id(), onBoardingInfo.getCredentials());
                            } else {
                                identityService.createIdentityCredentials(onBoardingInfo.getIdentity().getContactNumber(), onBoardingInfo.getCredentials());
                            }
                            identityInfo.setState(IdentityState.CREDENTIALS_CREATED);
                        }
                    } finally {
                        found.add(identityInfo);
                    }
                }
                
            } catch (Exception e) {
                if (found.size() == 1) {
                    if (e instanceof RuntimeException) {
                       throw e;
                    } else {
                       return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);                  
                    }
                }
                log.warn("Error import " + onBoardingInfo, e);                    
            }
        }
        
        return CollectionUtils.isEmpty(found) ? new ResponseEntity<>(HttpStatus.GONE) : new ResponseEntity<>(found, HttpStatus.CREATED); 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AccountState> getAccountState(String accountNumber) {
        return new ResponseEntity<>(accountService.getAccountState(accountNumber), HttpStatus.OK);
    }
    
}