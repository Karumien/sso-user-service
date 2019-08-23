package com.karumien.cloud.sso.service;

import java.util.List;

import com.karumien.cloud.sso.api.model.RoleBaseInfo;

/**
 * Service provides informations about user Roles.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec"</a>
 * @since 1.0, 22. 8. 2019 18:58:57
 */

public interface RoleService {
	
	/**
	 *  Function that create role base on input object to realm
	 * @param role {@link RoleBaseInfo} object of role
	 * @return {@link RoleBaseInfo} new created role as return parameter
	 */
	RoleBaseInfo createRole(RoleBaseInfo role);
	
	
	/**
	 * Function that delete roles from client realm roles 
	 * @param id {@link String} id of role that we want to remove
	 */
	void deleteRole(String id);
	
	/**
	 * Function that return role base on role id
	 * @param id {@link String} id value of role
	 * @return {@link RoleBaseInfo} object of rule 
	 */
	RoleBaseInfo getRoleBaseOnId(String id);
	
	/**
	 * Function to assign input role to user 
	 * @param user_id {@link String} id of user that we want to assign role 
	 * @param role {@link RoleBaseInfo} object of role we want to ussigne to user
	 */
	void assigneRoleToClient(String clientId, RoleBaseInfo role);
	
	/**
	 * Function that return all role that user have signed
	 * @return user_id {@link String} id of user for what we want to find roles
	 */
	List<RoleBaseInfo> getAllRoleForUser(String user_id);
	
	/**
	 * Function that remove role from user
	 * @param userId {@link String} id of user from we want to remove role
	 * @param role_id {@link String} id of role that we want to remove
	 */
	void deleteRoleForUser(String userId, String role_id);

}
