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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

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

}