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

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.karumien.cloud.sso.api.model.IdentityInfo;
import com.karumien.cloud.sso.api.model.RoleInfo;
import com.karumien.cloud.sso.exceptions.IdentityNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class IdentityServiceTest {

    @Autowired
    private IdentityService identityService;

    @Test
    public void createAccount() throws Exception {
        
        IdentityInfo identity = new IdentityInfo();
        identity.setContactNumber("CRM00001");
        identity.setFirstName("Ladislav");
        identity.setLastName("Stary");
        identity.setEmail("stary@seznam.cz");
        
        IdentityInfo userCreated = identityService.createIdentity(identity);
        Assert.assertEquals(identity.getFirstName(), userCreated.getFirstName());
        Assert.assertEquals(identity.getLastName(), userCreated.getLastName());
        Assert.assertEquals(identity.getEmail(), userCreated.getEmail());
        Assert.assertNotNull(userCreated.getContactNumber());
        Assert.assertNotNull(userCreated.getUsername());
        Assert.assertEquals(userCreated.getEmail(), userCreated.getUsername());
        
        identityService.deleteIdentity(identity.getContactNumber());
    }
      
    @Test
    public void getAllIdentityRoles() throws Exception {
    	identityService.deleteIdentity("CRM00001");
    	IdentityInfo identity = null;
		try {
			identity = identityService.getIdentity("CRM00001");
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
        List<RoleInfo> roles = identityService.getAllIdentityRoles(identity.getContactNumber());
        Assert.assertNotNull(roles);       
        identityService.deleteIdentity(identity.getContactNumber());
    }
}
