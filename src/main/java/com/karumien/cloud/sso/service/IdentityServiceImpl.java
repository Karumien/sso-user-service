/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.net.URI;
import java.util.Optional;

import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.exceptions.IdentityDuplicateException;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;
import com.karumien.cloud.sso.exceptions.PolicyPasswordException;

/**
 * Implementation {@link IdentityService} for identity management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 6. 2019 22:07:27
 */
@Service
public class IdentityServiceImpl implements IdentityService {

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private Keycloak keycloak;

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteIdentity(String crmContactId) {
        keycloak.realm(realm).users().delete(crmContactId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo createIdentity(@Valid IdentityInfo identityInfo) {

        UserRepresentation identity = new UserRepresentation();
        identity.setUsername(Optional.ofNullable(identityInfo.getUsername()).orElse(identityInfo.getEmail()));
        identity.setFirstName(identityInfo.getFirstName());
        identity.setLastName(identityInfo.getLastName());
        identity.setId(identityInfo.getCrmContactId());

        // TODO: extract email as new method
        identity.setEmail(identityInfo.getEmail());
        // TODO: Identity.setEmailVerified(emailVerified);
        identity.setEnabled(true);


        identity.singleAttribute(ATTR_PHONE, identityInfo.getPhone());
        identity.singleAttribute(ATTR_CONTACT_EMAIL, identityInfo.getContactEmail());
        identity.singleAttribute(ATTR_CRM_CONTACT_ID, identityInfo.getCrmContactId());

        //identity.setCredentials(Arrays.asList(credential));

        Response response = keycloak.realm(realm).users().create(identity);

        identityInfo.setCrmContactId(getCreatedId(response));
        identityInfo.setUsername(identity.getUsername());

        return identityInfo;
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
            throw new IdentityDuplicateException("Identity exists");
        default:
            throw new UnsupportedOperationException("Unknown status " + response.getStatusInfo().toEnum());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Policy getPasswordPolicy() {

        String policyDescription = keycloak.realm(realm).toRepresentation().getPasswordPolicy();

        Policy policy = new Policy();
        policy.setValue(policyDescription);
        policy.setHashAlgorithm(extract("hashAlgorithm", policyDescription, String.class));
        policy.setMinSpecialChars(extract("specialChars", policyDescription, Integer.class));
        policy.setMinUpperCase(extract("upperCase", policyDescription, Integer.class));
        policy.setMinLowerCase(extract("lowerCase", policyDescription, Integer.class));
        policy.setPasswordHistory(extract("passwordHistory", policyDescription, Integer.class));
        policy.setMinDigits(extract("digits", policyDescription, Integer.class));
        policy.setHashIterations(extract("hashIterations", policyDescription, Integer.class));

        if (extract("passwordBlacklist", policyDescription, String.class) != null) {
            policy.setPasswordBlacklist(true);
        }

        if (extract("notUsername", policyDescription, String.class) != null) {
            policy.setNotUseUsername(true);
        }

        policy.setRegexPattern(extract("regexPattern", policyDescription, String.class));
        policy.setPasswordExpireDays(extract("forceExpiredPasswordChange", policyDescription, Integer.class));
        policy.setMinLength(extract("length", policyDescription, Integer.class));

        return policy;
    }

    @SuppressWarnings("unchecked")
    private <T> T extract(String code, String policyDescription, Class<T> clazz) {

        if (policyDescription == null || !policyDescription.contains(code)) {
            return null;
        }

        String extractedValue = policyDescription.substring(policyDescription.indexOf(code) + code.length() + 1);
        extractedValue = extractedValue.substring(0, extractedValue.indexOf(")"));

        if (Integer.class.equals(clazz)) {
            return (T) Integer.valueOf(extractedValue);
        }

        return (T) extractedValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createIdentityCredentials(String crmContactId, Credentials newCredentials) {

        CredentialRepresentation newCredential = new CredentialRepresentation();
        // TODO: empty password simple validation?
        UserResource UserResource = keycloak.realm(realm).users().get(crmContactId);
        newCredential.setType(CredentialRepresentation.PASSWORD);
        newCredential.setValue(newCredentials.getPassword());
        newCredential.setTemporary(Boolean.TRUE.equals(newCredential.isTemporary()));

        try {
            UserResource.resetPassword(newCredential);
        } catch (BadRequestException e) {
            e.printStackTrace();
            throw new PolicyPasswordException(newCredentials.getPassword());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo getIdentity(String crmContactId) {

        UserResource UserResource = Optional.ofNullable(keycloak.realm(realm).users().get(crmContactId)).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        UserRepresentation userRepresentation = UserResource.toRepresentation();

        // TODO: Orica Mapper
        IdentityInfo identity = new IdentityInfo();
        identity.setCrmContactId(userRepresentation.getId());
        identity.setFirstName(userRepresentation.getFirstName());
        identity.setLastName(userRepresentation.getLastName());
        identity.setUsername(userRepresentation.getUsername());
        identity.setEmail(userRepresentation.getEmail());

        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void impersonateIdentity(String crmContactId) {
        Optional.ofNullable(keycloak.realm(realm).users().get(crmContactId))
            .orElseThrow(() -> new IdentityNotFoundException(crmContactId)).impersonate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logoutIdentity(String crmContactId) {
        Optional.ofNullable(keycloak.realm(realm).users().get(crmContactId))
            .orElseThrow(() -> new IdentityNotFoundException(crmContactId)).logout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIdentityExists(String username) {
        return !keycloak.realm(realm).users().search(username).isEmpty();
    }
    
}