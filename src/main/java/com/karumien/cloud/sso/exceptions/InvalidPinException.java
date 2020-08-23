/**
 * 
 */
package com.karumien.cloud.sso.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidPinException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public InvalidPinException() {
	}

	/**
	 * @param message
	 */
	public InvalidPinException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidPinException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidPinException(String message, Throwable cause) {
		super(message, cause);
	}

}
