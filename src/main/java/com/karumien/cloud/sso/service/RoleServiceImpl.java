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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.NotFoundException;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.ClientNotFoundException;
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
    private LocalizationService localizationService;

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleInfo getRoleBaseOnId(String roleId) {    	
        RoleRepresentation role = findRoleRepresentation(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
        return transformRoleToBaseRole(role, findRoleResource(roleId).getRoleComposites()); 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RoleRepresentation> findRoleRepresentation(String roleId) {
        try {
            return Optional.of(keycloak.realm(realm).roles().get(roleId).toRepresentation());
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleResource findRoleResource(String roleName) {
    	return keycloak.realm(realm).roles().get(roleName);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getIdentityRoles(UserRepresentation userRepresentation) {
        return keycloak.realm(realm).users().get(userRepresentation.getId()).roles().realmLevel().listEffective().stream()
            .filter(r -> isRole(r.getName()))
            .map(r -> r.getName())
            .collect(Collectors.toList());
    }

    protected List<String> getIdentityRights(UserRepresentation userRepresentation) {
        return keycloak.realm(realm).users().get(userRepresentation.getId()).roles().realmLevel().listEffective().stream()
            .filter(r -> !isRole(r.getName()))
            .map(r -> r.getName())
            .collect(Collectors.toList());
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getIdentityRolesNav4(String nav4Id) {
        return getIdentityRoles(identityService.findIdentityNav4(nav4Id).orElseThrow(() -> new IdentityNotFoundException("nav4Id = " + nav4Id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getIdentityRoles(String contactNumber) {
        return getIdentityRoles(identityService.findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber)));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getIdentityRights(String contactNumber) {
        return getIdentityRights(identityService.findIdentity(contactNumber).orElseThrow(() -> new IdentityNotFoundException(contactNumber)));
    }

    /**
     * Funtion to remap {@link RoleRepresentation} to {@link RoleInfo}
     * 
     * @param role
     *            {@link RoleRepresentation}
     * @return {@link RoleInfo}
     */
    private RoleInfo transformRoleToBaseRole(RoleRepresentation role, Set<RoleRepresentation> rights) {
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setRoleId(role.getName());
        roleInfo.setDescription(role.getDescription());
        
        if (!CollectionUtils.isEmpty(rights)) {
            List<String> rightKeys = rights.stream()
                .filter(r -> !isRole(r.getName()))
                .map(r -> r.getName()).collect(Collectors.toList());
        
            roleInfo.setRights(CollectionUtils.isEmpty(rightKeys) ? null : rightKeys);
        }
        
        // role.setId(userClientRole.getId());
        roleInfo.setTranslation(
                localizationService.translate("role" + "." + role.getName().toLowerCase(), role.getAttributes(), 
                        LocaleContextHolder.getLocale(), roleInfo.getDescription()));
        return roleInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleInfo getClientsRoleBaseOnId(String roleId, String clientId) {
        keycloak.realm(realm).clients().findByClientId(clientId).stream().findAny().orElseThrow(() -> new ClientNotFoundException(clientId));
        RoleResource role = keycloak.realm(realm).clients().get(clientId).roles().get(roleId);
        return transformRoleToBaseRole(role.toRepresentation(), role.getRealmRoleComposites());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RoleInfo> getAccountRoles(GroupResource groupResource, boolean effective) {
        List<RoleRepresentation> roles = getAccountRolesRepresentation(groupResource, effective);
        return roles.stream()
            .map(role -> getRoleBaseOnId(role.getName()))
            .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable
    public List<RoleInfo> getRoles() {
        return keycloak.realm(realm).roles().list().stream()
            .filter(r -> isRole(r.getName()))
            .map(role -> transformRoleToBaseRole(keycloak.realm(realm).rolesById().getRole(role.getId()), null))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RoleInfo> getRights() {
        return keycloak.realm(realm).roles().list().stream()
            .filter(r -> !isRole(r.getName()))
            .map(role -> transformRoleToBaseRole(role, null))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RoleRepresentation> getAccountRolesRepresentation(GroupResource groupResource, boolean effective) {
        return effective ? groupResource.roles().realmLevel().listEffective() : groupResource.roles().realmLevel().listAll();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getIdentityRights(GroupResource groupResource, String contactNumber) {
       
        // TODO: how to solve rights from more clients? -> user rights? or clientId?
        List<String> roleIds = getIdentityRoles(contactNumber);
        Set<String> rights = new HashSet<>();
        getAccountRoles(groupResource, false).stream()
            .filter(role -> roleIds.contains(role.getRoleId()))
            .filter(role -> !CollectionUtils.isEmpty(role.getRights()))
            .forEach(role -> rights.addAll(role.getRights()));
        return rights.stream().collect(Collectors.toList());
        
    }
    
}
