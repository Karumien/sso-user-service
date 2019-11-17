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
 * Exception when no ClientSystem exists - {@link HttpStatus#GONE}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 10. 6. 2019 13:56:31
 */
@ResponseStatus(HttpStatus.GONE)
public class ClientNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public ClientNotFoundException(String code) {
        super("Client not found: " + code);
    }
    
}
