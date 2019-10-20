/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;
import com.karumien.cloud.sso.exceptions.RoleNotFoundException;

/**
 * Implementation {@link RuleService} for identity management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 22. 8. 2019 18:59:57
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Autowired
    private Keycloak keycloak;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SearchService searchService;

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleInfo createRole(RoleInfo role) {

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setDescription(role.getDescription());
        roleRepresentation.setName(role.getRoleId());

        // if (Boolean.TRUE.equals(role.isClientRole())) {
        // org.keycloak.representations.idm.ClientRepresentation clientResource =
        // keycloak.realm(realm).clients().findByClientId(role.getClientId()).get(0);
        // keycloak.realm(realm).clients().get(clientResource.getId()).roles().create(roleRepresentation);
        // return getClientsRoleBaseOnId(role.getRoleId(), role.getClientId());
        // } else {
        keycloak.realm(realm).roles().create(roleRepresentation);
        return getRoleBaseOnId(role.getRoleId());
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRole(String roleId) {
        try {
            keycloak.realm(realm).roles().deleteRole(roleId);
        } catch (NotFoundException e) {
            throw new RoleNotFoundException(roleId);
        }
        //
        //
        // RoleInfo role = getRoleBaseOnId(id);
        //
        // RoleRepresentation roleRepresentation = new RoleRepresentation();
        // roleRepresentation.setDescription(role.getDescription());
        // roleRepresentation.setName(role.getName());
        // roleRepresentation.setId(role.getId());
        //
        // keycloak.realm(realm).users().get(role.getClientId()).roles().realmLevel().remove(Arrays.asList(roleRepresentation));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleInfo getRoleBaseOnId(String roleId) {
        return transformRoleToBaseRole(findRoleResource(roleId).orElseThrow(() -> new RoleNotFoundException(roleId)).toRepresentation());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RoleResource> findRoleResource(String roleId) {
        try {
            return Optional.of(keycloak.realm(realm).roles().get(roleId));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignRoleToIdentity(String contactNumber, RoleInfo role) {
        UserRepresentation userRepresentation = identityService.findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        RoleRepresentation realmRole = findRoleResource(role.getRoleId()).orElseThrow(() -> new RoleNotFoundException(role.getRoleId())).toRepresentation();
        UserResource user = keycloak.realm(realm).users().get(userRepresentation.getId());
        user.roles().realmLevel().add(Arrays.asList(realmRole));
        user.update(userRepresentation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RoleInfo> getAllRolesOfIdentity(String contactNumber) {
        UserRepresentation userRepresentation = identityService.findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        return keycloak.realm(realm).users().get(userRepresentation.getId()).roles().realmLevel().listEffective().stream()
                .map(role -> transformRoleToBaseRole(role)).collect(Collectors.toList());
    }

    /**
     * Funtion to remap {@link RoleRepresentation} to {@link RoleInfo}
     * 
     * @param role
     *            {@link RoleRepresentation}
     * @return {@link RoleInfo}
     */
    private RoleInfo transformRoleToBaseRole(RoleRepresentation role) {
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRoleId(role.getName());
        roleInfo.setDescription(role.getDescription());
        // role.setId(userClientRole.getId());
        roleInfo.setTranslation(
                translate("role" + "." + role.getName().toLowerCase(), role.getAttributes(), LocaleContextHolder.getLocale(), roleInfo.getDescription()));
        return roleInfo;
    }

    private String translate(String localeKey, Map<String, List<String>> attributes, Locale locale, String defaultTranslate) {
        String translate = null;
        List<String> translates = attributes.get(ATTR_TRANSLATION + "[" + locale.getLanguage() + "]");
        if (!CollectionUtils.isEmpty(translates)) {
            translate = translates.get(0);
        }
        if (translate == null) {
            translates = attributes.get(ATTR_TRANSLATION);
            if (!CollectionUtils.isEmpty(translates)) {
                translate = translates.get(0);
            }
        }
        if (translate == null) {
            translate = messageSource.getMessage(localeKey, null, locale);
            if (localeKey.equals(translate)) {
                translate = null;
            }
        }
        if (translate == null) {
            translate = defaultTranslate;
        }
        return translate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unassignRoleFromIdentity(String contactNumber, String roleId) {
        UserRepresentation userRepresentation = identityService.findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber));
        RoleRepresentation realmRole = findRoleResource(roleId).orElseThrow(() -> new RoleNotFoundException(roleId)).toRepresentation();
        UserResource user = keycloak.realm(realm).users().get(userRepresentation.getId());
        user.roles().realmLevel().remove(Arrays.asList(realmRole));
        user.update(userRepresentation);
    }

    /**
	 * {@inheritDoc}
	 */
    @Override
    public String getRolesBinary(UserRepresentation userRepresentation) {	

        StringBuilder binaryRule = new StringBuilder();
        Optional<String> accountNumber = searchService.getSimpleAttribute(userRepresentation.getAttributes(), IdentityService.ATTR_ACCOUNT_NUMBER);
        
        if (!accountNumber.isPresent()) {
            return binaryRule.toString();
        }
        
        Map<String, Integer> maskMap = new HashMap<String, Integer>();
		keycloak.realm(realm).users().get(userRepresentation.getId()).roles().realmLevel().listEffective().forEach(role ->
      		{
      		  Optional<RoleResource> roleWithAttributes = findRoleResource(role.getName());
      		  if (roleWithAttributes.isPresent() &&  roleWithAttributes.get().toRepresentation().getAttributes().get(ATTR_BINARY_MASK) != null) {
      			String stringMask = roleWithAttributes.get().toRepresentation().getAttributes().get(ATTR_BINARY_MASK).get(0);
      		    Integer binaryMask = Integer.valueOf(stringMask.substring(0,stringMask.length()-2), 2);
      			
      		    // TODO: use attribute module - no split?
      		    String[] splitName = role.getName().split("_");
    			if(splitName[0].equals("ROLE")) {
    				Integer rigtValue = maskMap.get(splitName[1]) != null ? maskMap.get(splitName[1]) + binaryMask : binaryMask;
    				maskMap.put(splitName[1], rigtValue);
    			}
      		  }
    		});
		    
		
		    List<String> modules = moduleService.getAccountModulesSimple(accountNumber.get());
		
    		for (Entry<String, Integer> entry : maskMap.entrySet()) {
    		    String key = entry.getKey();
    		    if (modules.contains(key)) {
    		        binaryRule.append(key + ":" + Integer.toHexString(entry.getValue())+ " ");
    		    }
    		}
    		return binaryRule.toString();
    
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleInfo getClientsRoleBaseOnId(String roleId, String clientId) {
        org.keycloak.representations.idm.ClientRepresentation clientResource = keycloak.realm(realm).clients().findByClientId(clientId).get(0);
        return transformRoleToBaseRole(keycloak.realm(realm).clients().get(clientResource.getId()).roles().get(roleId).toRepresentation());
    }

}
