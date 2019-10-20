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

import org.keycloak.representations.idm.UserRepresentation;

import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;

/**
 * Service provides scenarios for Identity's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface IdentityService {

    String ATTR_CRM_CONTACT_ID = "contactNumber";

    String ATTR_CRM_ACCOUNT_ID = "accountNumber";

    String ATTR_GLOBAL_EMAIL = "globalEmail";

    String ATTR_DRIVER_PIN = "driverPin";

    String ATTR_BINARY_RIGHTS = "binaryRights";

    String ATTR_PHONE = "phone";

    String ATTR_LOCALE = "locale";

    String ATTR_NAV4ID = "nav4Id";

    String ATTR_BUSINESS_PRIORITY = "businessPriority";

    /**
     * Create Identity in target SSO.
     * 
     * @param identity
     *            Identity specification
     * @return {@link IdentityInfo} changes after entity save
     */
    IdentityInfo createIdentity(IdentityInfo identity);

    /**
     * Delete Identity in target SSO.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     */
    void deleteIdentity(String contactNumber);
    
    /**
     * Create/update Identity credentials
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @param credentials
     *            new credentials for Identity
     */
    void createIdentityCredentials(String contactNumber, Credentials credentials);

    /**
     * Return base information about Identity by {@code contactNumber}.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @return {@link IdentityInfo}
     */
    IdentityInfo getIdentity(String contactNumber);

    /**
     * Impersonate Identity by specified {@code id}.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     */
    void impersonateIdentity(String contactNumber);

    /**
     * Logout all sessions of Identity by specified {@code contactNumber}.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     */
    void logoutIdentity(String contactNumber);

    /**
     * Check exist Identity by username.
     * 
     * @param username
     *            username for serach
     * @return boolean {@code true} when Identity with given username exists
     */
    boolean isIdentityExists(String username);

    /**
     * Assign selected list of roles to as current Identity
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @param roles
     *            {@link List} {@link String} list of ids of roles we want to add to identity
     * @return {@link Boolean} return true if we successfully assign roles false if not
     */
    boolean assignRolesToIdentity(String contactNumber, @Valid List<String> roles);

    /**
     * Remove selected list of roles to as current Identity.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @param roles
     *            {@link List} {@link String} list of ids of roles we want to remove from identity
     * @return {@link Boolean} return true if we successfully remove roles false if not
     */
    boolean unassignRolesToIdentity(String contactNumber, @Valid List<String> roles);

    /**
     * Return all roles that are assigned to selected identity.
     * 
     * @param contactNumber
     *            unique Identity CRM ID we want to find roles for
     * @return {@link List} {@link RoleInfo} list of roles that identity have
     */
    List<RoleInfo> getAllIdentityRoles(String contactNumber);

    /**
     * Function that save pin of Identity driver base on input.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @param pin
     *            {@link DriverPin} pin we want to add to the driver identity
     */
    void savePinOfIdentityDriver(String contactNumber, DriverPin pin);

    /**
     * Function that remove pin of Identity driver.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     */
    void removePinOfIdentityDriver(String contactNumber);

    /**
     * Initiate action for email change.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     */
    void resetPasswordByEmail(String contactNumber);

    Optional<UserRepresentation> findIdentity(String contactNumber);

    /**
     * Block/unblock Ientity.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @param blockedStatus
     *            new blocked status
     */
    void blockIdentity(String contactNumber, boolean blockedStatus);

    /**
     * Return PIN of Identity type Driver.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @return {@link DriverPin} pin of the driver
     */
    DriverPin getPinOfIdentityDriver(String contactNumber);

    boolean isActiveRole(String roleId, String contactNumber);

    void refreshBinaryRoles(UserRepresentation userRepresentation);

    IdentityInfo mapping(UserRepresentation userRepresentation);

    /**
     * Return identity base on nav4Id from request parameter
     * @param nav4Id {@link String} nav4Id id of identity we want to find
     * @return {@link IdentityInfo} Identity we want to get
     */
	IdentityInfo getIdentityByNav4(String nav4Id);

    List<String> getUserRequiredActions(String username);
}
