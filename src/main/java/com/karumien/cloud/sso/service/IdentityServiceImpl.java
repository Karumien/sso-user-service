/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;
import com.karumien.cloud.sso.exceptions.AttributeNotFoundException;
import com.karumien.cloud.sso.exceptions.IdNotFoundException;
import com.karumien.cloud.sso.exceptions.IdentityDuplicateException;
import com.karumien.cloud.sso.exceptions.IdentityEmailNotExistsOrVerifiedException;
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

    @Autowired
    private RoleService roleService;

    @Autowired
    private AccountService accountService;

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

        identity.setUsername(StringUtils.isEmpty(identityInfo.getUsername()) ?
                "generated-" + identityInfo.getCrmContactId() : identityInfo.getUsername());
        identity.setFirstName(identityInfo.getFirstName());
        identity.setLastName(identityInfo.getLastName());

        identity.setEmail(identityInfo.getEmail());
        
        identity.setEnabled(true);
        identity.setEmailVerified(Boolean.TRUE.equals(identityInfo.isEmailVerified() && !StringUtils.isEmpty(identityInfo.getEmail())));

        if (!StringUtils.isEmpty(identityInfo.getPhone())) {
            identity.singleAttribute(ATTR_PHONE, identityInfo.getPhone());
        }
        if (!StringUtils.isEmpty(identityInfo.getGlobalEmail())) {
            identity.singleAttribute(ATTR_GLOBAL_EMAIL, identityInfo.getGlobalEmail());
        }
        if (!StringUtils.isEmpty(identityInfo.getLocale())) {
            identity.singleAttribute(ATTR_LOCALE, identityInfo.getLocale());
        }
        identity.singleAttribute(ATTR_CRM_CONTACT_ID, 
                Optional.of(identityInfo.getCrmContactId()).orElseThrow(() -> new IdNotFoundException(ATTR_CRM_CONTACT_ID)));
        identity.singleAttribute(ATTR_CRM_ACCOUNT_ID, 
                Optional.of(identityInfo.getCrmAccountId()).orElseThrow(() -> new IdNotFoundException(ATTR_CRM_ACCOUNT_ID)));
		if (!StringUtils.isEmpty(identityInfo.getNav4Id())) {
			identity.singleAttribute(ATTR_NAV4ID,
					Optional.of(identityInfo.getNav4Id()).orElseThrow(() -> new IdNotFoundException(ATTR_NAV4ID)));
		}
        Response response = keycloak.realm(realm).users().create(identity);        
        identityInfo.setIdentityId(getCreatedId(response));
        
        String groupId = accountService.findGroup(identityInfo.getCrmAccountId())
            .orElseThrow(() -> new AccountNotFoundException(identityInfo.getCrmAccountId())).getId();
       
        keycloak.realm(realm).users().get(identityInfo.getIdentityId()).joinGroup(groupId);
            
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
    public void createIdentityCredentials(String crmContactId, Credentials newCredentials) {

        UserRepresentation user = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        user.setEnabled(true);
        user.setUsername(newCredentials.getUsername());
        
        // TODO: verify currentPassword
                
        UserResource userResource = keycloak.realm(realm).users().get(user.getId());
        
        CredentialRepresentation newCredential = new CredentialRepresentation();
        newCredential.setType(CredentialRepresentation.PASSWORD);
        newCredential.setValue(newCredentials.getPassword());
        newCredential.setTemporary(Boolean.TRUE.equals(newCredential.isTemporary()));
        
        try {
            userResource.update(user);
            userResource.resetPassword(newCredential);
        } catch (BadRequestException e) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo mapping(UserRepresentation userRepresentation) {

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
        identity.setNav4Id(getSimpleAttribute(userRepresentation.getAttributes(), ATTR_NAV4ID).orElse(null));
        identity.setLocale(getSimpleAttribute(userRepresentation.getAttributes(), ATTR_LOCALE).orElse(null));
        identity.setIdentityId(userRepresentation.getId());
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
    public boolean assignRolesToIdentity(String crmContactId, @Valid List<String> roles) {
    	UserRepresentation userRepresentation = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
    	UserResource user = keycloak.realm(realm).users().get(userRepresentation.getId());
        List<RoleRepresentation> list = getListOfRoleReprasentationBaseOnIds(roles);        
    	user.roles().realmLevel().add(list);
    	refreshBinaryRoles(userRepresentation);
    	return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshBinaryRoles(UserRepresentation userRepresentation) {        
        UserResource userResource = keycloak.realm(realm).users().get(userRepresentation.getId());
        String binaryRoles = roleService.getRolesBinary(userRepresentation);
        if (StringUtils.isEmpty(binaryRoles)) {
            userRepresentation.getAttributes().remove(ATTR_BINARY_RIGHTS);
        } else {
            userRepresentation.getAttributes().put(ATTR_BINARY_RIGHTS, Arrays.asList(binaryRoles));
        }
        userResource.update(userRepresentation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unassignRolesToIdentity(String crmContactId, @Valid List<String> roles) {
        UserRepresentation userRepresentation = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        UserResource user = keycloak.realm(realm).users().get(userRepresentation.getId());
        List<RoleRepresentation> list = getListOfRoleReprasentationBaseOnIds(roles);
        user.roles().realmLevel().remove(list);
        refreshBinaryRoles(userRepresentation);
        return true;
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
        UserRepresentation user = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        user.getAttributes().put(ATTR_DRIVER_PIN, Arrays.asList(pin.getPin()));
        keycloak.realm(realm).users().get(user.getId()).update(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePinOfIdentityDriver(String crmContactId) {
        UserRepresentation user = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        user.getAttributes().remove(ATTR_DRIVER_PIN);
        keycloak.realm(realm).users().get(user.getId()).update(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetPasswordByEmail(String crmContactId) {
        UserRepresentation user = findIdentity(crmContactId).orElseThrow(() -> new IdentityNotFoundException(crmContactId));
        if (StringUtils.isEmpty(user.getEmail()) || !user.isEmailVerified()) {
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
        keycloak.realm(realm).users().get(user.getId()).update(user);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getSimpleAttribute(Map<String, List<String>> attributes, String attrName) {
        if (CollectionUtils.isEmpty(attributes) || CollectionUtils.isEmpty(attributes.get(attrName))) {
            return Optional.empty();
        }
        return attributes.get(attrName).stream().findFirst();
    }

	private List<RoleRepresentation> getListOfRoleReprasentationBaseOnIds(List<String> roles) {
		List<RoleRepresentation> returnList = new ArrayList<>();
		roles.forEach(role -> {
			RoleResource searcherRole = keycloak.realm(realm).roles().get(role);
			try {
				returnList.add(searcherRole.toRepresentation());
			}
			catch(Exception e) {
				
			}
		});
		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
    public boolean isActiveRole(String roleId, String crmContactId) {
        //FIXME: performance
        return getAllIdentityRoles(crmContactId).stream().filter(role -> role.getRoleId().equals(roleId)).findAny().isPresent();
    }

}