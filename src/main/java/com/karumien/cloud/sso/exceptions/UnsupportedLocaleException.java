package com.karumien.cloud.sso.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when given locale is not supported {@link HttpStatus#UNPROCESSABLE_ENTITY}.
 *
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnsupportedLocaleException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public UnsupportedLocaleException(String locale) {
        super("Specified locale " + locale + " is unsupported.");
    }
    
}