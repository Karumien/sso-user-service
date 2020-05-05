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

import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.UpdateType;
import com.karumien.cloud.sso.api.model.ClientRedirect;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.IdentityPropertyType;
import com.karumien.cloud.sso.api.model.IdentityState;
import com.karumien.cloud.sso.api.model.UserActionType;

/**
 * Service provides scenarios for Identity's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface IdentityService {

    String ATTR_CONTACT_NUMBER = IdentityPropertyType.ATTR_CONTACT_NUMBER.getValue();

    String ATTR_ACCOUNT_NUMBER = IdentityPropertyType.ATTR_ACCOUNT_NUMBER.getValue();

    String ATTR_NAV4ID = IdentityPropertyType.ATTR_NAV4ID.getValue();

    String ATTR_NOTE = IdentityPropertyType.ATTR_NOTE.getValue();

    String ATTR_PHONE = IdentityPropertyType.ATTR_PHONE.getValue();

    String ATTR_LOCALE = IdentityPropertyType.ATTR_LOCALE.getValue();

    String ATTR_BINARY_RIGHTS = IdentityPropertyType.ATTR_BINARY_RIGHTS.getValue();

    String ATTR_DRIVER_PIN = IdentityPropertyType.ATTR_DRIVER_PIN.getValue();

    String ATTR_BUSINESS_PRIORITY = IdentityPropertyType.ATTR_BUSINESS_PRIORITY.getValue();

    String ATTR_LAST_LOGIN = IdentityPropertyType.ATTR_LAST_LOGIN.getValue();

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
     * Create/update Identity credentials
     * 
     * @param username
     *            unique Identity username
     * @param credentials
     *            new credentials for Identity
     */
    void createIdentityCredentialsByUsername(String username, Credentials newCredentials);

    /**
     * Return base information about Identity by {@code contactNumber}.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @return {@link IdentityInfo}
     */
    IdentityInfo getIdentity(String contactNumber);

    /**
     * Return base information about Identity by {@code contactNumber}.
     * 
     * @param username
     *            unique Identity username
     * @return {@link IdentityInfo}
     */
    IdentityInfo getIdentityByUsername(String username);

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
     * Add/remove roles of as current Identity.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @param roles
     *            {@link List} {@link String} list of ids of roles we want to be on identity
     * @param updateType
     *            update type
     * @param scope
     *            selected roles for apply, empty means all
     */
    void updateRolesOfIdentity(String identityId, List<String> roles, UpdateType updateType, List<RoleRepresentation> scope);

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
     * Initiate {@link UserActionType#UPDATE_PASSWORD}.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @param clientRedirect
     *            client redirect informtions
     * @param clientRedirect
     */
    void resetPasswordUserAction(String contactNumber, ClientRedirect clientRedirect);

    /**
     * Initiate {@link UserActionType#VERIFY_EMAIL}.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     */
    void changeEmailUserAction(String contactNumber);

    Optional<UserRepresentation> findIdentity(String contactNumber);

    Optional<UserRepresentation> findIdentityNav4(String nav4Id);

    Optional<UserRepresentation> findIdentityByUsername(String username);

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

    IdentityInfo mapping(UserRepresentation userRepresentation);

    /**
     * Return identity base on nav4Id from request parameter
     * 
     * @param nav4Id
     *            {@link String} nav4Id id of identity we want to find
     * @return {@link IdentityInfo} Identity we want to get
     */
    IdentityInfo getIdentityByNav4(String nav4Id);

    /**
     * Returns list of user's required actions.
     * 
     * @param username
     *            unique username
     * @return {@link List} of {@link String} user's required actions
     */
    List<String> getUserRequiredActions(String username);

    /**
     * Create/update Identity credentials
     * 
     * @param nav4Id
     *            unique Identity nav4Id
     * @param credentials
     *            new credentials for Identity
     */
    void createIdentityCredentialsNav4(String nav4Id, Credentials credentials);

    /**
     * Update identity by specified changes.
     * 
     * @param contactNumber
     *            unique Identity CRM ID
     * @param identity
     *            changes
     * @return {@link IdentityInfo} saved identity after changes
     */
    IdentityInfo updateIdentity(String contactNumber, IdentityInfo identity);

    boolean isIdentityTemporaryLocked(String username);

    boolean isActiveRoleNav4(String roleId, String nav4Id);

    void refreshBinaryRoles(UserResource userResource);

    List<IdentityInfo> search(Map<IdentityPropertyType, String> searchFilter);

    Optional<UserRepresentation> findUserRepresentationById(String identityId);

    default void putIfPresent(Map<IdentityPropertyType, String> search, IdentityPropertyType key, String value) {
        if (StringUtils.hasText(value)) {
            search.put(key, value);
        }
    }

    List<IdentityInfo> getIdentities(List<String> contactNumbers);

    IdentityState getIdentityState(String contactNumber);

    IdentityState mappingIdentityState(UserRepresentation userRepresentation);

    IdentityState getIdentityStateByNav4(String nav4Id);

    void resetPasswordUserActionNav4(String nav4Id, ClientRedirect clientRedirect);

    IdentityInfo updateIdentityNav4(String nav4Id, IdentityInfo identity);

    void deleteIdentityNav4(String nav4Id);

}
