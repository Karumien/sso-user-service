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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karumien.cloud.sso.api.model.AuthorizationResponseDTO;

/**
 * Implementation {@link AuthService} for authentication tokens management.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 13. 8. 2019 22:07:27
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Value("${keycloak.realm}")
    private String REALM;

    @Value("${keycloak.auth-server-url}")
    private String ADMIN_SERVER_URL;
    
    @Autowired
    private Keycloak keycloak;

    private static PublicKey toPublicKey(String publicKeyString) {
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

        ObjectMapper om = new ObjectMapper();
        Map<String, Object> realmInfo;
        try {
            realmInfo = om.readValue(new URL(ADMIN_SERVER_URL).openStream(), Map.class);
            return (String) realmInfo.get("public_key");
        } catch (Exception e) {
            throw new IllegalStateException("Can't retreive public key");
        }
    }

    @Override
    public void logoutByToken(String token) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AuthorizationResponseDTO loginByUsernamePassword(String username, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AuthorizationResponseDTO loginByClientCredentials(String clientId, String clientSecret) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AuthorizationResponseDTO loginByToken(String refreshToken) {
        // TODO Auto-generated method stub
        return null;
    }

}