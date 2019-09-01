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

import com.karumien.cloud.sso.api.handler.AuthApi;
import com.karumien.cloud.sso.api.model.AuthorizationRequest;
import com.karumien.cloud.sso.api.model.AuthorizationResponse;
import com.karumien.cloud.sso.api.model.GrantType;
import com.karumien.cloud.sso.service.AuthService;

import io.swagger.annotations.Api;

/**
 * REST Controller for User Service (API).
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 13. 8. 2019 11:15:51 
 */
@RestController
@Api(value = "Authentication Service", description = "Authentication Process", tags = { "Authentication Service" })
public class AuthController implements AuthApi  {

    @Autowired
    private AuthService authService;
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ResponseEntity<Void> publicKey() {
        return new ResponseEntity(authService.getPublicKey(), HttpStatus.OK);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<AuthorizationResponse> login(@Valid AuthorizationRequest user) {
        if (user.getGrantType() == null) {
            throw new IllegalArgumentException("grant_type can't be emoty");
        }
        
        AuthorizationResponse response = null;

        switch (user.getGrantType()) {
        case REFRESH_TOKEN:
            response = authService.loginByToken(user.getRefreshToken());
            break;
        case PASSWORD:
            response = authService.loginByUsernamePassword(user.getUsername(), user.getPassword());
            break;
        case CLIENT_CREDENTIALS:
            response = authService.loginByClientCredentials(user.getClientId(), user.getClientSecret());
            break;
        default:
            break;
        }
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<Void> logout(@Valid AuthorizationRequest user) {
    
        if (user.getGrantType() != GrantType.REFRESH_TOKEN) {
            throw new IllegalArgumentException("Use grant_type refresh_token for logout");
        }
        
        authService.logoutByToken(user.getRefreshToken());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);   
    }
}
