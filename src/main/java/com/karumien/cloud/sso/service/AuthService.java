/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import com.karumien.cloud.sso.api.model.AuthorizationResponse;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.PasswordPolicy;
import com.karumien.cloud.sso.api.model.UsernamePolicy;

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
     * @return {@link PasswordPolicy} configuration of Password Policy
     */
    PasswordPolicy getPasswordPolicy();

    /**
     * Logout user by token.
     * 
     * @param token
     *            representation of token
     */
    void logoutByToken(String token);

    AuthorizationResponse loginByUsernamePassword(String clientId, String clientSecret, String username, String password);

    AuthorizationResponse loginByClientCredentials(String clientId, String clientSecret);

    AuthorizationResponse loginByToken(String clientId, String refreshToken);

    AuthorizationResponse loginByImpersonator(String clienId, String clientSecret, String refreshToken, String username);

    IdentityInfo loginByPin(String clientId, String username, String pin);

    String generatePassword();

    UsernamePolicy getUsernamePolicy();


}
