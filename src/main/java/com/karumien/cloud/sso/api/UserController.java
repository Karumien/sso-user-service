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
import com.karumien.cloud.sso.api.model.UserBaseInfoDTO;
import com.karumien.cloud.sso.service.UserService;

/**
 * REST Controller for User Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
@RestController
public class UserController implements UsersApi {

    @Autowired
    private UserService userService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserBaseInfoDTO> createUser(@Valid UserBaseInfoDTO user) {
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
    
}
