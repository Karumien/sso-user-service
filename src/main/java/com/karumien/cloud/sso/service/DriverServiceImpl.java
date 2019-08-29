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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karumien.cloud.sso.api.model.DriverInfo;
import com.karumien.cloud.sso.api.model.DriverPin;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.exceptions.UnsupportedApiOperationException;

@Service
public class DriverServiceImpl implements DriverService {

	@Autowired
	private IdentityService identityService;
	
	@Autowired
	private RoleService rolesService;
	
	private static final String DRIVERROLE = "DRIVER_ROLE";
	
	@Override
	public DriverInfo createDriver(@Valid DriverInfo driver) {
		IdentityInfo driverUser = null;
		if(identityService.isIdentityExists(driver.getUsername())) {
			driverUser = identityService.getIdentity(driver.getId());
		} else {
			IdentityInfo userDriver = remapFromDriverBaseToUserBase(driver);
			driverUser = identityService.createIdentity(userDriver);			
		}
		rolesService.assigneRoleToClient(driverUser.getCrmContactId(), rolesService.getRoleBaseOnId(DRIVERROLE));
		return remapFromUserBaseToDriverBase(driverUser);
	}
	
	@Override
	public void createPinForTheDriver(String id, @Valid DriverPin pin) {
		throw new UnsupportedApiOperationException();
	}

	@Override
	public void deleteDriverUser(String id) {
		IdentityInfo user = identityService.getIdentity(id);
		if(rolesService.getAllRoleForUser(user.getCrmContactId()).stream().anyMatch(role -> role.getName().equals(DRIVERROLE))) {
			identityService.deleteIdentity(id);
		} else {
			rolesService.deleteRoleForUser(user.getCrmContactId(),  rolesService.getRoleBaseOnId(DRIVERROLE).getId());
		}
	}
	
	private IdentityInfo remapFromDriverBaseToUserBase(@Valid DriverInfo driver) {
		IdentityInfo user = new IdentityInfo();
		user.setUsername(driver.getUsername());
		user.setFirstName(driver.getFirstName());
		user.setLastName(driver.getLastName());
		user.setEmail(driver.getEmail());
		user.setCrmContactId(driver.getId() != null ? driver.getId() : null);
		return user;
	}
	
	private DriverInfo remapFromUserBaseToDriverBase(@Valid IdentityInfo user) {
		DriverInfo driver = new DriverInfo();
		driver.setUsername(user.getUsername());
		driver.setFirstName(user.getFirstName());
		driver.setLastName(user.getLastName());
		driver.setEmail(user.getEmail());
		driver.setId(user.getCrmContactId() != null ? user.getCrmContactId() : null);
		return driver;
	}

}
