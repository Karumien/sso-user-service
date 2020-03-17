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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.entity.AccountEntity;
import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.AccountPropertyType;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityPropertyType;
import com.karumien.cloud.sso.api.model.IdentityRoleInfo;
import com.karumien.cloud.sso.api.model.IdentityState;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.api.repository.AccountEntityRepository;
import com.karumien.cloud.sso.exceptions.AccountDeleteException;
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

    @Autowired
    private IdentityService identityService;
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private RoleService roleService;

    @Autowired
    private GroupService groupService;
    
    @Autowired
    private AccountEntityRepository accountEntityRepository;
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AccountInfo createAccount(AccountInfo account) {
        
        try {
            
            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setAccountNumber(account.getAccountNumber());
            accountEntity.setName(account.getName());
            accountEntity.setCompRegNo(account.getCompRegNo());
            accountEntity.setNote(account.getNote());
            accountEntity.setContactEmail(account.getContactEmail());
            
            return mapping(accountEntityRepository.save(accountEntity));
        } catch (DuplicateKeyException e) {
            throw new AccountDuplicateException(account.getAccountNumber());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AccountInfo getAccount(String accountNumber) {
        return mapping(findAccount(accountNumber).orElseThrow(() -> new AccountNotFoundException(accountNumber)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AccountEntity> findAccount(String accountNumber) {
        return accountEntityRepository.findById(accountNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AccountInfo getAccountByCompRegNo(String compRegNo) {
        return mapping(accountEntityRepository.findByCompRegNo(compRegNo).orElseThrow(() -> new AccountNotFoundException("compReqgNo = " + compRegNo)));
    }

    private AccountInfo mapping(AccountEntity accountEntity) {

        // TODO viliam: Orica        
        AccountInfo accountInfo = new AccountInfo ();
        accountInfo.setAccountNumber(accountEntity.getAccountNumber());
        accountInfo.setName(accountEntity.getName());
        accountInfo.setCompRegNo(accountEntity.getCompRegNo());
        accountInfo.setNote(accountEntity.getNote());
        accountInfo.setContactEmail(accountEntity.getContactEmail());
        accountInfo.setLocale(StringUtils.isEmpty(accountEntity.getLocale()) ? "en" : accountEntity.getLocale());
        return accountInfo;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAccount(String accountNumber) {
        if (!searchService.findUserIdsByAttribute(IdentityPropertyType.ATTR_ACCOUNT_NUMBER, accountNumber).isEmpty()) {
            throw new AccountDeleteException(accountNumber);
        }        
        accountEntityRepository.deleteById(accountNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountInfo> getAccounts() {        
        return accountEntityRepository.findAll().stream()
            .map(g -> mapping(g))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<IdentityInfo> getAccountIdentities(String accountNumber, String roleId, List<String> contactNumbers) {

        List<String> userIds = null;
        
        // add all identities from account when no filter specified
        if (CollectionUtils.isEmpty(contactNumbers)) {
            userIds = searchService.findUserIdsByAttribute(IdentityPropertyType.ATTR_ACCOUNT_NUMBER, accountNumber);
        } else { 
            userIds = contactNumbers.stream()
                .map(contactNumber -> searchService.findUserIdsByAttribute(IdentityPropertyType.ATTR_CONTACT_NUMBER, contactNumber))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        }
                
        List<IdentityInfo> identities = userIds.stream()
            .map(identityId -> identityService.findUserRepresentationById(identityId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(u -> searchService.getSimpleAttribute(u.getAttributes(), IdentityService.ATTR_ACCOUNT_NUMBER).isPresent()
                 && searchService.getSimpleAttribute(u.getAttributes(), IdentityService.ATTR_ACCOUNT_NUMBER).get().equals(accountNumber))
            .filter(u -> !StringUtils.hasText(roleId) || roleService.getIdentityRoles(u).contains(roleId))
            .map(user -> identityService.mapping(user))
            .collect(Collectors.toList());
        
        return identities;
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	@Transactional(readOnly = true)	
	public boolean deleteAccountIdentity(String accountNumber, String contactNumber) {
	    getAccountIdentity(accountNumber, contactNumber);	    
		identityService.deleteIdentity(contactNumber);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)    
	public List<RoleInfo> getAccountRoles(String accountNumber) {
	    getAccount(accountNumber);
	    return groupService.getAccountRoles(accountNumber);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)    
	public List<RoleRepresentation> getAccountRolesRepresentation(String accountNumber) {
	    getAccount(accountNumber);
        return groupService.getAccountRolesRepresentation(accountNumber);
	}
	
    /**
     * {@inheritDoc}
     */
	@Override
	public List<String> getAccountRightsOfIdentity(String contactNumber) {
        return groupService.getAccountRightsOfIdentity(contactNumber);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)    
	public List<IdentityRoleInfo> getAccountIdentitiesRoles(String accountNumber, List<String> contactNumbers) {
	    
        // TODO: https://jira.eurowag.com/browse/P572-313
	    Set<String> accountRoles = getAccountRoles(accountNumber).stream().map(r -> r.getRoleId()).collect(Collectors.toSet());
	        
        // TODO: performance - optimize by DB?
	    List<IdentityRoleInfo> roles = new ArrayList<>();
	    for (IdentityInfo info : getAccountIdentities(accountNumber, null, contactNumbers)) {
	        IdentityRoleInfo role = new IdentityRoleInfo();
	        role.setAccountNumber(info.getAccountNumber());
	        role.setContactNumber(info.getContactNumber());
	        role.setNav4Id(info.getNav4Id());
	        role.setRoles(roleService.getIdentityRoles(info.getContactNumber()).stream()
	                .filter(k -> accountRoles.contains(k)).collect(Collectors.toList()));
	        if (info.isLocked()) {
	            role.setLocked(true);
	        }
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
        found = mappingIds(searchService.findAccountIdsByAttribute(firstKey, searchFilter.remove(firstKey)));
        
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
        return accountIds.stream()
            .map(accountNumber -> findAccount(accountNumber))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(accountEntity -> mapping(accountEntity))
            .collect(Collectors.toList());
    }
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)    
	public IdentityState getIdentityState(String accountNumber, String contactNumber) {
	    getAccount(accountNumber);
	    return identityService.getIdentityState(contactNumber);
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
    public List<ModuleInfo> getAccountHierarchy(String accountNumber) {
        return groupService.getAccountHierarchy(accountNumber);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true) 
    public List<String> getAccountIdentitiesLocales(String accountNumber) {
        return accountEntityRepository.getLocales(accountNumber);
    }
	
}
