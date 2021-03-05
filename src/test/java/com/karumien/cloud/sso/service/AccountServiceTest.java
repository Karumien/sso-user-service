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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import com.karumien.cloud.sso.api.model.AccountInfo;
import com.karumien.cloud.sso.exceptions.AccountNotFoundException;
import com.karumien.cloud.sso.util.PageableUtils;

@SpringBootTest
@Disabled
class AccountServiceTest {

    @Autowired
    private AccountService accountService;
    
    private static String accountNumber;
    
    @BeforeTestClass
    public static void beforeClass() {
        accountNumber = "999" + new Random().nextInt(100);
    }
    
    @Test
    void crudAccount() {
        
        AccountInfo account = new AccountInfo();
        account.setAccountNumber(accountNumber);
        account.setName("TEST_COMPANY_" + accountNumber);
        account.setCompRegNo("60255523");
        account.setContactEmail("info@firma.cz");
        
        AccountInfo accountCreated = accountService.createAccount(account);
        assertEquals(accountCreated.getAccountNumber(), accountNumber);
        assertEquals(accountCreated.getName(), "TEST_COMPANY_" + accountNumber);
        assertEquals(accountCreated.getCompRegNo(), "60255523");
        assertEquals(accountCreated.getContactEmail(), "info@firma.cz");

//        AccountInfo AccountUpdated = AccountService.udateAccount(Account);
//        assertEquals(Account.getAccountId(), "TEST" + id);
//        assertEquals(Account.getDescription(), "TEST-Account");
                
        List<AccountInfo> accounts = accountService.getAccounts(null, PageableUtils.getRequest(0, 100, Arrays.asList("name,ASC"), Arrays.asList("name")));
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
        
        AccountInfo accountFound = accountService.getAccount(accountNumber);
        assertEquals(accountFound.getAccountNumber(), accountNumber);
        assertEquals(accountFound.getName(), "TEST_COMPANY_" + accountNumber);
        assertEquals(accountFound.getCompRegNo(), "60255523");
        assertEquals(accountFound.getContactEmail(), "info@firma.cz");

        accountService.deleteAccount(accountNumber);
    }
    
    @Test
    void unexistedAccountForFind() {
    	Exception exception = assertThrows(AccountNotFoundException.class, () -> {    	
    		accountService.getAccount("QWESS342343ADSDAS");
    	});
    	assertNotNull(exception);
    }    
    
}
