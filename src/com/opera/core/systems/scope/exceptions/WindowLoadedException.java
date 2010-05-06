package com.opera.core.systems.scope.exceptions;

import org.openqa.selenium.WebDriverException;

public class WindowLoadedException extends WebDriverException{

	private static final long serialVersionUID = 1L;
	
	public WindowLoadedException() {
		super();
	}
	
	public WindowLoadedException(String message) {
		super(message);
	}

	public WindowLoadedException(Throwable cause) {
		super(cause);
	}

	public WindowLoadedException(String message, Throwable cause) {
		super(message, cause);
	}
}
