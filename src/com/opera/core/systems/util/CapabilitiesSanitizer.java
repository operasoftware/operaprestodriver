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

package com.opera.core.systems.util;

import com.opera.core.systems.OperaDriver;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Sanitizes the values of the capabilities object provided according to the rules defined in the
 * IntegerCapabilities.
 *
 * If connecting to RemoteWebDriver from a language without support for proper integers (such as
 * Ruby and Python), we must convert the longs (produced by the Integer class or #to_i method in
 * Ruby) to integers.
 *
 * WARNING: Since Java's Long can't be casted into Integer, we need to ask java.lang.Long for the
 * integer value of its value.  This will perform a narrowing conversion simply discarding the
 * lowest bits that don't fit into the Integer type.
 *
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class CapabilitiesSanitizer {

  private DesiredCapabilities capabilities;

  private enum IntegerCapabilities {
    PORT(OperaDriver.PORT),
    DISPLAY(OperaDriver.DISPLAY);

    private String value;

    IntegerCapabilities(String value) {
      this.value = value;
    }
  }

  /**
   * The capabilities you wish to sanitize.  Typically you want to check the user's input and not
   * what the driver produces after merging the default capabilities and the ones provided by the
   * user.
   *
   * @param capabilities a Capabilities object
   */
  public CapabilitiesSanitizer(Capabilities capabilities) {
    this.capabilities = (DesiredCapabilities) capabilities;
  }

  /**
   * Performs the sanitization of the capabilities object, returning the same capabilities object
   * with nice values.
   *
   * @return a capabilities object that is safe to use
   */
  public Capabilities sanitize() {
    for (IntegerCapabilities c : IntegerCapabilities.values()) {
      capabilities.setCapability(c.value, valueToInteger(capabilities.getCapability(c.value)));
    }

    return capabilities;
  }

  /**
   * Takes the capability's value, checks whether it is a Long or a String and converts it safely
   * into an Integer.
   *
   * @param value value to convert
   * @return an integer representation of the value
   */
  private Object valueToInteger(Object value) {
    if (value instanceof Long) {
      return ((Long) value).intValue();
    } else if (value instanceof String) {
      return Integer.parseInt((String) value);
    }

    return value;
  }

}