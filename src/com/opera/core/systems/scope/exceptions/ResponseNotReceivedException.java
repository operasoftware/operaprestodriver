package com.opera.core.systems.scope.exceptions;

import org.openqa.selenium.WebDriverException;

public class ResponseNotReceivedException extends WebDriverException {

	private static final long serialVersionUID = 1L;

	public ResponseNotReceivedException() {
		super();
	}

	public ResponseNotReceivedException(String message) {
		super(message);
	}

	public ResponseNotReceivedException(Throwable cause) {
		super(cause);
	}

	public ResponseNotReceivedException(String message, Throwable cause) {
		super(message, cause);
	}
}
