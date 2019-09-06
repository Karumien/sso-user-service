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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.api.model.ModuleInfo;
import com.karumien.cloud.sso.exceptions.ModuleNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class ModuleServiceTest {

    @Autowired
    private ModuleService moduleService;
    
    @Autowired
    private AccountService accountService;
    
    private static String moduleId;
    private static String crmAccountId = "99931565";
    
    @BeforeClass
    public static void beforeClass() {
        moduleId = "TEST_" + new Random().nextInt(100);
    }
    
    @Test
    public void crudModule() {
        
        
        ModuleInfo module = new ModuleInfo();
        module.setModuleId(moduleId);
        module.setDescription("TEST-MODULE");
        
        ModuleInfo moduleCreated = moduleService.createModule(module);
        Assert.assertEquals(moduleCreated.getModuleId(), moduleId);
        Assert.assertEquals(moduleCreated.getDescription(), "TEST-MODULE");

//        ModuleInfo moduleUpdated = moduleService.udateModule(module);
//        Assert.assertEquals(module.getModuleId(), "TEST" + id);
//        Assert.assertEquals(module.getDescription(), "TEST-MODULE");
                
        ModuleInfo moduleFound = moduleService.getModule(moduleId);
        Assert.assertEquals(moduleFound.getModuleId(), moduleId);
        Assert.assertEquals(moduleFound.getDescription(), "TEST-MODULE");

        moduleService.deleteModule(moduleId);
    }
    
    @Test(expected = ModuleNotFoundException.class)
    public void unexistedModuleForFind() {
        moduleService.getModule("QWESS342343ADSDAS");
    }    

    @Test(expected = ModuleNotFoundException.class)
    public void unexistedModuleForDelete() {
        moduleService.deleteModule("QWESS342343ADSDAS");
    }    
    
    @Test
    public void assignModulesToAccount() {
        
        String crmAccountId2 = crmAccountId + "1";
        String moduleId2 = moduleId + "B";
        
        ModuleInfo module = new ModuleInfo();
        module.setModuleId(moduleId);
        module.setDescription("TEST-MODULE");
        
        ModuleInfo module1 = new ModuleInfo();
        module1.setModuleId(moduleId + "B");
        module1.setDescription("TEST-MODULE-B");

        AccountInfo account = new AccountInfo();
        account.setCrmAccountId(crmAccountId);
        account.setName("COMPANY" + moduleId);
        account.setCompRegNo("60255523");
        account.setContactEmail("info@firma.cz");
        
        AccountInfo account1 = new AccountInfo();
        account1.setCrmAccountId(crmAccountId + "1");
        account1.setName("COMPANY" + moduleId + "B");
        account1.setCompRegNo("70255523");
        account1.setContactEmail("info@firma2.cz");

        moduleService.createModule(module);
        moduleService.createModule(module1);
        
        accountService.createAccount(account);
        accountService.createAccount(account1);
        
        moduleService.activateModules(Arrays.asList(moduleId, moduleId2), Arrays.asList(crmAccountId, crmAccountId2));

        List<ModuleInfo> modules = moduleService.getAccountModules(crmAccountId);
        Assert.assertEquals(modules.size(), 2);
        Assert.assertTrue(moduleService.isActiveModule(moduleId, crmAccountId));
        Assert.assertTrue(moduleService.isActiveModule(moduleId, crmAccountId2));
        Assert.assertTrue(moduleService.isActiveModule(moduleId2, crmAccountId));
        Assert.assertTrue(moduleService.isActiveModule(moduleId2, crmAccountId2));
        
        moduleService.deactivateModules(Arrays.asList(moduleId), Arrays.asList(crmAccountId));
        modules = moduleService.getAccountModules(crmAccountId);
        Assert.assertEquals(modules.size(), 1);
        Assert.assertFalse(moduleService.isActiveModule(moduleId, crmAccountId));
        Assert.assertTrue(moduleService.isActiveModule(moduleId, crmAccountId2));
        Assert.assertTrue(moduleService.isActiveModule(moduleId2, crmAccountId));
        Assert.assertTrue(moduleService.isActiveModule(moduleId2, crmAccountId2));
        
        moduleService.deactivateModules(Arrays.asList(moduleId, moduleId2), Arrays.asList(crmAccountId, crmAccountId2));
        Assert.assertEquals(moduleService.getAccountModules(crmAccountId2).size(), 0);
        Assert.assertEquals(moduleService.getAccountModules(crmAccountId).size(), 0);
        Assert.assertFalse(moduleService.isActiveModule(moduleId, crmAccountId));
        Assert.assertFalse(moduleService.isActiveModule(moduleId, crmAccountId2));
        Assert.assertFalse(moduleService.isActiveModule(moduleId2, crmAccountId));
        Assert.assertFalse(moduleService.isActiveModule(moduleId2, crmAccountId2));
        
        accountService.deleteAccount(crmAccountId);
        accountService.deleteAccount(crmAccountId2);
                
        moduleService.deleteModule(moduleId);
        moduleService.deleteModule(moduleId + "B");
    
    }

}
