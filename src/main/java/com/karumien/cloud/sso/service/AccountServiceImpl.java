/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.AccountPropertyType;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityPropertyType;
import com.karumien.cloud.sso.api.model.IdentityRoleInfo;
import com.karumien.cloud.sso.api.model.IdentityState;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RightGroup;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.AccountDuplicateException;
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;


/**
 * Implementation {@link AccountService} for Account Management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 22. 8. 2019 18:59:57
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private IdentityService identityService;
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private RoleService roleService;

    @Autowired
    private LocalizationService localizationService;


    private GroupRepresentation getMasterGroup(String groupKey) {

        // TODO viliam.litavec: Optimize performance - use stored group id for MASTER_GROUP
        try {
            return keycloak.realm(realm).getGroupByPath("/" + groupKey);
        } catch (NotFoundException e) {
            // autocreate
            GroupRepresentation newMasterGroup = new GroupRepresentation();
            newMasterGroup.setName(groupKey);
            keycloak.realm(realm).groups().add(newMasterGroup);
            return newMasterGroup;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GroupRepresentation> findGroup(String accountNumber) {        
        String groupId = searchService.findGroupIdsByAttribute(AccountPropertyType.ATTR_ACCOUNT_NUMBER, accountNumber).stream().findFirst().orElse(null);
        return Optional.ofNullable(groupId == null ? null : keycloak.realm(realm).groups().group(groupId).toRepresentation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GroupRepresentation> findGroupByCompRegNo(String compRegNo) {        
        String groupId = searchService.findGroupIdsByAttribute(AccountPropertyType.ATTR_COMP_REG_NO, compRegNo).stream().findFirst().orElse(null);
        return Optional.ofNullable(groupId == null ? null : keycloak.realm(realm).groups().group(groupId).toRepresentation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GroupResource> findGroupResourceById(String groupId) {
        try {
            return Optional.ofNullable(keycloak.realm(realm).groups().group(groupId));
        } catch (Exception e) {
            System.out.println(e);
        }
        return Optional.empty();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GroupResource> findGroupResource(String accountNumber) {

        Optional<GroupRepresentation> group = findGroup(accountNumber);
        if (!group.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(keycloak.realm(realm).groups().group(group.get().getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountInfo createAccount(AccountInfo account) {

        GroupRepresentation group = new GroupRepresentation();
        group.setName(account.getName() + " (" + account.getAccountNumber() + ")");
        group.setPath("/" + MASTER_GROUP + "/" + group.getName());
        group.singleAttribute(ATTR_ACCOUNT_NUMBER, account.getAccountNumber());
        group.singleAttribute(ATTR_ACCOUNT_NAME, account.getName());

        if (StringUtils.hasText(account.getCompRegNo())) {
            group.singleAttribute(ATTR_COMP_REG_NO, account.getCompRegNo());
        }

        if (StringUtils.hasText(account.getContactEmail())) {
            group.singleAttribute(ATTR_CONTACT_EMAIL, account.getContactEmail().toLowerCase());
        }

        getCreatedId(keycloak.realm(realm).groups().group(getMasterGroup(MASTER_GROUP).getId()).subGroup(group));

        return getAccount(account.getAccountNumber());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ModuleInfo> getAccountHierarchy(String accountNumber) {
        getAccount(accountNumber);
        //TODO: apply buyed services
        return getMasterGroup(SELFCARE_GROUP).getSubGroups().stream()
           .map(g -> mappingModule(g))
           .collect(Collectors.toList());               
    }

    /**
     * {@inheritDoc}
     */
    public String getCreatedId(Response response) {
        URI location = response.getLocation();

        switch (response.getStatusInfo().toEnum()) {
        case CREATED:
            if (location == null) {
                return null;
            }
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        case CONFLICT:
            throw new AccountDuplicateException("Account already exists");
        default:
            throw new UnsupportedOperationException("Unknown status " + response.getStatusInfo().toEnum());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountInfo getAccount(String accountNumber) {
        return mapping(findGroup(accountNumber).orElseThrow(() -> new AccountNotFoundException(accountNumber)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountInfo getAccountByCompRegNo(String compRegNo) {
        return mapping(findGroupByCompRegNo(compRegNo).orElseThrow(() -> new AccountNotFoundException("compReqgNo = " + compRegNo)));
    }

    private AccountInfo mapping(GroupRepresentation group) {

        // TODO viliam: Orica
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountNumber(searchService.getSimpleAttribute(group.getAttributes(), ATTR_ACCOUNT_NUMBER).orElse(null));
        accountInfo.setCompRegNo(searchService.getSimpleAttribute(group.getAttributes(), ATTR_COMP_REG_NO).orElse(null));
        accountInfo.setContactEmail(searchService.getSimpleAttribute(group.getAttributes(), ATTR_CONTACT_EMAIL).orElse(null));
        accountInfo.setName(searchService.getSimpleAttribute(group.getAttributes(), ATTR_ACCOUNT_NAME).orElse(group.getName()));

        return accountInfo;
    }
    
    private ModuleInfo mappingModule(GroupRepresentation group) {

        // TODO viliam: Orica
        ModuleInfo moduleInfo = new ModuleInfo();
        moduleInfo.setName(group.getName());
        moduleInfo.setModuleId(searchService.getSimpleAttribute(group.getAttributes(), ATTR_MODULE_ID).orElse(null));
        String businessPriority = searchService.getSimpleAttribute(group.getAttributes(), ATTR_BUSINESS_PRIORITY).orElse(null);
        if (businessPriority != null) {
            moduleInfo.setBusinessPriority(NumberUtils.parseNumber(businessPriority, Integer.class));
        }
        moduleInfo.setTranslation(localizationService.translate(
                moduleInfo.getModuleId() == null ? null : "module" + "." + moduleInfo.getModuleId().toLowerCase(), 
                        group.getAttributes(), LocaleContextHolder.getLocale(), group.getName()));
        
        moduleInfo.setGroups(group.getSubGroups().stream()
            .map(rg -> mappingRightGroup(rg))
            .collect(Collectors.toList()));
        return moduleInfo;
    }
    
    private RightGroup mappingRightGroup(GroupRepresentation group) {

        // TODO viliam: Orica
        RightGroup rightGroup = new RightGroup();
        rightGroup.setName(group.getName());
        rightGroup.setGroupId(searchService.getSimpleAttribute(group.getAttributes(), ATTR_RIGHT_GROUP_ID).orElse(null));
        rightGroup.setServiceId(searchService.getSimpleAttribute(group.getAttributes(), ATTR_SERVICE_ID).orElse(null));
        String businessPriority = searchService.getSimpleAttribute(group.getAttributes(), ATTR_BUSINESS_PRIORITY).orElse(null);
        if (businessPriority != null) {
            rightGroup.setBusinessPriority(NumberUtils.parseNumber(businessPriority, Integer.class));
        }
        rightGroup.setTranslation(localizationService.translate(
                rightGroup.getGroupId() == null ? null : "group" + "." + rightGroup.getGroupId().toLowerCase(), 
                        group.getAttributes(), LocaleContextHolder.getLocale(), group.getName()));
        
        return rightGroup;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAccount(String accountNumber) {
        keycloak.realm(realm).groups().group(findGroup(accountNumber).orElseThrow(() -> new AccountNotFoundException(accountNumber)).getId()).remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountInfo> getAccounts() {
        return getMasterGroup(MASTER_GROUP).getSubGroups().stream()
                .filter(g -> g.getAttributes().containsKey(ATTR_ACCOUNT_NUMBER))
                .map(g -> mapping(g))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo getAccountIdentity(String accountNumber, String contactNumber) {
        getAccount(accountNumber);
        IdentityInfo identity = identityService.getIdentity(contactNumber);
        if (accountNumber != null && accountNumber.equals(identity.getAccountNumber())) {
            return identity;
        }
        throw new IdentityNotFoundException(contactNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IdentityInfo> getAccountIdentities(String accountNumber, String roleId, List<String> contactNumbers) {
        
        // TODO: performance - search over DB (searchService)
        List<UserRepresentation> users = findGroupResource(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber)).members();
        
        List<IdentityInfo> identities = users.stream()
                .filter(u -> CollectionUtils.isEmpty(contactNumbers) 
                    || searchService.getSimpleAttribute(u.getAttributes(), IdentityService.ATTR_CONTACT_NUMBER).isPresent()
                      && contactNumbers.contains(searchService.getSimpleAttribute(u.getAttributes(), IdentityService.ATTR_CONTACT_NUMBER).get()))
                .map(user -> identityService.mapping(user))
                .collect(Collectors.toList());
        
        return StringUtils.hasText(roleId) ? identities.stream()
                .filter(i -> roleService.getIdentityRoles(i.getContactNumber()).contains(roleId))
                .collect(Collectors.toList()) : identities;
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public boolean deleteAccountIdentity(String accountNumber, String contactNumber) {
	    getAccountIdentity(accountNumber, contactNumber);	    
		identityService.deleteIdentity(contactNumber);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean checkIfUserNameExist(String username) {
		return searchService.findUserIdsByAttribute(IdentityPropertyType.USERNAME, username).isEmpty() ? Boolean.FALSE : Boolean.TRUE;		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleInfo> getAccountRoles(String accountNumber) {
	    return roleService.getAccountRoles(keycloak.realm(realm).groups().group(getMasterGroup(SELFCARE_GROUP).getId()), false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<RoleRepresentation> getAccountRolesRepresentation(String accountNumber) {
        return roleService.getAccountRolesRepresentation(keycloak.realm(realm).groups().group(getMasterGroup(SELFCARE_GROUP).getId()), false);
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public List<String> getAccountRightsOfIdentity(String contactNumber) {
        return roleService.getIdentityRights(keycloak.realm(realm).groups().group(getMasterGroup(SELFCARE_GROUP).getId()), contactNumber);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IdentityRoleInfo> getAccountIdentitiesRoles(String accountNumber, List<String> contactNumbers) {
	    
        // TODO: https://jira.eurowag.com/browse/P572-313
	    Set<String> accountRoles = getAccountRoles(accountNumber).stream().map(r -> r.getRoleId()).collect(Collectors.toSet());
	        
        // TODO: performance?
	    List<IdentityRoleInfo> roles = new ArrayList<>();
	    for (IdentityInfo info : getAccountIdentities(accountNumber, null, contactNumbers)) {
	        IdentityRoleInfo role = new IdentityRoleInfo();
	        role.setAccountNumber(info.getAccountNumber());
	        role.setContactNumber(info.getContactNumber());
	        role.setNav4Id(info.getNav4Id());
	        role.setRoles(roleService.getIdentityRoles(info.getContactNumber()).stream()
	                .filter(k -> accountRoles.contains(k)).collect(Collectors.toList()));
	        roles.add(role);
	    }
	    return roles;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AccountInfo> search(Map<AccountPropertyType, String> searchFilter) {
	    
        List<AccountInfo> found = new ArrayList<>();
        
        AccountPropertyType firstKey = searchFilter.keySet().stream().findFirst().get();
        found = mappingIds(searchService.findGroupIdsByAttribute(firstKey, searchFilter.remove(firstKey)));
        
        // filter other 
        for (AccountPropertyType key : searchFilter.keySet()) {
            found = found.stream().filter(i -> hasProperty(i, key, searchFilter.get(key))).collect(Collectors.toList());            
        }
        
        return found;
    }

    private boolean hasProperty(AccountInfo a, AccountPropertyType key, String value) {
        switch (key) {
        case ATTR_ACCOUNT_NAME:
            return value.equals(a.getName());
        case ATTR_CONTACT_EMAIL:
            return value.toLowerCase().equals(a.getContactEmail());
        case ATTR_COMP_REG_NO:
            return value.equals(a.getCompRegNo());
        case ATTR_ACCOUNT_NUMBER:
            return value.equals(a.getAccountNumber());
        case ATTR_NOTE:
            return value.equals(a.getNote());
        default:
            return false;
        }
	}

	private List<AccountInfo> mappingIds(List<String> accountIds) {
        return accountIds.stream().map(id -> findGroupResourceById(id))
                .filter(f -> f.isPresent()).map(u -> mapping(u.get().toRepresentation()))
                .collect(Collectors.toList());
    }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public IdentityState getIdentityState(String accountNumber, String contactNumber) {
	    getAccount(accountNumber);
	    return identityService.getIdentityState(contactNumber);
	}
	
}
