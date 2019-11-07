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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RightGroup;
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
        String groupId = searchService.findGroupIdsByAttribute(ATTR_ACCOUNT_NUMBER, accountNumber).stream().findFirst().orElse(null);
        return Optional.ofNullable(groupId == null ? null : keycloak.realm(realm).groups().group(groupId).toRepresentation());
//
//        
//        return getMasterGroup().getSubGroups().stream()
//                .filter(g -> searchService.containsAttribute(g.getAttributes(), ATTR_ACCOUNT_NUMBER, accountNumber)).findFirst();
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
        group.setName(account.getName());
        group.setPath("/" + MASTER_GROUP + "/" + group.getName());
        group.singleAttribute(ATTR_ACCOUNT_NUMBER, account.getAccountNumber());

        if (!StringUtils.isEmpty(account.getCompRegNo())) {
            group.singleAttribute(ATTR_COMP_REG_NO, account.getCompRegNo());
        }

        if (!StringUtils.isEmpty(account.getContactEmail())) {
            group.singleAttribute(ATTR_CONTACT_EMAIL, account.getContactEmail());
        }

        getCreatedId(keycloak.realm(realm).groups().group(getMasterGroup(MASTER_GROUP).getId()).subGroup(group));

        // TODO: caches?
        keycloak.realm(realm).clearRealmCache();
        return getAccount(account.getAccountNumber());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ModuleInfo> getAccountHierarchy(String accountNumber) {
        //AccountInfo accountInfo = getAccount(accountNumber);
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

    private AccountInfo mapping(GroupRepresentation group) {

        // TODO viliam: Orica
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountNumber(searchService.getSimpleAttribute(group.getAttributes(), ATTR_ACCOUNT_NUMBER).orElse(null));
        accountInfo.setCompRegNo(searchService.getSimpleAttribute(group.getAttributes(), ATTR_COMP_REG_NO).orElse(null));
        accountInfo.setContactEmail(searchService.getSimpleAttribute(group.getAttributes(), ATTR_CONTACT_EMAIL).orElse(null));
        accountInfo.setName(group.getName());

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
    public IdentityInfo getAccountIdentityBaseOnCrmContractId(String accountNumber, String contactNumber) {
        Optional<IdentityInfo> identityFind = getAccountIdentities(accountNumber, Arrays.asList(contactNumber))
                .stream().filter(identity -> identity.getContactNumber().equals(contactNumber))
                .findAny();
        return identityFind.orElseThrow(() -> new IdentityNotFoundException(contactNumber));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IdentityInfo> getAccountIdentities(String accountNumber, List<String> contactNumbers) {
        List<UserRepresentation> users = findGroupResource(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException(accountNumber)).members();
        return users.stream()
                .filter(u -> CollectionUtils.isEmpty(contactNumbers) 
                    || searchService.getSimpleAttribute(u.getAttributes(), IdentityService.ATTR_CONTACT_NUMBER).isPresent()
                      && contactNumbers.contains(searchService.getSimpleAttribute(u.getAttributes(), IdentityService.ATTR_CONTACT_NUMBER).get()))
                .map(user -> identityService.mapping(user)).collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public boolean deleteAccountIdentityBaseOnCrmContractId(String accountNumber, String contactNumber) {
	    IdentityInfo identityInfo = getAccountIdentityBaseOnCrmContractId(accountNumber, contactNumber);	    
		identityService.deleteIdentity(identityInfo.getContactNumber());
		return true;
	}

	@Override
	public boolean checkIfUserNameExist(String username) {
		return keycloak.realm(realm).users().search(username).isEmpty() ? Boolean.FALSE : Boolean.TRUE;		
	}

}
