/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.util.List;

import org.keycloak.representations.idm.RoleRepresentation;

import com.karumien.cloud.sso.api.model.AccountPropertyType;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;

/**
 * Service provides scenarios for Account's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface GroupService {

    String MASTER_GROUP = "Accounts";
    String SELFCARE_GROUP = "SelfCare";

    String ATTR_MODULE_ID = AccountPropertyType.ATTR_MODULE_ID.getValue();
    String ATTR_RIGHT_GROUP_ID= AccountPropertyType.ATTR_RIGHT_GROUP_ID.getValue();
    String ATTR_SERVICE_ID = AccountPropertyType.ATTR_SERVICE_ID.getValue();

    String ATTR_BUSINESS_PRIORITY = AccountPropertyType.ATTR_BUSINESS_PRIORITY.getValue();

    /**
     * Returns hierarchy information filtered by buyed services.
     * 
     * @param accountNumber
     *            Account CRM ID
     * @return {@link List} of {@link ModuleInfo} with right groups definitions
     */
    List<ModuleInfo> getAccountHierarchy(String accountNumber);

    List<RoleInfo> getAccountRoles(String accountNumber);

    List<RoleRepresentation> getAccountRolesRepresentation(String accountNumber);

    List<String> getAccountRightsOfIdentity(String contactNumber);


}
