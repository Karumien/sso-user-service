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

import java.util.Arrays;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
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
	public void deleteRole() {
		// TODO missing what role i should remove 	
		RoleRepresentation roleRepresentation = new RoleRepresentation();
		roleRepresentation.setDescription("fillMeIn");
		roleRepresentation.setName("fillMeIn");
		keycloak.realm(realm).users().get("fillMeIn").roles().realmLevel().remove(Arrays.asList(roleRepresentation));
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public RoleBaseInfo getRoleBaseOnId(String id) {
		RealmResource realmResource = keycloak.realm(realm);
		RoleRepresentation userClientRole = realmResource.clients().get(id) 
				.roles().get("user").toRepresentation();

		RoleBaseInfo role = new RoleBaseInfo();
		role.setName(userClientRole.getName());
		role.setDescription(userClientRole.getDescription());
		role.setId(userClientRole.getId());	
		return role;
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public void assigneRoleToClient(String clientId, RoleBaseInfo role) {
		RealmResource realmResource = keycloak.realm(realm);
		UsersResource userRessource = realmResource.users();
		
		RoleRepresentation realmRole = realmResource.roles().get(role.getName()).toRepresentation();
		userRessource.get(clientId).roles().realmLevel() //
		.add(Arrays.asList(realmRole));
	}

}
