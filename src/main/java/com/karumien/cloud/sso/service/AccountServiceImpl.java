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
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.exceptions.AccountDuplicateException;
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;


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

    private GroupRepresentation getMasterGroup() {

        // TODO viliam.litavec: Optimize performance - use stored group id for MASTER_GROUP
        try {
            return keycloak.realm(realm).getGroupByPath("/" + MASTER_GROUP);
        } catch (NotFoundException e) {
            // autocreate
            GroupRepresentation newMasterGroup = new GroupRepresentation();
            newMasterGroup.setName(MASTER_GROUP);
            keycloak.realm(realm).groups().add(newMasterGroup);
            return newMasterGroup;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GroupRepresentation> findGroup(String crmAccountId) {
        return getMasterGroup().getSubGroups().stream().filter(g -> g.getAttributes() != null)
            .filter(g -> g.getAttributes().containsKey(ATTR_CRM_ACCOUNT_ID))
            .filter(g -> g.getAttributes().get(ATTR_CRM_ACCOUNT_ID)
            .contains(crmAccountId)).findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<GroupResource> findGroupResource(String crmAccountId) {

        Optional<GroupRepresentation> group = findGroup(crmAccountId);
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
        group.singleAttribute(ATTR_CRM_ACCOUNT_ID, account.getCrmAccountId());

        if (!StringUtils.isEmpty(account.getCompRegNo())) {
            group.singleAttribute(ATTR_COMP_REG_NO, account.getCompRegNo());
        }

        if (!StringUtils.isEmpty(account.getContactEmail())) {
            group.singleAttribute(ATTR_CONTACT_EMAIL, account.getContactEmail());
        }

        getCreatedId(keycloak.realm(realm).groups().group(getMasterGroup().getId()).subGroup(group));

        // TODO: caches?
        keycloak.realm(realm).clearRealmCache();
        return getAccount(account.getCrmAccountId());
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
    public AccountInfo getAccount(String crmAccountId) {
        return mapping(findGroup(crmAccountId).orElseThrow(() -> new AccountNotFoundException(crmAccountId)));
    }

    private AccountInfo mapping(GroupRepresentation group) {

        // TODO viliam: Orica
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setCrmAccountId(group.getAttributes().get(ATTR_CRM_ACCOUNT_ID).stream().findFirst().orElse(null));
        if (group.getAttributes().get(ATTR_COMP_REG_NO) != null) {
            accountInfo.setCompRegNo(group.getAttributes().get(ATTR_COMP_REG_NO).stream().findFirst().orElse(null));
        }
        if (group.getAttributes().get(ATTR_CONTACT_EMAIL) != null) {
            accountInfo.setContactEmail(group.getAttributes().get(ATTR_CONTACT_EMAIL).stream().findFirst().orElse(null));
        }
        accountInfo.setName(group.getName());

        return accountInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAccount(String crmAccountId) {
        keycloak.realm(realm).groups().group(findGroup(crmAccountId).orElseThrow(() -> new AccountNotFoundException(crmAccountId)).getId()).remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountInfo> getAccounts() {
        return getMasterGroup().getSubGroups().stream().filter(g -> g.getAttributes().containsKey(ATTR_CRM_ACCOUNT_ID)).map(g -> mapping(g))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo getAccountIdentityBaseOnCrmContractId(String crmAccountId, String crmContactId) {
        Optional<IdentityInfo> identityFind = getAccountIdentities(crmAccountId, Arrays.asList(crmContactId))
                .stream().filter(identity -> identity.getCrmContactId().equals(crmContactId))
                .findAny();
        return identityFind.orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IdentityInfo> getAccountIdentities(String crmAccountId, List<String> crmContactIds) {
        List<UserRepresentation> users = findGroupResource(crmAccountId)
            .orElseThrow(() -> new AccountNotFoundException(crmAccountId)).members();
        return users.stream().map(user -> identityService.mapping(user)).collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
	@Override
	public boolean deleteAccountIdentityBaseOnCrmContractId(String crmAccountId, String crmContactId) {
		UserResource user = keycloak.realm(realm).users().get(crmContactId);
		user.leaveGroup(findGroup(crmAccountId).get().getId());
		return true;
	}

	@Override
	public boolean checkIfUserNameExist(String username) {
		return keycloak.realm(realm).users().search(username).isEmpty() ? Boolean.FALSE : Boolean.TRUE;		
	}

}
