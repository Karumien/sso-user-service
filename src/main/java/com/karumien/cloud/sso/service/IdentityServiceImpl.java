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
import java.util.Map;
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
import org.springframework.util.CollectionUtils;

import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.AttributeNotFoundException;
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

        identity.setUsername(StringUtils.isBlank(identityInfo.getUsername()) ?
                "generated-" + identityInfo.getCrmContactId() : identityInfo.getUsername());
        identity.setFirstName(identityInfo.getFirstName());
        identity.setLastName(identityInfo.getLastName());

        identity.setEmail(identityInfo.getEmail());
        
        //TODO: enabled after create = false, true when unblocked or create credentials
        identity.setEnabled(true);
        identity.setEmailVerified(Boolean.TRUE.equals(identityInfo.isEmailVerified() && !StringUtils.isBlank(identityInfo.getEmail())));

        if (!StringUtils.isBlank(identityInfo.getPhone())) {
            identity.singleAttribute(ATTR_PHONE, identityInfo.getPhone());
        }
        if (!StringUtils.isBlank(identityInfo.getGlobalEmail())) {
            identity.singleAttribute(ATTR_GLOBAL_EMAIL, identityInfo.getGlobalEmail());
        }
        identity.singleAttribute(ATTR_CRM_CONTACT_ID, 
                Optional.of(identityInfo.getCrmContactId()).orElseThrow(() -> new IdNotFoundException(ATTR_CRM_CONTACT_ID)));
        identity.singleAttribute(ATTR_CRM_ACCOUNT_ID, 
                Optional.of(identityInfo.getCrmAccountId()).orElseThrow(() -> new IdNotFoundException(ATTR_CRM_ACCOUNT_ID)));

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

        identity.setCrmAccountId(getSimpleAttribute(userRepresentation.getAttributes(), ATTR_CRM_ACCOUNT_ID).orElse(null));
        identity.setCrmContactId(getSimpleAttribute(userRepresentation.getAttributes(), ATTR_CRM_CONTACT_ID).orElse(null));
        identity.setGlobalEmail(getSimpleAttribute(userRepresentation.getAttributes(), ATTR_GLOBAL_EMAIL).orElse(null));
        identity.setPhone(getSimpleAttribute(userRepresentation.getAttributes(), ATTR_PHONE).orElse(null));

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
    public void savePinOfIdentityDriver(String crmContactId, DriverPin pin) {
        findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId))          
            .getAttributes().put(ATTR_DRIVER_PIN, Arrays.asList(pin.getPin()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePinOfIdentityDriver(String crmContactId) {
        findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId))
            .getAttributes().remove(ATTR_DRIVER_PIN);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void blockIdentity(String crmContactId, boolean blockedStatus) {
        UserRepresentation user = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        user.setEnabled(!blockedStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverPin getPinOfIdentityDriver(String crmContactId) {
        UserRepresentation user = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        DriverPin pin = new DriverPin();
        pin.setPin(getSimpleAttribute(user.getAttributes(), ATTR_DRIVER_PIN)
                .orElseThrow(() -> new AttributeNotFoundException(ATTR_DRIVER_PIN)));
        return pin;        
    }

    private Optional<String> getSimpleAttribute(Map<String, List<String>> attributes, String attrName) {
        if (CollectionUtils.isEmpty(attributes) || CollectionUtils.isEmpty(attributes.get(attrName))) {
            return Optional.empty();
        }
        return attributes.get(attrName).stream().findFirst();
    }

}