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
 * Exception User exists - {@link HttpStatus#UNPROCESSABLE_ENTITY} - duplicate code.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 10. 6. 2019 13:56:31
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class PasswordPolicyException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public PasswordPolicyException(String password) {
        super("Entered password is not accepted by Password Policy: " + password);
    }
    
}
