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
