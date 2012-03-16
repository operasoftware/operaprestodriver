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

package com.opera.core.systems.preferences;

import com.opera.core.systems.common.lang.OperaStrings;
import com.opera.core.systems.model.OperaColor;

import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Shared functionality between various {@link OperaPreferences} implementations.
 *
 * @see OperaScopePreferences, OperaFilePreferences
 */
public abstract class AbstractOperaPreferences implements OperaPreferences {

  protected Collection<OperaPreference> preferences = new LinkedHashSet<OperaPreference>();

  public Iterator<OperaPreference> iterator() {
    return preferences.iterator();
  }

  public Iterable<OperaPreference> all() {
    return preferences;
  }

  public void set(OperaPreference preference) {
    // Does this preference already exist in our collection?
    for (OperaPreference p : this) {
      if (p.getSection().equalsIgnoreCase(preference.getSection()) &&
          p.getKey().equalsIgnoreCase(preference.getKey())) {
        // Pref exists, replace with given pref
        p.setValue(preference.getValue());
        return;
      }
    }

    // Given pref does not exist, add it to our collection
    preferences.add(preference);
  }

  public void set(String section, String key, Object value) {
    set(new OperaGenericPreferences.GenericPreference(section, key, value));
  }

  public OperaPreference get(String section, String key) {
    for (OperaPreference p : this) {
      if (p.getSection().equalsIgnoreCase(section) &&
          p.getKey().equalsIgnoreCase(key)) {
        return p;
      }
    }

    return null;
  }

  public void merge(OperaPreferences newPreferences) {
    for (OperaPreference preference : newPreferences) {
      set(preference);
    }
  }

  public int size() {
    return preferences.size();
  }

  /**
   * Shared methods for the {@link OperaPreferences.OperaPreference} interface for keeping the local
   * cache of individual preference up to date.
   *
   * @see OperaScopePreferences.ScopePreference, OperaFilePreferences.FilePreference
   */
  public static abstract class AbstractPreference implements OperaPreferences.OperaPreference {

    protected Object defaultValue;

    private final String section;
    private final String key;
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
      return this.getValue(false);
    }

    /**
     * Gets the preference's value.  Special cases are made for preferences of the types {@link
     * java.io.File} and {@link OperaColor}, where strings representing their values will be
     * returned instead of their actual object type.
     *
     * @param operaCompatible whether the returned value should be compatible for sending to Opera
     * @return the value of this preference
     */
    public Object getValue(boolean operaCompatible) {
      if (operaCompatible) {
        if (value instanceof OperaColor) {
          return ((OperaColor) value).getHex();
        } else if (value instanceof File) {
          return ((File) value).getPath();
        } else if (value instanceof Boolean) {
          return ((Boolean) value) ? "1" : "0";
        }

        return value.toString();
      }

      String stringValue = value.toString();

      if (OperaStrings.isNumeric(stringValue) && isTruthy(value)) {
        return value.equals("1");  // boolean
      } else if (OperaStrings.isInteger(stringValue)) {
        return Integer.valueOf(stringValue);  // integer
      } else if (OperaStrings.isDouble(stringValue)) {
        return Double.parseDouble(stringValue);  // double
      }

      return stringValue;
    }

    public void setValue(Object value) {
      checkNotNull(value);
      this.value = sanitizeValue(value);
    }

    public Object getDefaultValue() {
      return defaultValue;
    }

    public String toString() {
      return "Preference [section: " + getSection() +
             ", key: " + getKey() +
             ", value: " + getValue().toString() + "]";
    }

    /**
     * If object is a boolean, convert it to "1" or "0" since Java is strongly typed and because
     * we're sending data as strings.
     *
     * @param value an object
     * @return a sanitized object
     */
    private Object sanitizeValue(Object value) {
      if (value instanceof Boolean) {
        value = Boolean.parseBoolean(value.toString()) ? "1" : "0";
      }

      return value;
    }

    /**
     * Whether object holds a "truthy" value.  As  a string it equals "0", "1", "true" or "false".
     * This method says nothing about whether the object is true or false.
     *
     * @param o object to check
     * @return true if value is "truthy", false otherwise
     */
    private boolean isTruthy(Object o) {
      String s = o.toString();
      return s.equals("0") || s.equals("1") || s.equals("true") || s.equals("false");
    }

  }

}