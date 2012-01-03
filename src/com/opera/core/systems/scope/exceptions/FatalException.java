/*
Copyright 2008-2012 Opera Software ASA

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
 * Exception that is used when failing to start opera or the STP connection during early phases of
 * initializations.
 *
 * @author Jan Vidar Krey <janv@opera.com>
 */
public class FatalException extends WebDriverException {

  private static final long serialVersionUID = 3834561306215527454L;

  /**
   * Creates a new instance of <code>FatalException</code> without detail message.
   */
  public FatalException() {
  }

  /**
   * Constructs an instance of <code>FatalException</code> with the specified detail message.
   *
   * @param msg the detail message.
   */
  public FatalException(String msg) {
    super(msg);
  }

}