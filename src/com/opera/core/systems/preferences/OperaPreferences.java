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
import java.util.Map;
import java.util.Set;

/**
 * Interface for reading preferences from the Opera web browser.
 */
public interface OperaPreferences {

  public Set<Map.Entry<String, Object>> entrySet();

  public Collection<OperaPreference> values();

  public void merge(OperaPreferences newPreferences);

  public void set(OperaPreference preference);

  public void set(String section, String key, Object value);
  
  public void get(String section, String key);

}