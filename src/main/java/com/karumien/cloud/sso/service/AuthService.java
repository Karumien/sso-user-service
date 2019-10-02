/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import com.karumien.cloud.sso.api.model.AuthorizationResponse;
import com.karumien.cloud.sso.api.model.Policy;

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
     * Returns configuration of Password Policy
     * 
     * @return {@link Policy} configuration of Password Policy
     */
    Policy getPasswordPolicy();

    /**
     * Logout user by token.
     * 
     * @param token
     *            representation of token
     */
    void logoutByToken(String token);

    AuthorizationResponse loginByUsernamePassword(String username, String password);

    AuthorizationResponse loginByClientCredentials(String clientId, String clientSecret);

    AuthorizationResponse loginByToken(String refreshToken);

    AuthorizationResponse loginByImpersonator(String refreshToken, String clienId, String username);

}
