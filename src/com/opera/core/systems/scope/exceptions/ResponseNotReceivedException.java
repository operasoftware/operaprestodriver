/*
Copyright 2008-2011 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.opera.core.systems.scope.exceptions;

import org.openqa.selenium.WebDriverException;

/**
 * Thrown when the response is not received in the given amount of time
 * 
 * @author Deniz Turkoglu <denizt@opera.com>
 * 
 */
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
