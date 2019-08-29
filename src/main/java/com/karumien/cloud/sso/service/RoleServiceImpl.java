/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.karumien.cloud.sso.api.model.RoleBaseInfo;

/**
 * Implementation {@link RuleService} for identity management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0,  22. 8. 2019 18:59:57
 */

@Service
public class RoleServiceImpl implements RoleService{

    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.client-id}")
    private String clientId;

    @Autowired
    private Keycloak keycloak;

    /**
     * {@inheritDoc}
     */
	@Override
	public RoleBaseInfo createRole(RoleBaseInfo role) {
		RoleRepresentation roleRepresentation = new RoleRepresentation();
		roleRepresentation.setDescription(role.getDescription());
		roleRepresentation.setName(role.getName());
		keycloak.realm(realm).users().get(role.getClientId()).roles().realmLevel().add(Arrays.asList(roleRepresentation));
		return getRoleBaseOnId(role.getName());
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void deleteRole(String id) {
		RoleBaseInfo role = getRoleBaseOnId(id);
		RoleRepresentation roleRepresentation = new RoleRepresentation();
		roleRepresentation.setDescription(role.getDescription());
		roleRepresentation.setName(role.getName());
		roleRepresentation.setId(role.getId());
		keycloak.realm(realm).users().get(role.getClientId()).roles().realmLevel().remove(Arrays.asList(roleRepresentation));
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public RoleBaseInfo getRoleBaseOnId(String roleId) {
		RealmResource realmResource = keycloak.realm(realm);
		RoleRepresentation userClientRole = realmResource.clients().get(clientId) 
				.roles().get(roleId).toRepresentation();
		
		return transformRoleToBaseRole(userClientRole);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void assigneRoleToClient(String userId, RoleBaseInfo role) {
		RealmResource realmResource = keycloak.realm(realm);
		UsersResource userRessource = realmResource.users();

		RoleRepresentation realmRole = realmResource.roles().get(role.getName()).toRepresentation();
		userRessource.get(userId).roles().realmLevel() //
		.add(Arrays.asList(realmRole));
	}

	@Override
	public List<RoleBaseInfo> getAllRoleForUser(String user_id) {
		List<RoleBaseInfo> listOfRoles = new ArrayList<>();
		
		RealmResource realmResource = keycloak.realm(realm);
		RolesResource userRoles = realmResource.clients().get(clientId).roles();	
		userRoles.list().forEach(role -> {
			listOfRoles.add(transformRoleToBaseRole(role));
		});
		return listOfRoles;
	}

	/**
	 * Funtion to remap {@link RoleRepresentation} to {@link RoleBaseInfo}
	 * @param userClientRole {@link RoleRepresentation} 
	 * @return {@link RoleBaseInfo}
	 */
	
	private RoleBaseInfo transformRoleToBaseRole(RoleRepresentation userClientRole) {
		RoleBaseInfo role = new RoleBaseInfo();
		role.setName(userClientRole.getName());
		role.setDescription(userClientRole.getDescription());
		role.setId(userClientRole.getId());
		return role;
	}

	@Override
	public void deleteRoleForUser(String userId, String role_id) {
		RoleBaseInfo role = getRoleBaseOnId(role_id);
		keycloak.realm(realm).users().get(userId).roles().realmLevel().remove(Arrays.asList(keycloak.realm(realm).roles().get(role.getName()).toRepresentation()));
	}
	
}
