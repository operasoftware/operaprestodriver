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

/**
 * Interface for reading and interacting with preferences in the Opera web browser.
 */
public interface OperaPreferences extends Iterable<OperaPreferences.OperaPreference> {

  /**
   * Returns an iterable collection of all preferences present in the current object.
   *
   * @return iterable list of preferences
   */
  Iterable<OperaPreference> all();

  /**
   * Merges one representation with the given representation.  This will overwrite the preferences
   * in the current object with the ones in the given object to be merged.
   *
   * @param newPreferences the preferences to merge
   */
  void merge(OperaPreferences newPreferences);

  /**
   * Sets the given preference.  If a previous preference with the same section and key names exist,
   * it will be replaced by the given preference.
   *
   * @param preference the preference to add or update
   */
  void set(OperaPreference preference);

  /**
   * Sets the given preference information.  If a previous preference with the same section and key
   * names exist, it will be replaced by the given preference.
   *
   * This is a shortcut for the {@link OperaPreferences#set(OperaPreference)} method.
   *
   * @param section the section name, can be case-insensitive
   * @param key     the key name, can be case-insensitive
   * @param value   the new value (will be treated as a {@link String} in Opera
   */
  void set(String section, String key, Object value);

  /**
   * Returns the preference with the given section and key values.
   *
   * @param section the section name, can be case-insensitive
   * @param key     the key name, can be case-insensitive
   * @return the preference requested
   */
  OperaPreference get(String section, String key);

  /**
   * Returns the number of preferences in the representation.
   *
   * @return number of preferences
   */
  int size();

  /**
   * Interface representing individual preferences.
   */
  interface OperaPreference {

    /**
     * Gets the section this preference belongs to.
     *
     * @return the parent section of this preference
     */
    String getSection();

    /**
     * Gets the key of this preference.
     *
     * @return the key of this preference
     */
    String getKey();

    /**
     * Gets the value of this preference.
     *
     * @return the value of this preference
     */
    Object getValue();

    /**
     * Sets the value of this preference.
     *
     * @param value the new value
     */
    void setValue(Object value);

    /**
     * Gets the default value of the preference.
     *
     * @return the default value of this preference
     */
    Object getDefaultValue();

    /**
     * Gets the value of this preference.
     *
     * @return the value of this preference
     */
    String toString();

  }

}