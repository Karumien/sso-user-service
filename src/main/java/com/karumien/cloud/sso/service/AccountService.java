/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.UpdateType;
import com.karumien.cloud.sso.api.entity.AccountEntity;
import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.AccountPropertyType;
import com.karumien.cloud.sso.api.model.AccountState;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityRoleInfo;
import com.karumien.cloud.sso.api.model.IdentityState;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;

/**
 * Service provides scenarios for Account's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface AccountService {

    AccountInfo createAccount(AccountInfo account);

    AccountInfo getAccount(String accountNumber);

    AccountInfo getAccountByCompRegNo(String compRegNo);

    void deleteAccount(String accountNumber);

    List<AccountInfo> getAccounts(String search, Pageable pageable);


    /**
     * Return all identidities that are under this account
     * 
     * @param accountNumber
     *            Account CRM ID
     * @param roleId
     *            Filtered by roleId
     * @param contactNumbers
     *            IDs of identities for filter
     * @param withIdentityInfo
     *            attach informations about login
     *            
     * @return {@link List} {@link IdentityInfo} list of identities
     */
    List<IdentityInfo> getAccountIdentities(String accountNumber, String roleId, List<String> contactNumber, boolean withLoginInfo);

    /**
     * Return {@link IdentityInfo} for account and contact ID
     * 
     * @param accountNumber
     *            Account CRM ID
     * @param contactNumber
     *            Contact CRM ID
     * @param withIdentityInfo
     *            attach informations about login
     * @return {@link IdentityInfo} identity info for selected crmContractId
     */
    IdentityInfo getAccountIdentity(String accountNumber, String contactNumber, boolean withIdentityInfo);

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

    List<IdentityRoleInfo> getAccountIdentitiesRoles(String accountNumber, List<String> contactNumbers);

    List<AccountInfo> search(Map<AccountPropertyType, String> searchFilter);

    default void putIfPresent(Map<AccountPropertyType, String> search, AccountPropertyType key, String value) {
        if (StringUtils.hasText(value)) {
            search.put(key, value);
        }
    }

    List<RoleRepresentation> getAccountRolesRepresentation(String accountNumber);

    IdentityState getIdentityState(String accountNumber, String contactNumber);

    Optional<AccountEntity> findAccount(String accountNumber);

    List<String> getAccountIdentitiesLocales(String accountNumber);

    List<String> getAccountIdentitiesIds(String accountNumber, List<String> contactNumbers);

    AccountState getAccountState(String accountNumber);

    AccountInfo updateAccount(String accountNumber, AccountInfo accountInfo, UpdateType update);
}
