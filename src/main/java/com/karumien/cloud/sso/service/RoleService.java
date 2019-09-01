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

import org.keycloak.admin.client.resource.RoleResource;

import com.karumien.cloud.sso.api.model.RoleInfo;

/**
 * Service provides informations about user Roles.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec"</a>
 * @since 1.0, 22. 8. 2019 18:58:57
 */
public interface RoleService {

    /**
     * Function that create role base on input object to realm
     * 
     * @param role
     *            {@link RoleInfo} object of role
     * @return {@link RoleInfo} new created role as return parameter
     */
    RoleInfo createRole(RoleInfo role);

    /**
     * Function that delete roles from client realm roles
     * 
     * @param roleId
     *            {@link String} id of role that we want to remove
     */
    void deleteRole(String roleId);

    /**
     * Function that return role base on role id
     * 
     * @param roleId
     *            {@link String} id value of role
     * @return {@link RoleInfo} object of rule
     */
    RoleInfo getRoleBaseOnId(String roleId);

    /**
     * Function to assign input role to user
     * 
     * @param user_id
     *            {@link String} id of user that we want to assign role
     * @param role
     *            {@link RoleInfo} object of role we want to ussigne to user
     */
    void assigneRoleToClient(String clientId, RoleInfo role);

    /**
     * Function that return all role that user have signed
     * 
     * @return user_id {@link String} id of user for what we want to find roles
     */
    List<RoleInfo> getAllRoleForUser(String crmContactId);

    /**
     * Function that remove role from user
     * 
     * @param crmContactId
     *            {@link String} id of user from we want to remove role
     * @param roleId
     *            {@link String} id of role that we want to remove
     */
    void deleteRoleForUser(String crmContactId, String roleId);

    /**
     * 
     * @param roleId
     * @return
     */
    Optional<RoleResource> findRole(String roleId);

}
