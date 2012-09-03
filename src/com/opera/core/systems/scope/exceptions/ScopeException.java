package com.opera.core.systems.scope.exceptions;

import org.openqa.selenium.WebDriverException;

public class ScopeException extends WebDriverException {

  public ScopeException(Exception e) {
    super(e);
  }

  public ScopeException(String message) {
    super(message);
  }

  public ScopeException(String message, Exception exception) {
    super(message, exception);
  }

}