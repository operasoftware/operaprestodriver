/*
Copyright 2011-2012 Opera Software ASA

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

package com.opera.core.systems.arguments;

/**
 * OperaArgument represents a single argument in a command-line argument list, typically represented
 * by {@link com.opera.core.systems.arguments.interfaces.OperaArguments}.
 */
public class OperaArgument {

  private String argument;
  private String value;

  /**
   * Holds definitions for prepended signs for arguments in various styles.  Available signs are
   * {@link #GNU_SIGN}, {@link #POSIX_SIGN} and {@link #WINDOWS_SIGN}.
   *
   * @see com.opera.core.systems.arguments.interfaces.OperaArguments
   */
  public enum OperaArgumentSign {

    /**
     * The GNU sign consists of two dashes, <var>--</var>.
     */
    GNU_SIGN("--"),

    /**
     * The POSIX sign consists of one dash, <var>-</var>.
     */
    POSIX_SIGN("-"),

    /**
     * The WINDOWS sign consists of a slash, <var>/</var>.
     */
    WINDOWS_SIGN("/");

    private final String sign;

    private OperaArgumentSign(String sign) {
      this.sign = sign;
    }

    /**
     * Gets the string representation value of the specified sign.
     *
     * @return the sign
     */
    public String getValue() {
      return sign;
    }

  }

  /**
   * Creates a new argument with no value.  The attribute <code>argument</code> should not contain
   * any argument signs (dashes) as this will be prepended later depending on which implementation
   * of {@link com.opera.core.systems.arguments.interfaces.OperaArguments} you use.
   *
   * @param argument the argument to create, without any prepended signs
   */
  public OperaArgument(String argument) {
    this(argument, null);
  }

  /**
   * Creates a new argument with a value.  The attribute <code>attribute</code> should not contain
   * any argument signs (dashes) as this will be prepended later depending on which implementation
   * of {@link com.opera.core.systems.arguments.interfaces.OperaArguments} you use.
   *
   * @param argument the argument to create, without any prepended signs
   * @param value    the value of the argument
   */
  public OperaArgument(String argument, Object value) {
    this.argument = OperaArgument.sanitize(argument);
    if (value != null) {
      this.value = String.valueOf(value);
    }
  }

  /**
   * Gets the string representation of the argument without any prepended signs.
   *
   * @return the argument
   */
  public String getArgument() {
    return argument;
  }

  /**
   * Gets the value of the argument if it exists.
   *
   * @return value of argument, null if there is no value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sanitizes an argument that contains a sign.  By default we sanitize all added arguments,
   * meaning "-foo" will be sanitized to "foo".
   *
   * @param key the argument key to sanitize
   * @return a sanitized argument key
   */
  private static String sanitize(String key) {
    for (OperaArgumentSign sign : OperaArgumentSign.values()) {
      if (hasSwitch(key, sign.sign)) {
        return key.substring(sign.sign.length());
      }
    }

    return key;
  }

  /**
   * Determines whether given argument key contains given argument sign.
   *
   * @param key  the argument key to check
   * @param sign the sign to check for
   * @return true if key contains sign as first characters, false otherwise
   * @see OperaArgumentSign
   */
  private static Boolean hasSwitch(String key, String sign) {
    return (key.length() > sign.length()) && key.substring(0, sign.length()).equals(sign);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((argument == null) ? 0 : argument.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || (getClass() != obj.getClass())) {
      return false;
    }
    OperaArgument other = (OperaArgument) obj;
    if (argument == null) {
      if (other.argument != null) {
        return false;
      }
    } else if (!argument.equals(other.argument)) {
      return false;
    }
    return true;
  }

}