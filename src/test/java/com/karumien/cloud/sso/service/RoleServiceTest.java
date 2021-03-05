/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

/**
 * Tests Service {@link RoleService}.
 * 
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 29. 8. 2019 15:17:10
 */
@Slf4j
@SpringBootTest
@Disabled
class RoleServiceTest {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Autowired
    private Keycloak keycloak;

    @Test
    void roles() throws Exception {
        keycloak.realm(realm).roles().list().forEach(r -> log.debug("realm-role: " + r.getName()));
        keycloak.realm(realm).clients().findAll()
                .forEach(r -> log.debug("clientId: " + r.getClientId() + ", clientName: " + r.getName() + ", id: " + r.getId()));
        ClientResource client = keycloak.realm(realm).clients().get("6fd0f65b-de70-4a4c-a7eb-ca2dce1ae42c");
        log.debug("" + client.getSecret());
        log.debug("" + client.roles().list());
        assertNotNull(client.getSecret());
    }

}
