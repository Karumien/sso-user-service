/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.Policy;

/**
 * Service provides scenarios for Identity's management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface IdentityService {

    String ATTR_CRM_CONTACT_ID = "crmContactId";
    String ATTR_CONTACT_EMAIL = "contactEmail";
    String ATTR_PHONE = "phone";
    
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
     * @param crmContactId
     *            unique Identity CRM ID
     */
    void deleteIdentity(String crmContactId);

    /**
     * Returns configuration of Password Policy
     * 
     * @return {@link Policy} configuration of Password Policy
     */
    Policy getPasswordPolicy();

    /**
     * Create/update Identity credentials
     * 
     * @param crmContactId
     *            unique Identity CRM ID
     * @param credentials
     *            new credentials for Identity
     */
    void createIdentityCredentials(String crmContactId, Credentials credentials);

    /**
     * Return base information about Identity by {@code crmContactId}.
     * 
     * @param crmContactId
     *            unique Identity CRM ID
     * @return {@link IdentityInfo}
     */
    IdentityInfo getIdentity(String crmContactId);

    /**
     * Impersonate Identity by specified {@code id}.
     * 
     * @param crmContactId
     *            unique Identity CRM ID
     */
    void impersonateIdentity(String crmContactId);

    /**
     * Logout all sessions of Identity by specified {@code crmContactId}.
     * 
     * @param crmContactId
     *            unique Identity CRM ID
     */
    void logoutIdentity(String crmContactId);

    /**
     * Check exist Identity by username.
     * 
     * @param username
     *            username for serach
     * @return boolean {@code true} when Identity with given username exists
     */
    boolean isIdentityExists(String username);
}
