/*
Copyright 2011 Opera Software ASA

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

package com.opera.core.systems.preferences;

import com.opera.core.systems.common.lang.OperaStrings;

/**
 * Shared methods for the {@link OperaPreferences.OperaPreference} interface for keeping the local
 * cache of individual preference up to date.
 *
 * @see ScopePreference, FilePreference
 */
public abstract class AbstractPreference implements OperaPreferences.OperaPreference {

  protected Object defaultValue;

  private String section;
  private String key;
  private Object value;

  public AbstractPreference(String section, String key, Object value) {
    this.section = section;
    this.key = key;
    this.value = sanitizeValue(value);
  }

  public String getSection() {
    return section;
  }

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return getValue(false);
  }

  /**
   * Returns the value of the preference.  If argument raw is true, it will be returned as it is
   * saved in the cache, meaning we won't try to figure out whether the object is /actually/ a
   * boolean or an integer.
   *
   * @param raw set to true to return the raw data in cache
   * @return value of the preference
   */
  public Object getValue(boolean raw) {
    if (!raw) {
      if (OperaStrings.isNumeric(value.toString()) && isTruthy(value)) {
        return value.equals("1");  // boolean, true or false
      } else if (OperaStrings.isNumeric(value.toString())) {
        return Integer.valueOf(value.toString());  // integer
      }
    }

    return value;  // object, whatever it is
  }

  public void setValue(Object value) {
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }

    this.value = sanitizeValue(value);
  }

  public Object getDefaultValue() {
    return defaultValue;
  }
  
  public String toString() {
    return getValue().toString();
  }

  /**
   * If object is a boolean, convert it to "1" or "0".
   *
   * @param value an object
   * @return a sanitized object
   */
  protected Object sanitizeValue(Object value) {
    if (value instanceof Boolean) {
      value = Boolean.parseBoolean(value.toString()) ? "1" : "0";
    }

    return value;
  }

  /**
   * Whether object holds a "truthy" value.  As a string it equals "0", "1", "true" or "false".
   *
   * @param o object to check
   * @return true if value is "truthy", false otherwise
   */
  protected boolean isTruthy(Object o) {
    String s = o.toString();
    return s.equals("0") || s.equals("1") || s.equals("true") || s.equals("false");
  }

}