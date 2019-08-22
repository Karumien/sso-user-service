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
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.api.model.UserBaseInfo;
import com.karumien.cloud.sso.exceptions.PolicyPasswordException;
import com.karumien.cloud.sso.exceptions.UserDuplicateException;
import com.karumien.cloud.sso.exceptions.UserNotFoundException;

/**
 * Implementation {@link UserService} for identity management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 6. 2019 22:07:27
 */
@Service
public class UserServiceImpl implements UserService {

    @Value("${keycloak.realm}")
    private String realm;

    @Autowired
    private Keycloak keycloak;

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(String id) {
        keycloak.realm(realm).users().delete(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBaseInfo createUser(@Valid UserBaseInfo userBaseInfo) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(Optional.ofNullable(userBaseInfo.getUsername()).orElse(userBaseInfo.getEmail()));
        user.setFirstName(userBaseInfo.getFirstName());
        user.setLastName(userBaseInfo.getLastName());
        user.setId(userBaseInfo.getId());

        // TODO: extract email as new method
        user.setEmail(userBaseInfo.getEmail());
        // TODO: user.setEmailVerified(emailVerified);
        user.setEnabled(true);


        // user.singleAttribute("customAttribute", "customAttribute");
        // user.setCredentials(Arrays.asList(credential));

        Response response = keycloak.realm(realm).users().create(user);

        userBaseInfo.setId(getCreatedId(response));
        userBaseInfo.setUsername(user.getUsername());

        return userBaseInfo;
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
            throw new UserDuplicateException("User exists");
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
    public void createUserCredentials(String id, Credentials newCredentials) {

        CredentialRepresentation newCredential = new CredentialRepresentation();
        // TODO: empty password simple validation?
        UserResource userResource = keycloak.realm(realm).users().get(id);
        newCredential.setType(CredentialRepresentation.PASSWORD);
        newCredential.setValue(newCredentials.getPassword());
        newCredential.setTemporary(Boolean.TRUE.equals(newCredential.isTemporary()));

        try {
            userResource.resetPassword(newCredential);
        } catch (BadRequestException e) {
            e.printStackTrace();
            throw new PolicyPasswordException(newCredentials.getPassword());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBaseInfo getUser(String id) {

        UserResource userResource = Optional.ofNullable(keycloak.realm(realm).users().get(id)).orElseThrow(() -> new UserNotFoundException(id));
        UserRepresentation userRepresentation = userResource.toRepresentation();

        // TODO: Orica Mapper
        UserBaseInfo user = new UserBaseInfo();
        user.setId(userRepresentation.getId());
        user.setFirstName(userRepresentation.getFirstName());
        user.setLastName(userRepresentation.getLastName());
        user.setUsername(userRepresentation.getUsername());
        user.setEmail(userRepresentation.getEmail());

        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void impersonateUser(String id) {
        Optional.ofNullable(keycloak.realm(realm).users().get(id))
            .orElseThrow(() -> new UserNotFoundException(id)).impersonate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logoutUser(String id) {
        Optional.ofNullable(keycloak.realm(realm).users().get(id))
            .orElseThrow(() -> new UserNotFoundException(id)).logout();
    }

}