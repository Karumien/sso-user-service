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

import com.karumien.cloud.sso.api.handler.UserApi;
import com.karumien.cloud.sso.api.model.UserBaseInfoDTO;
import com.karumien.cloud.sso.service.UserService;

/**
 * REST Controller for User Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:15:51 
 */
public class UserController implements UserApi {

    @Autowired
    private UserService userService;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<UserBaseInfoDTO> createUser(@Valid UserBaseInfoDTO user) {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }
    
}
