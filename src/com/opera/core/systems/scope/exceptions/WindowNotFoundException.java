package com.opera.core.systems.scope.exceptions;

import org.openqa.selenium.WebDriverException;

public class WindowNotFoundException extends WebDriverException {
	
	private static final long serialVersionUID = 1L;

	public WindowNotFoundException() {
		super();
	}
	
	public WindowNotFoundException(String message) {
		super(message);
	}

	public WindowNotFoundException(Throwable cause) {
		super(cause);
	}

	public WindowNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
