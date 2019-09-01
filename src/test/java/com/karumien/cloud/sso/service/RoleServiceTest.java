/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 29. 8. 2019 15:17:10 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleServiceTest {

    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.client-id}")
    private String clientId;

    @Autowired
    private Keycloak keycloak;
    
    @Test
    public void roles() throws Exception {
        keycloak.realm(realm)
            .roles().list().forEach(r -> System.out.println("realm-role: " + r.getName()));
        keycloak.realm(realm).clients().findAll()
            .forEach(r -> System.out.println("clientId: " + r.getClientId() + ", clientName: " + r.getName() + ", id: " + r.getId()));
        ClientResource client = keycloak.realm(realm).clients().get("6fd0f65b-de70-4a4c-a7eb-ca2dce1ae42c");
        System.out.println(client.getSecret());
        System.out.println(client.roles().list());
        
        
        //    .roles().list().forEach(r -> System.out.println("client-role: " + r.getName()));
    }

}
