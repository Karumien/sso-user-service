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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karumien.cloud.sso.api.model.AuthorizationResponse;
import com.karumien.cloud.sso.api.model.Policy;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;
import com.karumien.cloud.sso.internal.ImpersonateConfig;
import com.karumien.cloud.sso.internal.ImpersonateTokenManager;

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

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private PasswordGeneratorService passwordGeneratorService;

    protected AuthorizationResponse mapping(AccessTokenResponse token) {
        
        AuthorizationResponse auth = new AuthorizationResponse();
        auth.setAccessToken(token.getToken());
        auth.setExpiresIn(token.getExpiresIn());
        auth.setRefreshToken(token.getRefreshToken());
        auth.setRefreshExpiresIn(token.getRefreshExpiresIn());
        auth.setTokenType(token.getTokenType());
        
        return auth;           
    }

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

        // List<KeyMetadataRepresentation> keys = keycloak.realm(REALM).get keys().getKeyMetadata().getKeys();
        //
        // String publicKeyString = null;
        // for (KeyMetadataRepresentation key : keys) {
        // if (key.getKid().equals(jwsHeader.getKeyId())) {
        // publicKeyString = key.getPublicKey();
        // break;
        // }
        // }

        //
        // RealmResource realmResource = keycloak.realm(realm);
        // KeyResource keys = realmResource.keys();
        // keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getAlgorithm()));
        // keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getCertificate()));
        // keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getKid()));
        // keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getProviderId()));
        // keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getPublicKey()));
        // keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getStatus()));
        // keys.getKeyMetadata().getKeys().forEach(key -> System.out.println(key.getType()));

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
       
        TokenManager tokenManager = KeycloakBuilder.builder().serverUrl(adminServerUrl).realm(realm)
            .clientId(clientId).username(username).password(password)
            .build().tokenManager();
            
        return mapping(tokenManager.getAccessToken());            
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationResponse loginByClientCredentials(String clientId, String clientSecret) {
        TokenManager tokenManager = KeycloakBuilder.builder().serverUrl(adminServerUrl).realm(realm).clientId(clientId).clientSecret(clientSecret).build()
                .tokenManager();

        return mapping(tokenManager.getAccessToken());            
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationResponse loginByToken(String refreshToken) {
        TokenManager tokenManager = Keycloak.getInstance(adminServerUrl, realm, clientId, refreshToken).tokenManager();

        return mapping(tokenManager.getAccessToken());            
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
        policy.setTranslation(getPolicyTranslation(LocaleContextHolder.getLocale(), policy));

        return policy;
    }

    private String getPolicyTranslation(Locale locale, Policy policy) {

        boolean finalAnd = false;
        int minimalLength = policy.getMinLength() != null && policy.getMinLength() > 0 ? policy.getMinLength() : 8;

        StringBuilder sb = new StringBuilder();
        finalAnd = policyRule(sb, locale, "policy.description.lower", "s", policy.getMinLowerCase()) || finalAnd;
        finalAnd = policyRule(sb, locale, "policy.description.upper", "s", policy.getMinUpperCase()) || finalAnd;
        finalAnd = policyRule(sb, locale, "policy.description.number", "s", policy.getMinDigits()) || finalAnd;
        finalAnd = policyRule(sb, locale, "policy.description.special", "s", policy.getMinSpecialChars()) || finalAnd;
        finalAnd = policyRule(sb, locale, "policy.description.history", "", policy.getPasswordHistory()) || finalAnd;

        if (finalAnd) {
            sb.append(" ").append(messageSource.getMessage("policy.description.and", null, locale)).append(" ");
        }

        sb.append(messageSource.getMessage("policy.description.length", new Object[] { minimalLength }, locale)).append(".");

        return sb.toString();
    }

    private boolean policyRule(StringBuilder sb, Locale locale, String key, String keyAdvances, Integer count) {

        if (count == null || count <= 0) {
            return false;
        }

        sb.append(sb.length() == 0 ? messageSource.getMessage("policy.description", null, locale) + " " : ", ");
        sb.append(messageSource.getMessage(count == 1 ? key : key + keyAdvances, new Object[] { count }, locale));
        return true;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationResponse loginByImpersonator(String refreshToken, String clientId, String username) {

        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder().connectionPoolSize(10);

        List<UserRepresentation> users = keycloak.realm(realm).users().search(username);
        if (users.isEmpty()) {
            throw new IdentityNotFoundException("username = " + username);
        }

        ImpersonateTokenManager tokenManager = new ImpersonateTokenManager(
                new ImpersonateConfig(this.adminServerUrl, realm, users.get(0).getId(), null, this.clientId, null, OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE),
                clientBuilder.build(), refreshToken);

        return mapping(tokenManager.getAccessToken());            
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generatePassword() {
        return passwordGeneratorService.generate(getPasswordPolicy());
    }

}