/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.representations.idm.GroupRepresentation;

import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityRoleInfo;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;

/**
 * Service provides scenarios for Account's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface AccountService {

    String MASTER_GROUP = "Accounts";

    String SELFCARE_GROUP = "SelfCare";

    String ATTR_COMP_REG_NO = "compRegNo";

    String ATTR_ACCOUNT_NUMBER = "accountNumber";

    String ATTR_CONTACT_EMAIL = "contactEmail";

    String ATTR_MODULE_ID = "moduleId";

    String ATTR_RIGHT_GROUP_ID = "groupId";

    String ATTR_SERVICE_ID = "serviceId";

    String ATTR_BUSINESS_PRIORITY = "businessPriority";

    AccountInfo createAccount(AccountInfo account);

    AccountInfo getAccount(String accountNumber);

    void deleteAccount(String accountNumber);

    List<AccountInfo> getAccounts();

    Optional<GroupRepresentation> findGroup(String accountNumber);

    Optional<GroupResource> findGroupResource(String accountNumber);

    /**
     * Return all identidities that are under this account
     * 
     * @param accountNumber
     *            Account CRM ID
     * @param roleId
     *            Filtered by roleId
     * @param contactNumbers
     *            IDs of identities for filter
     * @return {@link List} {@link IdentityInfo} list of identities
     */
    List<IdentityInfo> getAccountIdentities(String accountNumber, String roleId, List<String> contactNumber);

    /**
     * Return {@link IdentityInfo} for account and contact ID
     * 
     * @param accountNumber
     *            Account CRM ID
     * @param contactNumber
     *            Contact CRM ID
     * @return {@link IdentityInfo} identity info for selected crmContractId
     */
    IdentityInfo getAccountIdentity(String accountNumber, String contactNumber);

    /**
     * Remove identity from account members base on contact Id
     * 
     * @param accountNumber
     *            Account CRM ID
     * @param contactNumber
     *            Contact CRM ID
     * @return {@link Boolean} value if we ware successfull of not
     */
    boolean deleteAccountIdentity(String accountNumber, String contactNumber);

    /**
     * Check if this username is already used for some user
     * 
     * @param username
     *            username to check
     * @return {@link Boolean} return true if user name was already used and false if not
     */
    boolean checkIfUserNameExist(String username);

    /**
     * Returns hierarchy information filtered by buyed services.
     * 
     * @param accountNumber
     *            Account CRM ID
     * @return {@link List} of {@link ModuleInfo} with right groups definitions
     */
    List<ModuleInfo> getAccountHierarchy(String accountNumber);

    /**
     * Return roles for account (global and custom)
     * 
     * @param accountNumber
     *            Account CRM ID
     * @return
     */
    List<RoleInfo> getAccountRoles(String accountNumber);

    List<String> getAccountRightsOfIdentity(String contactNumber);

    List<IdentityRoleInfo> getAccountIdentitiesRoles(String accountNumber, @Valid List<String> contactNumbers);
}
