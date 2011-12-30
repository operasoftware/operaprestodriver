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

/**
 * Shared methods for the {@link OperaPreferences.OperaPreference} interface for keeping the local
 * cache of individual preference up to date.
 *
 * @see ScopePreference, FilePreference
 */
public abstract class AbstractOperaPreference implements OperaPreferences.OperaPreference {

  protected Object defaultValue;

  private String section;
  private String key;
  private Object value;

  public AbstractOperaPreference(String section, String key, Object value) {
    this.section = section;
    this.key = key;
    //setValue(value);
    this.value = value;
  }

  public String getSection() {
    return section;
  }

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }

    if (value instanceof Boolean) {
      value = Boolean.parseBoolean(value.toString()) ? "1" : "0";
    }

    this.value = value;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }
  
  public String toString() {
    return getValue().toString();
  }

}