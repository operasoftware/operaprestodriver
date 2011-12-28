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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class AbstractOperaPreferences implements OperaPreferences {
  
  protected Map<String, List<OperaPreference>> preferences;
  
  public void clear() {
    preferences.clear();
  }
  
  public boolean containsKey(String key) {
    return preferences.containsKey(key);
  }
  
  public boolean containsValue(Object value) {
    return preferences.containsValue(value);
  }
  
  public Set<Map.Entry<String, Object>> entrySet() {
    List<Map<String, Object>> set = new LinkedList<Map<String, Object>>();

    for (Map.Entry<String, List<OperaPreference>> section : preferences.entrySet()) {
      String sectionName = section.getKey();
      List<OperaPreference> preferences = section.getValue();
      
      for (OperaPreference preference : preferences) {
        HashMap<String, Object> pref = new HashMap<String, Object>();
        pref.put(preference.getKey(), preference.getValue());
        set.add(sectionName, pref);
      }
    }
  }
  
  public void set(OperaPreference preference) {
    // Does this preference already exist in our collection?
    for (List<OperaPreference> prefList : preferences.values()) {
      for (OperaPreference p : prefList) {
        if (p.getSection().equalsIgnoreCase(preference.getSection()) &&
            p.getKey().equalsIgnoreCase(preference.getKey())) {
          // Pref exists, replace with given pref
          p.setValue(preference.getValue());
          return;
        }
      }
    }
  }
  
  public void set(String section, String key, Object value) {
    set(new OperaPreference(section, key, value));
  }

  public int size() {
    return preferences.size();
  }
  
  public Collection<OperaPreference> values() {
    return preferences;
  }
  
}