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
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;
    
    private static String accountNumber;
    
    @BeforeClass
    public static void beforeClass() {
        accountNumber = "999" + new Random().nextInt(100);
    }
    
    @Test
    public void crudAccount() {
        
        AccountInfo account = new AccountInfo();
        account.setAccountNumber(accountNumber);
        account.setName("TEST_COMPANY_" + accountNumber);
        account.setCompRegNo("60255523");
        account.setContactEmail("info@firma.cz");
        
        AccountInfo accountCreated = accountService.createAccount(account);
        Assert.assertEquals(accountCreated.getAccountNumber(), accountNumber);
        Assert.assertEquals(accountCreated.getName(), "TEST_COMPANY_" + accountNumber);
        Assert.assertEquals(accountCreated.getCompRegNo(), "60255523");
        Assert.assertEquals(accountCreated.getContactEmail(), "info@firma.cz");

//        AccountInfo AccountUpdated = AccountService.udateAccount(Account);
//        Assert.assertEquals(Account.getAccountId(), "TEST" + id);
//        Assert.assertEquals(Account.getDescription(), "TEST-Account");
                
        List<AccountInfo> accounts = accountService.getAccounts();
        Assert.assertNotNull(accounts);
        Assert.assertFalse(accounts.isEmpty());
        
        AccountInfo accountFound = accountService.getAccount(accountNumber);
        Assert.assertEquals(accountFound.getAccountNumber(), accountNumber);
        Assert.assertEquals(accountFound.getName(), "TEST_COMPANY_" + accountNumber);
        Assert.assertEquals(accountFound.getCompRegNo(), "60255523");
        Assert.assertEquals(accountFound.getContactEmail(), "info@firma.cz");

        accountService.deleteAccount(accountNumber);
    }
    
    @Test(expected = AccountNotFoundException.class)
    public void unexistedAccountForFind() {
        accountService.getAccount("QWESS342343ADSDAS");
    }    

    @Test(expected = AccountNotFoundException.class)
    public void unexistedAccountForDelete() {
        accountService.deleteAccount("QWESS342343ADSDAS");
    }    
    
    
    

}
