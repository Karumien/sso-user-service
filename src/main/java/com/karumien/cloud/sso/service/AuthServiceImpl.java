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
import java.util.ArrayList;
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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karumien.cloud.sso.api.model.AuthorizationResponse;
import com.karumien.cloud.sso.api.model.GrantType;
import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.PasswordPolicy;
import com.karumien.cloud.sso.api.model.UsernamePolicy;
import com.karumien.cloud.sso.exceptions.AttributeNotFoundException;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;
import com.karumien.cloud.sso.internal.AdvancedTokenConfig;
import com.karumien.cloud.sso.internal.AdvancedTokenManager;

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
    private IdentityService identityService;

    @Autowired
    private RoleService roleService;
   
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
    public AuthorizationResponse loginByUsernamePassword(String clientId, String clientSecret, String username, String password) {
        String client = StringUtils.hasText(clientId) ? clientId : this.clientId;
        MDC.put("clientId", client);
        MDC.put("usr", username);
        
        TokenManager tokenManager = KeycloakBuilder.builder().serverUrl(adminServerUrl).realm(realm)
            .clientId(client).clientSecret(clientSecret)
            .username(username).password(password).grantType(GrantType.PASSWORD.toString())           
            .build().tokenManager();
            
        return mapping(tokenManager.getAccessToken());            
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationResponse loginByClientCredentials(String clientId, String clientSecret) {
        MDC.put("clientId", clientId);

        TokenManager tokenManager = KeycloakBuilder.builder().serverUrl(adminServerUrl)
            .realm(realm).clientId(clientId).clientSecret(clientSecret).grantType(GrantType.CLIENT_CREDENTIALS.toString())
            .build().tokenManager();

        return mapping(tokenManager.getAccessToken());            
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorizationResponse loginByToken(String clientId, String refreshToken) {
        String client = StringUtils.hasText(clientId) ? clientId : this.clientId;
        MDC.put("clientId", client);

        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder().connectionPoolSize(10);

        AdvancedTokenManager tokenManager = new AdvancedTokenManager(
                new AdvancedTokenConfig(this.adminServerUrl, realm, null, null, client, null, OAuth2Constants.REFRESH_TOKEN),
                clientBuilder.build(), refreshToken);
        
        return mapping(tokenManager.getAccessToken());            
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IdentityInfo loginByPin(String clientId, String username, String pin) {

        String client = StringUtils.hasText(clientId) ? clientId : this.clientId;
        MDC.put("clientId", client);
        MDC.put("usr", username);
        
        if (!StringUtils.hasText(pin)) {
            return null;
        }
            
        UserRepresentation user = identityService.findIdentityByUsername(username).orElseThrow(() -> new IdentityNotFoundException("username " + username));
        IdentityInfo identityInfo = identityService.mapping(user, false);
        try {
            if (pin.equals(identityService.getPinOfIdentityDriver(identityInfo.getContactNumber()).getPin())) {
                identityInfo.setBinaryRights(roleService.getRolesBinary(user));
                return identityInfo;
            }
        } catch (AttributeNotFoundException e) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsernamePolicy getUsernamePolicy() {

        UsernamePolicy policy = new UsernamePolicy();
        policy.setMinLength(8);
        policy.setUseDigits(true);
        policy.setUseUpperCase(false);
        policy.setUseLowerCase(true);
        policy.setCanSpecialCharStart(false);
        policy.setCanSpecialCharEnd(false);
        policy.setCanSpecialCharRepeated(false);
        policy.setSpecialCharsOnly("@._-");
        policy.setMinLength(8);
        policy.setTranslation(getPolicyTranslation(LocaleContextHolder.getLocale(), policy));
        
        return policy;
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PasswordPolicy getPasswordPolicy() {

        String policyDescription = keycloak.realm(realm).toRepresentation().getPasswordPolicy();

        PasswordPolicy policy = new PasswordPolicy();
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
        
        if (policy.getMinLength() == null || policy.getMinLength() == 0) {
            policy.setMinLength(1);
        }
        
        policy.setTranslation(getPolicyTranslation(LocaleContextHolder.getLocale(), policy));

        return policy;
    }

    private String getPolicyTranslation(Locale locale, PasswordPolicy policy) {

        List<String> texts = new ArrayList<>();
        
        policyRule(texts, locale, "policy.length", "s", policy.getMinLength());
        policyRule(texts, locale, "policy.password.lower", "s", policy.getMinLowerCase());
        policyRule(texts, locale, "policy.password.upper", "s", policy.getMinUpperCase());
        policyRule(texts, locale, "policy.password.number", "s", policy.getMinDigits());
        policyRule(texts, locale, "policy.password.special", "s", policy.getMinSpecialChars());
        policyRule(texts, locale, "policy.password.history", "", policy.getPasswordHistory());

        return messageSource.getMessage("policy.password", null, locale)
            + join(texts, messageSource.getMessage("policy.and", null, locale)) 
            + ".";
    }

   
    private String getPolicyTranslation(Locale locale, UsernamePolicy policy) {

        List<String> texts = new ArrayList<>();

        policyRule(texts, locale, "policy.username.digits", policy.isUseDigits());
        policyRule(texts, locale, "policy.username.uppers", policy.isUseUpperCase());
        policyRule(texts, locale, "policy.username.lowers", policy.isUseLowerCase());
        policyRule(texts, locale, "policy.username.specials", policy.getSpecialCharsOnly());
        policyRule(texts, locale, "policy.username.starts", policy.isCanSpecialCharStart());
        policyRule(texts, locale, "policy.username.ends", policy.isCanSpecialCharRepeated());
        policyRule(texts, locale, "policy.username.repeated", policy.isCanSpecialCharRepeated());
        policyRule(texts, locale, "policy.length", "s", policy.getMinLength());
        
        return messageSource.getMessage("policy.username", null, locale)
                + join(texts, messageSource.getMessage("policy.and", null, locale)) 
                + ".";
    }

    
    private void policyRule(List<String> texts, Locale locale, String key, String specialCharsOnly) {
      
        if (specialCharsOnly == null) {
            return;
        }
        
        texts.add(messageSource.getMessage(key, new Object[] { specialCharsOnly }, locale));
    }

    private void policyRule(List<String> texts, Locale locale, String key, Boolean useIt) {

        if (useIt == null) {
            return;
        }
        
        texts.add(messageSource.getMessage(Boolean.TRUE.equals(useIt) ? "policy.can" : "policy.cannot", null, locale)
            + " " + messageSource.getMessage(key, null, locale));
    }

    private void policyRule(List<String> texts, Locale locale, String key, String keyAdvances, Integer count) {
        
        if (count == null || count <= 0) {
            return;
        }
        
        texts.add((key.equals("policy.length") ? messageSource.getMessage("policy.contain", null, locale) + " " : "")
             + messageSource.getMessage(count == 1 ? key : key + keyAdvances, new Object[] { count }, locale));
    }
    
    private StringBuilder join(List<String> texts, String last) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texts.size(); i++) {
            if (i == 0) {
                sb.append(" ");
            } else {
                if (i == texts.size() - 1) {
                    sb.append(" " + last + " ");
                } else {
                    sb.append(", ");
                }
            }
            sb.append(texts.get(i));
        }
        return sb;
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
    public AuthorizationResponse loginByImpersonator(String clientId, String clientSecret, String refreshToken, String username) {

        String client = StringUtils.hasText(clientId) ? clientId : this.clientId;
        MDC.put("clientId", client);
        MDC.put("usr", username);

        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder().connectionPoolSize(10);
//        UserRepresentation identity = identityService.findIdentityByUsername(username).orElseThrow(() -> new IdentityNotFoundException("username " + username));
        
        AdvancedTokenManager tokenManager = new AdvancedTokenManager(
                new AdvancedTokenConfig(this.adminServerUrl, realm, username, null, client, StringUtils.hasText(clientId) ? clientSecret : null, 
                                OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE), clientBuilder.build(), refreshToken);

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