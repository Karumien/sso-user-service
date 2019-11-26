/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.internal;

import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;
import static org.keycloak.OAuth2Constants.PASSWORD;
import static org.keycloak.OAuth2Constants.REFRESH_TOKEN;
import static org.keycloak.OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE;

/**
 * Added missing functionality of impersonate by login method.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 2. 10. 2019 0:23:22 
 */
public class AdvancedTokenConfig {

    private String serverUrl;
    private String realm;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String grantType;

    public AdvancedTokenConfig(String serverUrl, String realm, String username, String password, String clientId, String clientSecret) {
        this(serverUrl, realm, username, password, clientId, clientSecret, PASSWORD);
    }

    public AdvancedTokenConfig(String serverUrl, String realm, String username, String password, String clientId, String clientSecret, String grantType) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.username = username;
        this.password = password;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        checkGrantType(grantType);
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isPublicClient() {
        return clientSecret == null;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
        checkGrantType(grantType);
    }

    public static void checkGrantType(String grantType) {
        if (grantType != null && !PASSWORD.equals(grantType) && !CLIENT_CREDENTIALS.equals(grantType) 
                && !TOKEN_EXCHANGE_GRANT_TYPE.equals(grantType) && !REFRESH_TOKEN.equals(grantType)) {
            throw new IllegalArgumentException("Unsupported grantType: " + grantType +
                    " (only " + PASSWORD + ", " + CLIENT_CREDENTIALS + " and " + TOKEN_EXCHANGE_GRANT_TYPE + " are supported)");
        }
    }

}
