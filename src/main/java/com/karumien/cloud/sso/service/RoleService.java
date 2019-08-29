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

import com.karumien.cloud.sso.api.model.RoleBaseInfo;

/**
 * Service provides informations about user Roles.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec"</a>
 * @since 1.0, 22. 8. 2019 18:58:57
 */

public interface RoleService {
	
	RoleBaseInfo createRole(RoleBaseInfo role);
	
	void deleteRole();
	
	RoleBaseInfo getRoleBaseOnId(String id);
	
	void assigneRoleToClient(String clientId, RoleBaseInfo role);

}
