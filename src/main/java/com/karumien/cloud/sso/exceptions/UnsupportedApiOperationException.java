/*
 * Copyright (c) 2019-2029 Karumien s.r.o.
 *
 * Karumien s.r.o. is not responsible for defects arising from 
 * unauthorized changes to the source code.
 */
package com.karumien.cloud.sso.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Unsupported Operation Exception - NOT_IMPLEMENTED.
 *
 * @author <a href="miroslav.svoboda@karumien.com">Miroslav Svoboda</a>
 * @since 1.0, 21. 8. 2019 14:40:10 
 */
@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class UnsupportedApiOperationException extends UnsupportedOperationException {

    private static final long serialVersionUID = 1L;

    public UnsupportedApiOperationException() {
        super("Not implemented now.");
    }

    public UnsupportedApiOperationException(String message) {
        super(message);
    }
}
