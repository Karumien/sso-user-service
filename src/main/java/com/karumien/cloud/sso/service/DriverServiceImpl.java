package com.karumien.cloud.sso.service;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.karumien.cloud.sso.api.model.DriverBaseInfo;
import com.karumien.cloud.sso.api.model.DriverPIN;
import com.karumien.cloud.sso.api.model.UserBaseInfo;
import com.karumien.cloud.sso.exceptions.UnsupportedApiOperationException;

@Service
public class DriverServiceImpl implements DriverService{

	@Autowired
	UserService userService;
	
	@Autowired
	RoleService rolesService;
	
	private static final String DRIVERROLE = "DRIVER_ROLE";
	
	@Override
	public DriverBaseInfo createDriver(@Valid DriverBaseInfo driver) {
		UserBaseInfo driverUser = null;
		if(userService.isUserExists(driver.getUsername())) {
			driverUser = userService.getUser(driver.getId());
		} else {
			UserBaseInfo userDriver = remapFromDriverBaseToUserBase(driver);
			driverUser = userService.createUser(userDriver);			
		}
		rolesService.assigneRoleToClient(driverUser.getId(), rolesService.getRoleBaseOnId(DRIVERROLE));
		return remapFromUserBaseToDriverBase(driverUser);
	}

	@Override
	public void createPinForTheDriver(String id, @Valid DriverPIN pin) {
		throw new UnsupportedApiOperationException();
	}

	@Override
	public void deleteDriverUser(String id) {
		UserBaseInfo user = userService.getUser(id);
		if(rolesService.getAllRoleForUser(user.getId()).stream().anyMatch(role -> role.getName().equals(DRIVERROLE))) {
			userService.deleteUser(id);
		} else {
			rolesService.deleteRoleForUser(user.getId(),  rolesService.getRoleBaseOnId(DRIVERROLE).getId());
		}
	}
	
	private UserBaseInfo remapFromDriverBaseToUserBase(@Valid DriverBaseInfo driver) {
		UserBaseInfo user = new UserBaseInfo();
		user.setUsername(driver.getUsername());
		user.setFirstName(driver.getFirstName());
		user.setLastName(driver.getLastName());
		user.setEmail(driver.getEmail());
		user.setId(driver.getId() != null ? driver.getId() : null);
		return user;
	}
	
	private DriverBaseInfo remapFromUserBaseToDriverBase(@Valid UserBaseInfo user) {
		DriverBaseInfo driver = new DriverBaseInfo();
		driver.setUsername(user.getUsername());
		driver.setFirstName(user.getFirstName());
		driver.setLastName(user.getLastName());
		driver.setEmail(user.getEmail());
		driver.setId(user.getId() != null ? user.getId() : null);
		return driver;
	}

}
