/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.List;
import java.util.Optional;

import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.representations.idm.GroupRepresentation;

import com.karumien.cloud.sso.api.model.AccountInfo;

/**
 * Service provides scenarios for Account's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface AccountService {

    String MASTER_GROUP = "Accounts";

    String ATTR_COMP_REG_NO = "compRegNo";
    String ATTR_CRM_ACCOUNT_ID = "crmAccountId";
    String ATTR_CONTACT_EMAIL = "contactEmail";
        
    AccountInfo createAccount(AccountInfo account);

    AccountInfo getAccount(String crmAccountId);

    void deleteAccount(String crmAccountId);

    List<AccountInfo> getAccounts();

    Optional<GroupRepresentation> findGroup(String crmAccountId);
        
    Optional<GroupResource> findGroupResource(String crmAccountId);
}
