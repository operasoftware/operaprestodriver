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
 * Allows setting Opera preferences in a preference file (typically <code>opera.ini</code> or
 * <code>operaprefs.ini</code>) as well as keeping the local individual preference cache
 * up-to-date.
 */
public class FilePreference extends AbstractPreference {

  private OperaFilePreferences parent;

  public FilePreference(OperaFilePreferences parent, String section, String key, Object value) {
    super(section, key, value);
    this.parent = parent;
    parent.write();
  }

  /**
   * Sets the value of this preference to the given value.  Writes the preference to file
   * immediately after updating the local cache.
   *
   * @param value the new value
   */
  public void setValue(Object value) {
    super.setValue(value);
    parent.write();
  }
  
  public static FilePreference convert(OperaFilePreferences parent, OperaPreferences.OperaPreference convertee) {
    return new FilePreference(parent,
                              convertee.getSection(),
                              convertee.getKey(),
                              convertee.getValue());
  }

}