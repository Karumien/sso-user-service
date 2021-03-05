/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.karumien.cloud.sso.api.model.IdentityPropertyType;

/**
 * Tests Service {@link SearchService}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 4. 10. 2019 11:35:53 
 */
@SpringBootTest
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Test
    @Disabled
    void findUserIdsByAttribute() {
        
        List<String> usersIds = searchService.findUserIdsByAttribute(IdentityPropertyType.ATTR_CONTACT_NUMBER, "11714");
        assertNotNull(usersIds);
        assertEquals(2, usersIds.size());
        
    }
    
}
