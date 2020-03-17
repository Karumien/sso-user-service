/*
 * Copyright (c) 2019 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when can't delete Account because contains identities - {@link HttpStatus#UNPROCESSABLE_ENTITY}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 10. 6. 2019 13:56:31
 */
@ResponseStatus(HttpStatus.GONE)
public class AccountDeleteException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public AccountDeleteException(String accountNumber) {
        super("Can't delete account: " + accountNumber + ", because contains identities");
    }
    
}
