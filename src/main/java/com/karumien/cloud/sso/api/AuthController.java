/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.api;

import java.io.ByteArrayInputStream;

import javax.validation.Valid;

import org.apache.commons.codec.binary.Base64;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.jayway.jsonpath.JsonPath;
import com.karumien.cloud.sso.api.handler.AuthApi;
import com.karumien.cloud.sso.api.model.AuthorizationRequest;
import com.karumien.cloud.sso.api.model.AuthorizationResponse;
import com.karumien.cloud.sso.api.model.ErrorCode;
import com.karumien.cloud.sso.api.model.ErrorMessage;
import com.karumien.cloud.sso.api.model.GrantType;
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.exceptions.UnsupportedApiOperationException;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ResponseEntity<AuthorizationResponse> login(@Valid AuthorizationRequest user) {
        if (user.getGrantType() == null) {
            throw new IllegalArgumentException("grant_type can't be emoty");
        }
        
        AuthorizationResponse response = null;

        try {
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
            case IMPERSONATE:
                response = authService.loginByImpersonator(user.getRefreshToken(), user.getClientId(), user.getUsername());
                break;            
            default:
                throw new UnsupportedApiOperationException("Unknown grant_type " + user.getGrantType());
            }
        
        } catch (javax.ws.rs.BadRequestException e) {
            ErrorMessage error = new ErrorMessage().errcode(ErrorCode.ERROR).errno(400)
                    .errmsg(JsonPath.parse((ByteArrayInputStream) e.getResponse().getEntity()).read("$.error_description", String.class));
            return new ResponseEntity(error, HttpStatus.BAD_REQUEST);
        } catch (javax.ws.rs.NotAuthorizedException e) {
            ErrorMessage error = new ErrorMessage().errcode(ErrorCode.ERROR).errno(401)
                    .errmsg(JsonPath.parse((ByteArrayInputStream) e.getResponse().getEntity()).read("$.error_description", String.class));
            return new ResponseEntity(error, HttpStatus.UNAUTHORIZED);
        }
        
        if (response.getAccessToken() != null) {
            MDC.put("access_token", decodeJWT(response.getAccessToken()));
            System.out.println(decodeJWT(response.getAccessToken()));
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
    

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Policy> getPasswordPolicy() {
        return new ResponseEntity<>(authService.getPasswordPolicy(), HttpStatus.OK);
    }
    
    private String decodeJWT(String jwtToken) {
    
        String[] split_string = jwtToken.split("\\.");
        //String base64EncodedHeader = split_string[0];
        String base64EncodedBody = split_string[1];
        // String base64EncodedSignature = split_string[2];
    
        Base64 base64Url = new Base64(true);
        // String header = new String(base64Url.decode(base64EncodedHeader));
    
        return new String(base64Url.decode(base64EncodedBody));
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<Void> generatePassword() {
        return new ResponseEntity(authService.generatePassword(), HttpStatus.OK);
    }
}
