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

package com.opera.core.systems.preferences;

/**
 *
 */
public class OperaGenericPreferences extends AbstractOperaPreferences {

  /**
   * Clears all preferences from the local cache.
   */
  public void clear() {
    preferences.clear();
  }

  /**
   * Instantiatable generic preference that does not do any special treatment a preferences like
   * {@link ScopePreference} or {@link FilePreference}.
   *
   * @see ScopePreference, FilePreference
   */
  public static class GenericPreference extends AbstractPreference {

    public GenericPreference(String section, String key, Object value) {
      super(section, key, value);
    }

  }

}