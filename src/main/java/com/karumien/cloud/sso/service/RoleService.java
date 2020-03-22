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

import java.util.List;
import java.util.Optional;

import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.karumien.cloud.sso.api.model.RoleInfo;

/**
 * Service provides informations about identity Roles.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec"</a>
 * @since 1.0, 22. 8. 2019 18:58:57
 */
public interface RoleService {

    String ATTR_BINARY_MASK = "binaryMask";

    /**
     * Function that create role base on input object to realm.
     * 
     * @param role
     *            {@link RoleInfo} object of role
     * @return {@link RoleInfo} new created role as return parameter
     */
    RoleInfo createRole(RoleInfo role);

    /**
     * Function that delete roles from client realm roles.
     * 
     * @param roleId
     *            {@link String} id of role that we want to remove
     */
    void deleteRole(String roleId);

    /**
     * Function that return role base on role ID.
     * 
     * @param roleId
     *            {@link String} ID value of role
     * @return {@link RoleInfo} object of rule
     */
    RoleInfo getRoleBaseOnId(String roleId);

    /**
     * Function that return client role base on role ID.
     * 
     * @param roleId
     *            {@link String} ID value of role
     * @param clientId
     *            {@link String} ID value of client
     * @return {@link RoleInfo} object of rule
     */
    RoleInfo getClientsRoleBaseOnId(String roleId, String clientId);

    /**
     * Returns all roles that identity have assigned.
     * 
     * @param contactNumber
     *            {@link String} ID of identity for what we want to find roles
     * 
     * @return {@link List} of {@link RoleInfo} all roles that identity have assigned
     */
    List<String> getIdentityRoles(String contactNumber);

    /**
     * Return Role Resource by unique ID {@code roleId}.
     * 
     * @param roleId
     *            ID of role
     * @return {@link RoleResource} role resource specified by Role ID
     */
    Optional<RoleResource> findRoleResource(String roleId);

    /**
     * Return roles binary representation for identity identified by {@code contactNumber}.
     * 
     * @param userRepresentation
     *            user representation
     * @return {@link String} binary representation of roles of specified identity
     */
    String getRolesBinary(UserRepresentation userRepresentation);

    /**
     * Returns all roles that account have assigned.
     * 
     * @param groupResource
     *            account for what we want to find roles
     * @param effective
     *            roles effective counted or original selected
     * 
     * @return {@link List} of {@link RoleInfo} all roles that account have assigned
     */
    List<RoleInfo> getAccountRoles(GroupResource groupResource, boolean effective);
    List<RoleRepresentation> getAccountRolesRepresentation(GroupResource group, boolean effective);

    /**
     * Returns all rights that identity have assigned.
//     * @param contactNumber
//     *            {@link String} ID of identity for what we want to find roles
     * 
     * @return {@link List} of {@link RoleInfo} all rights that identity have assigned
     */
    List<String> getIdentityRights(GroupResource groupResource, String contactNumber);

    List<String> getIdentityRolesNav4(String nav4Id);

    List<String> getIdentityRoles(UserRepresentation userRepresentation);

    List<RoleInfo> getRoles();


}