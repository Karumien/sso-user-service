/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import java.net.URI;
import java.util.Optional;

import javax.validation.Valid;
import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.karumien.cloud.sso.api.model.UserBaseInfoDTO;
import com.karumien.cloud.sso.exceptions.UserDuplicateException;

/**
 * Implementation {@link UserService} for managing {@link PerformanceData} entity.
 *
 * @author <a href="viliam.litavec@karumien.com">Viliam Litavec</a>
 * @since 1.0, 10. 6. 2019 22:07:27
 */
@Service
public class UserServiceImpl implements UserService {

    @Value("${keycloak.realm}")
    private String REALM;

    @Autowired
    private Keycloak keycloak;

    @Override
    public void deleteUser(String id) {
        keycloak.realm(REALM).users().delete(id);
    }

    @Override
    public UserBaseInfoDTO createUser(@Valid UserBaseInfoDTO userBaseInfo) {

        // CredentialRepresentation credential = new CredentialRepresentation();
        // credential.setType(CredentialRepresentation.PASSWORD);
        // credential.setValue(password);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(Optional.ofNullable(userBaseInfo.getUsername()).orElse(userBaseInfo.getEmail()));
        user.setFirstName(userBaseInfo.getFirstName());
        user.setLastName(userBaseInfo.getLastName());
        user.setId(userBaseInfo.getId());
        user.setEmail(userBaseInfo.getEmail());
        // user.setEmailVerified(emailVerified);
        user.setEnabled(true);

        // user.singleAttribute("customAttribute", "customAttribute");
        // user.setCredentials(Arrays.asList(credential));

        Response response = keycloak.realm(REALM).users().create(user);
        userBaseInfo.setId(getCreatedId(response));

        // UserRepresentation createdUser = keycloak.realm(REALM).users().get(userBaseInfo.getId());
        userBaseInfo.setUsername(user.getUsername());

        // Reset password
        // CredentialRepresentation newCredential = new CredentialRepresentation();
        // UserResource userResource = getInstance().realm(REALM).users().get(createdId);
        // newCredential.setType(CredentialRepresentation.PASSWORD);
        // newCredential.setValue(password);
        // newCredential.setTemporary(false);
        // userResource.resetPassword(newCredential);
        // return HttpStatus.CREATED.value();

        return userBaseInfo;
    }

    public String getCreatedId(Response response) {
        URI location = response.getLocation();

        switch (response.getStatusInfo().toEnum()) {
        case CREATED:
            if (location == null) {
                return null;
            }
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        case CONFLICT:
            throw new UserDuplicateException("User exists");
        default:
            throw new UnsupportedOperationException("Unknown status " + response.getStatusInfo().toEnum());
        }
    }


}