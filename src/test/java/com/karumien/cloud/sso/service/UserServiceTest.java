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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.karumien.cloud.sso.api.model.UserBaseInfoDTO;
import com.karumien.cloud.sso.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void createAccount() throws Exception {
        
        UserBaseInfoDTO user = new UserBaseInfoDTO();
        user.setFirstName("Ladislav");
        user.setLastName("Stary");
        user.setEmail("stary@seznam.cz");
        
        UserBaseInfoDTO userCreated = userService.createUser(user);
        Assert.assertEquals(user.getFirstName(), userCreated.getFirstName());
        Assert.assertEquals(user.getLastName(), userCreated.getLastName());
        Assert.assertEquals(user.getEmail(), userCreated.getEmail());
        Assert.assertNotNull(userCreated.getId());
        Assert.assertNotNull(userCreated.getUsername());
        Assert.assertEquals(userCreated.getEmail(), userCreated.getUsername());
        
        userService.deleteUser(user.getId());
    }
}
