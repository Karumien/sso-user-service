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
import java.util.stream.Collectors;

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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.karumien.cloud.sso.api.UpdateType;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;
import com.karumien.cloud.sso.exceptions.AttributeNotFoundException;
import com.karumien.cloud.sso.exceptions.IdNotFoundException;
import com.karumien.cloud.sso.exceptions.IdentityDuplicateException;
import com.karumien.cloud.sso.exceptions.IdentityEmailNotExistsOrVerifiedException;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;
import com.karumien.cloud.sso.exceptions.PasswordPolicyException;
import com.karumien.cloud.sso.exceptions.UpdateIdentityException;


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
    
    @Autowired
    private SearchService searchService;
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteIdentity(String contactNumber) {
        UserRepresentation user = findIdentity(contactNumber)
            .orElseThrow(() -> new IdentityNotFoundException(contactNumber));        
        keycloak.realm(realm).users().delete(user.getId());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo updateIdentity(String contactNumber, IdentityInfo identityInfo) {
        
        UserRepresentation user = findIdentity(contactNumber)
                .orElseThrow(() -> new IdentityNotFoundException(contactNumber));        
        
        if (StringUtils.hasText(identityInfo.getUsername())) {
            user.setUsername(identityInfo.getUsername());  
        }
        
        user.setFirstName(identityInfo.getFirstName());
        user.setLastName(identityInfo.getLastName());
        user.setEmail(identityInfo.getEmail());
        user.setEmailVerified(Boolean.TRUE.equals(identityInfo.isEmailVerified()) && !StringUtils.hasText(identityInfo.getEmail()));
        
        if (StringUtils.hasText(identityInfo.getPhone())) {
            user.singleAttribute(ATTR_PHONE, identityInfo.getPhone());
        } else {
            user.getAttributes().remove(ATTR_PHONE);
        } 
        if (StringUtils.hasText(identityInfo.getGlobalEmail())) {
            user.singleAttribute(ATTR_GLOBAL_EMAIL, identityInfo.getGlobalEmail());
        } else {
            user.getAttributes().remove(ATTR_GLOBAL_EMAIL);
        } 

        user.singleAttribute(ATTR_LOCALE, StringUtils.hasText(identityInfo.getLocale()) ? 
                identityInfo.getLocale() : LocaleContextHolder.getLocale().getLanguage());
       
        UserResource userResource = keycloak.realm(realm).users().get(user.getId());
        
        try {
            userResource.update(user);
        } catch (BadRequestException e) {
            throw new UpdateIdentityException(e.getMessage());
        }
        
        return getIdentity(contactNumber);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo createIdentity(IdentityInfo identityInfo) {

        UserRepresentation identity = new UserRepresentation();

        // TODO: Username Policy validation
        String username = identityInfo.getUsername();
        
        if (!StringUtils.hasText(identityInfo.getUsername())) {
            username = "generated-" + identityInfo.getContactNumber();
            if (StringUtils.hasText(identityInfo.getNav4Id())) {
                username += "-n4-" + identityInfo.getNav4Id();
            }
        }
        
        if (isIdentityExists(username)) {
            throw new IdentityDuplicateException("Identity with same username already exists"); 
        }
        
        identity.setUsername(username);
        identity.setFirstName(identityInfo.getFirstName());
        identity.setLastName(identityInfo.getLastName());
        identity.setEmail(identityInfo.getEmail());
        identity.setEmailVerified(Boolean.TRUE.equals(identityInfo.isEmailVerified()) && StringUtils.hasText(identityInfo.getEmail()));
        
        identity.setEnabled(true);
        
        // backward compatibility
        if (!StringUtils.hasText(identityInfo.getAccountNumber())) {
            identityInfo.setAccountNumber(identityInfo.getCrmAccountId());
        }
        if (!StringUtils.hasText(identityInfo.getContactNumber())) {
            identityInfo.setContactNumber(identityInfo.getCrmContactId());
        }

        identity.singleAttribute(ATTR_CONTACT_NUMBER, 
                Optional.of(identityInfo.getContactNumber()).orElseThrow(() -> new IdNotFoundException(ATTR_CONTACT_NUMBER)));
        identity.singleAttribute(ATTR_ACCOUNT_NUMBER, 
                Optional.of(identityInfo.getAccountNumber()).orElseThrow(() -> new IdNotFoundException(ATTR_ACCOUNT_NUMBER)));
        
        // TODO: Persistent lock?
		if (StringUtils.hasText(identityInfo.getNav4Id())) {
		    if (!CollectionUtils.isEmpty(searchService.findUserIdsByAttribute(ATTR_NAV4ID, identityInfo.getNav4Id()))) {
		        throw new IdentityDuplicateException("Identity with same nav4Id already exists"); 
		    }
			identity.singleAttribute(ATTR_NAV4ID, identityInfo.getNav4Id());
		} else {		    
		    if (!CollectionUtils.isEmpty(searchService.findUserIdsByAttribute(ATTR_CONTACT_NUMBER, identityInfo.getContactNumber()))) {
                throw new IdentityDuplicateException("Identity with same contactNumber already exists, use nav4Id for uniqueness"); 
            }
		}

        if (StringUtils.hasText(identityInfo.getPhone())) {
            identity.singleAttribute(ATTR_PHONE, identityInfo.getPhone());
        }
        if (StringUtils.hasText(identityInfo.getGlobalEmail())) {
            identity.singleAttribute(ATTR_GLOBAL_EMAIL, identityInfo.getGlobalEmail());
        }
        if (StringUtils.hasText(identityInfo.getLocale())) {
            identity.singleAttribute(ATTR_LOCALE, identityInfo.getLocale());
        }

        String groupId = accountService.findGroup(identityInfo.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(identityInfo.getAccountNumber())).getId();

        Response response = keycloak.realm(realm).users().create(identity);        
        identityInfo.setIdentityId(getCreatedId(response));
        
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
    public void createIdentityCredentials(String contactNumber, Credentials newCredentials) {
        UserRepresentation user = findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        createCredentials(user, newCredentials);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void createIdentityCredentialsByUsername(String username, Credentials newCredentials) {        
        UserRepresentation user = findIdentityByUsername(username).orElseThrow(() -> new IdentityNotFoundException(" username = " + username));
        createCredentials(user, newCredentials);
    }
        
    /**
     * {@inheritDoc}
     */
    @Override
    public void createIdentityCredentialsNav4(String nav4Id, Credentials newCredentials) {
        String userId = searchService.findUserIdsByAttribute(ATTR_NAV4ID, nav4Id).stream().findFirst().orElseThrow(
                () -> new IdentityNotFoundException("NAV4 ID: " + nav4Id));
        createCredentials(keycloak.realm(realm).users().get(userId).toRepresentation(), newCredentials);
    }

    private void createCredentials(UserRepresentation user, Credentials newCredentials) {
                
        UserResource userResource = keycloak.realm(realm).users().get(user.getId());
        try {

            // TODO: enabled?
            user.setEnabled(true);

            // change when new username ready
            if (StringUtils.hasText(newCredentials.getUsername())) {
                // TODO: validate username
                user.setUsername(newCredentials.getUsername());
                userResource.update(user);          
            }

            // TODO: verify currentPassword

            // change when new password ready
            if (StringUtils.hasText(newCredentials.getPassword())) {
            
                CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
                credentialRepresentation.setValue(newCredentials.getPassword());
                
                if (Boolean.TRUE.equals(newCredentials.isTemporary())) {
                    user.getRequiredActions().add(AuthService.USER_ACTION_UPDATE_PASSWORD);
                    credentialRepresentation.setTemporary(true);
                } else {
                    user.getRequiredActions().remove(AuthService.USER_ACTION_UPDATE_PASSWORD);
                    credentialRepresentation.setTemporary(false);
                }
            
                userResource.resetPassword(credentialRepresentation);                
                userResource.update(user);          
            }

        } catch (BadRequestException e) {
            throw new PasswordPolicyException(newCredentials.getPassword());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo getIdentity(String contactNumber) {
        return mapping(findIdentity(contactNumber)
                .orElseThrow(() -> new IdentityNotFoundException(contactNumber)));
    }
   
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserRepresentation> findIdentity(String contactNumber) {
        List<String> userIds = searchService.findUserIdsByAttribute(ATTR_CONTACT_NUMBER, contactNumber);
        if (userIds.size() > 1) {
            throw new IdentityDuplicateException(contactNumber);            
        }        
        String userId = userIds.stream().findFirst().orElse(null);
        return Optional.ofNullable(userId == null ? null : keycloak.realm(realm).users().get(userId).toRepresentation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UserRepresentation> findIdentityNav4(String nav4Id) {
        List<String> userIds = searchService.findUserIdsByAttribute(ATTR_NAV4ID, nav4Id);
        if (userIds.size() > 1) {
            throw new IdentityDuplicateException("nav4Id = " + nav4Id);            
        }        
        String userId = userIds.stream().findFirst().orElse(null);
        return Optional.ofNullable(userId == null ? null : keycloak.realm(realm).users().get(userId).toRepresentation());
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
        
        identity.setAccountNumber(searchService.getSimpleAttribute(userRepresentation.getAttributes(), ATTR_ACCOUNT_NUMBER).orElse(null));
        identity.setContactNumber(searchService.getSimpleAttribute(userRepresentation.getAttributes(), ATTR_CONTACT_NUMBER).orElse(null));
        identity.setGlobalEmail(searchService.getSimpleAttribute(userRepresentation.getAttributes(), ATTR_GLOBAL_EMAIL).orElse(null));
        identity.setPhone(searchService.getSimpleAttribute(userRepresentation.getAttributes(), ATTR_PHONE).orElse(null));
        identity.setNav4Id(searchService.getSimpleAttribute(userRepresentation.getAttributes(), ATTR_NAV4ID).orElse(null));
        identity.setLocale(searchService.getSimpleAttribute(userRepresentation.getAttributes(), ATTR_LOCALE).orElse(null));
        identity.setIdentityId(userRepresentation.getId());
        
        identity.setCrmAccountId(identity.getAccountNumber());
        identity.setCrmContactId(identity.getContactNumber());
        
        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void impersonateIdentity(String contactNumber) {
        UserRepresentation userRepresentation = findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        UserResource user = keycloak.realm(realm).users().get(userRepresentation.getId());
        Map<String, Object> map = user.impersonate();
        System.out.println(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logoutIdentity(String contactNumber) {
        UserRepresentation userRepresentation = findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        UserResource user = keycloak.realm(realm).users().get(userRepresentation.getId());
        user.logout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIdentityExists(String username) {
        return findIdentityByUsername(username).isPresent();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isIdentityTemporaryLocked(String username) {
        Optional<UserRepresentation> user = findIdentityByUsername(username);
        return user.isPresent() && Boolean.TRUE.equals(keycloak.realm(realm).attackDetection().bruteForceUserStatus(user.get().getId()).get("disabled"));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRolesOfIdentity(String identityId, List<String> roles, UpdateType updateType) {
    	
    	UserResource userResource = Optional.ofNullable(keycloak.realm(realm).users().get(identityId))
	        .orElseThrow(() -> new IdentityNotFoundException("identityId = " + identityId));
    	
    	if (updateType == UpdateType.UPDATE) {
    	    // remove unused roles
    	    userResource.roles().realmLevel().remove(userResource.roles().realmLevel().listAll().stream()
    	        .filter(actualRole -> !roles.contains(actualRole.getId())).collect(Collectors.toList()));
    	}
    	
    	// add new roles
        if (updateType == UpdateType.ADD || updateType == UpdateType.UPDATE) {
            userResource.roles().realmLevel().add(getListOfRoleReprasentationBaseOnIds(roles));
        }
        
        // remove roles
        if (updateType == UpdateType.DELETE) {
            userResource.roles().realmLevel().remove(getListOfRoleReprasentationBaseOnIds(roles));
        }
        
    	refreshBinaryRoles(userResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshBinaryRoles(UserResource userResource) {        
        UserRepresentation userRepresentation = userResource.toRepresentation();
        String binaryRoles = roleService.getRolesBinary(userRepresentation);
        if (!StringUtils.hasText(binaryRoles)) {
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
    public void savePinOfIdentityDriver(String contactNumber, DriverPin pin) {
        UserRepresentation user = findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        user.getAttributes().put(ATTR_DRIVER_PIN, Arrays.asList(pin.getPin()));
        keycloak.realm(realm).users().get(user.getId()).update(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePinOfIdentityDriver(String contactNumber) {
        UserRepresentation user = findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        user.getAttributes().remove(ATTR_DRIVER_PIN);
        keycloak.realm(realm).users().get(user.getId()).update(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetPasswordByEmail(String contactNumber) {
        UserRepresentation user = findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        if (StringUtils.hasText(user.getEmail()) || !user.isEmailVerified()) {
            throw new IdentityEmailNotExistsOrVerifiedException(contactNumber);
        }
        keycloak.realm(realm).users().get(user.getId()).executeActionsEmail(Arrays.asList("UPDATE_PASSWORD"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void blockIdentity(String contactNumber, boolean blockedStatus) {
        UserRepresentation user = findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        user.setEnabled(!blockedStatus);
        keycloak.realm(realm).users().get(user.getId()).update(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverPin getPinOfIdentityDriver(String contactNumber) {
        UserRepresentation user = findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        DriverPin pin = new DriverPin();
        pin.setPin(searchService.getSimpleAttribute(user.getAttributes(), ATTR_DRIVER_PIN)
                .orElseThrow(() -> new AttributeNotFoundException(ATTR_DRIVER_PIN)));
        return pin;        
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
    public boolean isActiveRole(String roleId, String contactNumber) {
        return roleService.getIdentityRoles(contactNumber).contains(roleId);
    }

    /**
     * {@inheritDoc}
     */
	@Override
	public IdentityInfo getIdentityByNav4(String nav4Id) {
        String userId = searchService.findUserIdsByAttribute(ATTR_NAV4ID, nav4Id).stream().findFirst().orElseThrow(
                () -> new IdentityNotFoundException("NAV4 ID: " + nav4Id));
        return mapping(keycloak.realm(realm).users().get(userId).toRepresentation());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getUserRequiredActions(String username) {
	    if (!StringUtils.hasText(username)) {
	        return new ArrayList<>();
	    }
	    List<UserRepresentation> identities = keycloak.realm(realm).users().search(username);
	    return identities.isEmpty() ? new ArrayList<>() : identities.get(0).getRequiredActions();
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
    public Optional<UserRepresentation> findIdentityByUsername(String username) {
        return keycloak.realm(realm).users().search(username).stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActiveRoleNav4(String roleId, String nav4Id) {
        return roleService.getIdentityRolesNav4(nav4Id).contains(roleId);
    }

}