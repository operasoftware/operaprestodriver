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

import com.opera.core.systems.scope.protos.PrefsProtos.GetPrefArg.Mode;
import com.opera.core.systems.scope.services.IPrefs;

/**
 * Allows setting Opera preferences in Opera in addition to keeping the local cache
 * of preferences up to date.
 */
public class ScopePreference extends AbstractOperaPreference {
  
  private OperaScopePreferences parent;

  public ScopePreference(OperaScopePreferences parent, String section, String key, Object value) {
    super(section, key, value);
    this.parent = parent;
  }
  
  public Object getValue() {
    super.setValue(parent.service.getPref(getSection(), getKey(), Mode.CURRENT));
    return super.getValue();
  }
  
  public void setValue(Object value) {
    super.setValue(value);
    parent.service.setPrefs(getSection(), getKey(), getValue().toString());
  }
  
  public Object getDefaultValue() {
    defaultValue = parent.service.getPref(getSection(), getKey(), Mode.DEFAULT);
    return defaultValue;
  }

  public static ScopePreference convert(OperaScopePreferences parent, OperaPreference convertee) {
    return new ScopePreference(parent,
                               convertee.getSection(),
                               convertee.getKey(),
                               convertee.getValue());
  }

}