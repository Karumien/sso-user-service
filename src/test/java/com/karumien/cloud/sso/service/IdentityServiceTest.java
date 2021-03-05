/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.karumien.cloud.sso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;

@SpringBootTest
@Disabled
class IdentityServiceTest {

    @Autowired
    private IdentityService identityService;

    @Test
    void createAccount() throws Exception {
        
        IdentityInfo identity = new IdentityInfo();
        identity.setContactNumber("CRM00001");
        identity.setFirstName("Ladislav");
        identity.setLastName("Stary");
        identity.setEmail("stary@seznam.cz");
        
        IdentityInfo userCreated = identityService.createIdentity(identity);
        assertEquals(identity.getFirstName(), userCreated.getFirstName());
        assertEquals(identity.getLastName(), userCreated.getLastName());
        assertEquals(identity.getEmail(), userCreated.getEmail());
        assertNotNull(userCreated.getContactNumber());
        assertNotNull(userCreated.getUsername());
        assertEquals(userCreated.getEmail(), userCreated.getUsername());
        
        identityService.deleteIdentity(identity.getContactNumber());
    }
      
    @Test
    void getAllIdentityRoles() throws Exception {
    	identityService.deleteIdentity("CRM00001");
    	IdentityInfo identity = null;
		try {
			identity = identityService.getIdentity("CRM00001", false);
		} catch (IdentityNotFoundException e) {
			if (identity == null) {
				identity = new IdentityInfo();
				identity.setContactNumber("CRM00001");
				identity.setFirstName("Ladislav");
				identity.setLastName("Stary");
				identity.setEmail("stary@seznam.cz");
			}
        }
		
        //IdentityInfo userCreated = 
        identityService.createIdentity(identity);
        identityService.deleteIdentity(identity.getContactNumber());
        assertNotNull(identity);
    }
}
