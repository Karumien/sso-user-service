/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * KeyCloak server instance konfiguration.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 18. 7. 2019 11:22:26
 */
@Configuration
public class KeyCloakConfiguration {

    @Value("${keycloak.auth-server-url}")
    private String ADMIN_SERVER_URL;

    @Value("${keycloak.username}")
    private String USERNAME;

    @Value("${keycloak.password}")
    private String PASSWORD;

    @Value("${keycloak.client-id}")
    private String CLIENT_ID;

    @Bean
    public Keycloak getKeyCloak() {
        return KeycloakBuilder.builder().serverUrl(ADMIN_SERVER_URL).realm("master")
                .username(USERNAME).password(PASSWORD).clientId(CLIENT_ID).build();
    }
}
