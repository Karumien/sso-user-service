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
 * Exception when Identity doesn't have email - {@link HttpStatus#}.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 10. 6. 2019 13:56:31
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class IdentityEmailNotExistsOrVerifiedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public IdentityEmailNotExistsOrVerifiedException(String code) {
        super("Identity email not set or verified: " + code);
    }
    
}
