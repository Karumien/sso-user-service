/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.karumien.cloud.sso.api.model.PasswordPolicy;

@SpringBootTest
class PasswordGeneratorServiceTest {

    @Autowired
    private PasswordGeneratorService passwordGeneratorService;

    @Test
    void testMinLength() {
        
        PasswordPolicy policy = new PasswordPolicy();
        assertEquals(passwordGeneratorService.generate(policy).length(), 6);        
        
        policy.setMinLength(16);
        assertEquals(passwordGeneratorService.generate(policy).length(), 16);        
    }

    @Test
    void testFullPolicy() {
        
        PasswordPolicy policy = new PasswordPolicy();
        policy.setMinLength(12);
        policy.setMinDigits(3);
        policy.setMinSpecialChars(3);
        policy.setMinLowerCase(3);
        policy.setMinUpperCase(3);

        assertEquals(passwordGeneratorService.generate(policy).length(), 12);        
        assertEquals(passwordGeneratorService.generate(policy).length(), 12);        
        assertEquals(passwordGeneratorService.generate(policy).length(), 12);        
        assertEquals(passwordGeneratorService.generate(policy).length(), 12);        

        policy.setMinLength(20);
        assertEquals(passwordGeneratorService.generate(policy).length(), 20);        
        assertEquals(passwordGeneratorService.generate(policy).length(), 20);        
        assertEquals(passwordGeneratorService.generate(policy).length(), 20);        
    }
}
