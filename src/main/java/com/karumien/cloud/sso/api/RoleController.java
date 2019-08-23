/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.karumien.cloud.sso.api.handler.RolesApi;
import com.karumien.cloud.sso.api.model.RoleBaseInfo;
import com.karumien.cloud.sso.service.RoleService;

import io.swagger.annotations.Api;

/**
 * REST Controller for Role Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
@RestController
@Api(value = "Role Service", description = "REST API for Role Service", tags = { "Role Service" })
public class RoleController implements RolesApi {   
    
	@Autowired
    private RoleService roleService;
	
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<RoleBaseInfo> createRole(@Valid RoleBaseInfo role) {
        return new ResponseEntity<>(roleService.createRole(role), HttpStatus.CREATED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteRole(String id) {
    	roleService.deleteRole(); 
    	return new ResponseEntity<>(HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<RoleBaseInfo> getRole(String id) {
    	return new ResponseEntity<>(roleService.getRoleBaseOnId(id), HttpStatus.OK);
    }
}
