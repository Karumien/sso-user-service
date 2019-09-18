/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karumien.cloud.sso.api.model.AuthorizationResponse;
import com.karumien.cloud.sso.api.model.Policy;

/**
 * Implementation of {@link AuthService} for authentication tokens management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 13. 8. 2019 22:07:27
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String adminServerUrl;
    
    @Value("${keycloak.client-id}")
    private String clientId;
    
    @Autowired
    protected Keycloak keycloak;

    protected static PublicKey toPublicKey(String publicKeyString) {
        try {
          byte[] publicBytes = Base64.getDecoder().decode(publicKeyString);
          X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
          KeyFactory keyFactory = KeyFactory.getInstance("RSA");
          return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
          return null;
        }
      }
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getPublicKey() {
        
//        List<KeyMetadataRepresentation> keys = keycloak.realm(REALM).get keys().getKeyMetadata().getKeys();
//
//        String publicKeyString = null;
//        for (KeyMetadataRepresentation key : keys) {
//          if (key.getKid().equals(jwsHeader.getKeyId())) {
//            publicKeyString = key.getPublicKey();
//            break;
//          }
//        }

//        
//        RealmResource realmResource = keycloak.realm(realm);
//        KeyResource keys = realmResource.keys();
//        keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getAlgorithm()));
//        keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getCertificate()));
//        keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getKid()));
//        keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getProviderId()));
//        keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getPublicKey()));
//        keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getStatus()));
//        keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getType()));
        
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> realmInfo;
        try {
            realmInfo = om.readValue(new URL(adminServerUrl + "/realms/" + realm).openStream(), Map.class);
            return (String) realmInfo.get("public_key");
        } catch (Exception e) {
            throw new IllegalStateException("Can't retreive public key", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logoutByToken(String token) {
        keycloak.tokenManager().invalidate(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationResponse loginByUsernamePassword(String username, String password) {
        TokenManager tokenManager = KeycloakBuilder.builder()
                .serverUrl(adminServerUrl)
                .realm(realm).clientId(clientId)
                .username(username).password(password).build()
                .tokenManager();

        AuthorizationResponse auth = new AuthorizationResponse();
        auth.setAccessToken(tokenManager.getAccessToken().getToken());
        auth.setExpiresIn(tokenManager.getAccessToken().getExpiresIn());
        auth.setRefreshToken(tokenManager.refreshToken().getToken());
        auth.setRefreshExpiresIn(tokenManager.refreshToken().getExpiresIn());
        auth.setTokenType(tokenManager.getAccessToken().getTokenType());
        return auth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationResponse loginByClientCredentials(String clientId, String clientSecret) {
        TokenManager tokenManager = KeycloakBuilder.builder()
                .serverUrl(adminServerUrl).realm(realm)
                .clientId(clientId).clientSecret(clientSecret).build()
                .tokenManager();

        AuthorizationResponse auth = new AuthorizationResponse();
        auth.setAccessToken(tokenManager.getAccessToken().getToken());
        auth.setExpiresIn(tokenManager.getAccessToken().getExpiresIn());
        auth.setRefreshToken(tokenManager.refreshToken().getToken());
        auth.setRefreshExpiresIn(tokenManager.refreshToken().getExpiresIn());
        auth.setTokenType(tokenManager.getAccessToken().getTokenType());
        return auth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationResponse loginByToken(String refreshToken) {
        TokenManager tokenManager = 
                Keycloak.getInstance(adminServerUrl, realm, clientId, refreshToken).tokenManager();

        AuthorizationResponse auth = new AuthorizationResponse();
        auth.setAccessToken(tokenManager.getAccessToken().getToken());
        auth.setExpiresIn(tokenManager.getAccessToken().getExpiresIn());
        auth.setRefreshToken(tokenManager.refreshToken().getToken());
        auth.setRefreshExpiresIn(tokenManager.refreshToken().getExpiresIn());
        auth.setTokenType(tokenManager.getAccessToken().getTokenType());
        return auth;        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Policy getPasswordPolicy() {

        String policyDescription = keycloak.realm(realm).toRepresentation().getPasswordPolicy();

        Policy policy = new Policy();
        policy.setValue(policyDescription);
        policy.setHashAlgorithm(extract("hashAlgorithm", policyDescription, String.class));
        policy.setMinSpecialChars(extract("specialChars", policyDescription, Integer.class));
        policy.setMinUpperCase(extract("upperCase", policyDescription, Integer.class));
        policy.setMinLowerCase(extract("lowerCase", policyDescription, Integer.class));
        policy.setPasswordHistory(extract("passwordHistory", policyDescription, Integer.class));
        policy.setMinDigits(extract("digits", policyDescription, Integer.class));
        policy.setHashIterations(extract("hashIterations", policyDescription, Integer.class));

        if (extract("passwordBlacklist", policyDescription, String.class) != null) {
            policy.setPasswordBlacklist(true);
        }

        if (extract("notUsername", policyDescription, String.class) != null) {
            policy.setNotUseUsername(true);
        }

        policy.setRegexPattern(extract("regexPattern", policyDescription, String.class));
        policy.setPasswordExpireDays(extract("forceExpiredPasswordChange", policyDescription, Integer.class));
        policy.setMinLength(extract("length", policyDescription, Integer.class));

        return policy;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T extract(String code, String policyDescription, Class<T> clazz) {

        if (policyDescription == null || !policyDescription.contains(code)) {
            return null;
        }

        String extractedValue = policyDescription.substring(policyDescription.indexOf(code) + code.length() + 1);
        extractedValue = extractedValue.substring(0, extractedValue.indexOf(")"));

        if (Integer.class.equals(clazz)) {
            return (T) Integer.valueOf(extractedValue);
        }

        return (T) extractedValue;
    }

}