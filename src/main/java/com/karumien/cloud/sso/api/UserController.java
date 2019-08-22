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

import com.karumien.cloud.sso.api.handler.UsersApi;
import com.karumien.cloud.sso.api.model.Credentials;
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.api.model.UserBaseInfo;
import com.karumien.cloud.sso.exceptions.PolicyPasswordException;
import com.karumien.cloud.sso.service.UserService;

import io.swagger.annotations.Api;

/**
 * REST Controller for User Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
@RestController
@Api(value = "User Service", description = "REST API for User Service", tags = { "User Service" })
public class UserController implements UsersApi {

    @Autowired
    private UserService userService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserBaseInfo> createUser(@Valid UserBaseInfo user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteUser(String id) {
        userService.deleteUser(id);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Policy> getPasswordPolicy() {
        return new ResponseEntity<>(userService.getPasswordPolicy(), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Policy> createUserCredentials(String id, @Valid Credentials credentials) {
        try {
            userService.createUserCredentials(id, credentials);
        } catch (PolicyPasswordException e) {
            return new ResponseEntity<>(userService.getPasswordPolicy(), HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserBaseInfo> getUser(String id) {
        return new ResponseEntity<>(userService.getUser(id), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> impersonateUser(String id) {
        userService.impersonateUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> logoutUser(String id) {
        userService.logoutUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
}
