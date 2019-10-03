/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.karumien.cloud.sso.api.model.Policy;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PasswordGeneratorServiceTest {

    @Autowired
    private PasswordGeneratorService passwordGeneratorService;

    @Test
    public void testMinLength() {
        
        Policy policy = new Policy();
        assertEquals(passwordGeneratorService.generate(policy).length(), 6);        
        
        policy.setMinLength(16);
        assertEquals(passwordGeneratorService.generate(policy).length(), 16);        
    }

    @Test
    public void testFullPolicy() {
        
        Policy policy = new Policy();
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
