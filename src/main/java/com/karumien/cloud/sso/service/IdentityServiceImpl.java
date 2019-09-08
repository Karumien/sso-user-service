/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
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
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.IdNotFoundException;
import com.karumien.cloud.sso.exceptions.IdentityDuplicateException;
import com.karumien.cloud.sso.exceptions.IdentityEmailNotExistsOrVerifiedException;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;
import com.karumien.cloud.sso.exceptions.PolicyPasswordException;

import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;

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

    @Autowired
    private RoleService roleService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteIdentity(String crmContactId) {
        UserRepresentation user = findIdentity(crmContactId)
            .orElseThrow(() -> new IdentityNotFoundException(crmContactId));        
        keycloak.realm(realm).users().delete(user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo createIdentity(@Valid IdentityInfo identityInfo) {

        UserRepresentation identity = new UserRepresentation();
        
        if (StringUtils.isBlank(identityInfo.getUsername())) {
            identity.setUsername("generated-" + identityInfo.getCrmContactId());
        }
        
        identity.setFirstName(identityInfo.getFirstName());
        identity.setLastName(identityInfo.getLastName());

        // TODO: extract email as new method
        identity.setEmail(identityInfo.getEmail());
        // TODO: Identity.setEmailVerified(emailVerified);
        identity.setEnabled(true);
        identity.setEmailVerified(Boolean.TRUE.equals(identityInfo.isEmailVerified() && !StringUtils.isBlank(identityInfo.getEmail())));

        if (!StringUtils.isBlank(identityInfo.getPhone())) {
            identity.singleAttribute(ATTR_PHONE, identityInfo.getPhone());
        }
        if (!StringUtils.isBlank(identityInfo.getContactEmail())) {
            identity.singleAttribute(ATTR_CONTACT_EMAIL, identityInfo.getContactEmail());
        }
        identity.singleAttribute(ATTR_CRM_CONTACT_ID, 
                Optional.of(identityInfo.getCrmContactId()).orElseThrow(() -> new IdNotFoundException(ATTR_CRM_CONTACT_ID)));
        identity.singleAttribute(ATTR_CRM_ACCOUNT_ID, 
                Optional.of(identityInfo.getCrmAccountId()).orElseThrow(() -> new IdNotFoundException(ATTR_CRM_ACCOUNT_ID)));
        
        // identity.setCredentials(Arrays.asList(credential));

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
            throw new IdentityDuplicateException("Identity with same username already exists");
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

        // FIXME: update to unblocked
        // UserRepresentation userResource = keycloak.realm(realm).users().get(crmContactId).;

        CredentialRepresentation newCredential = new CredentialRepresentation();
        // TODO: empty password simple validation?
        UserResource userResource = keycloak.realm(realm).users().get(crmContactId);
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
    public IdentityInfo getIdentity(String crmContactId) {
        return mapping(findIdentity(crmContactId)
                .orElseThrow(() -> new IdentityNotFoundException(crmContactId)));
    }
   
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserRepresentation> findIdentity(String crmContactId) {
        return keycloak.realm(realm).users().list().stream()
            .filter(g -> g.getAttributes() != null)
            .filter(g -> g.getAttributes().containsKey(ATTR_CRM_CONTACT_ID))
            .filter(g -> g.getAttributes().get(ATTR_CRM_CONTACT_ID).contains(crmContactId)).findFirst();
    }
    
    private IdentityInfo mapping(UserRepresentation userRepresentation) {

        // TODO: Orica Mapper
        IdentityInfo identity = new IdentityInfo();
        identity.setFirstName(userRepresentation.getFirstName());
        identity.setLastName(userRepresentation.getLastName());
        identity.setUsername(userRepresentation.getUsername());
        identity.setEmail(userRepresentation.getEmail());
        identity.setEmailVerified(userRepresentation.isEmailVerified());

        // TODO: function
        if (userRepresentation.getAttributes().get(ATTR_CRM_ACCOUNT_ID) != null) {
            identity.setCrmAccountId(userRepresentation.getAttributes().get(ATTR_CRM_ACCOUNT_ID).stream().findAny().orElse(null));
        }
        if (userRepresentation.getAttributes().get(ATTR_CRM_CONTACT_ID) != null) {
            identity.setCrmContactId(userRepresentation.getAttributes().get(ATTR_CRM_CONTACT_ID).stream().findAny().orElse(null));
        }
        if (userRepresentation.getAttributes().get(ATTR_CONTACT_EMAIL) != null) {
            identity.setContactEmail(userRepresentation.getAttributes().get(ATTR_CONTACT_EMAIL).stream().findAny().orElse(null));
        }
        if (userRepresentation.getAttributes().get(ATTR_PHONE) != null) {
            identity.setPhone(userRepresentation.getAttributes().get(ATTR_PHONE).stream().findAny().orElse(null));
        }

        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void impersonateIdentity(String crmContactId) {
        Optional.ofNullable(keycloak.realm(realm).users().get(crmContactId)).orElseThrow(() -> new IdentityNotFoundException(crmContactId)).impersonate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logoutIdentity(String crmContactId) {
        Optional.ofNullable(keycloak.realm(realm).users().get(crmContactId)).orElseThrow(() -> new IdentityNotFoundException(crmContactId)).logout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIdentityExists(String username) {
        return !keycloak.realm(realm).users().search(username).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean assigneRolesToIdentity(String crmContactId, @Valid List<String> roles) {
        return findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId))
            .getRealmRoles().addAll(roles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unassigneRolesToIdentity(String crmContactId, @Valid List<String> roles) {
        return findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId))
            .getRealmRoles().removeAll(roles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RoleInfo> getAllIdentityRoles(String crmContactId) {
        return roleService.getAllRolesOfIdentity(crmContactId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void savePinToIdentityDriver(String crmContactId, DriverPin pin) {
        findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId))
            .getAttributes().put("driverPin", Arrays.asList(pin.getPin()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetPasswordByEmail(String crmContactId) {
        UserRepresentation user = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        if (StringUtils.isBlank(user.getEmail()) || !user.isEmailVerified()) {
            throw new IdentityEmailNotExistsOrVerifiedException(crmContactId);
        }
        keycloak.realm(realm).users().get(user.getId()).executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));
    }

}