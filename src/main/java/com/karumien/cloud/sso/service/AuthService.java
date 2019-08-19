/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import com.karumien.cloud.sso.api.model.AuthorizationResponseDTO;

/**
 * Service provides scenatios for authentication's tokens management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 7. 2019 22:07:27
 */
public interface AuthService {

    /**
     * Returns public key from realm
     * 
     * @return String public key
     */
    String getPublicKey();

    /**
     * Logout user by token.
     * 
     * @param token
     *            representation of token
     */
    void logoutByToken(String token);

    AuthorizationResponseDTO loginByUsernamePassword(String username, String password);

    AuthorizationResponseDTO loginByClientCredentials(String clientId, String clientSecret);

    AuthorizationResponseDTO loginByToken(String refreshToken);

}
