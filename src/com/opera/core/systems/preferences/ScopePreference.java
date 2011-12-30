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
import com.opera.core.systems.model.OperaColor;
import com.opera.core.systems.scope.protos.PrefsProtos;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Allows setting Opera preferences in Opera in addition to keeping the local cache of preferences
 * up to date.
 */
public class ScopePreference extends AbstractPreference {

  private OperaScopePreferences parent;
  private PrefsProtos.Pref pref;

  public ScopePreference(OperaScopePreferences parent, PrefsProtos.Pref pref) {
    super(pref.getSection(), pref.getKey(), pref.getValue());
    this.parent = parent;
    this.pref = pref;
    setValue(getValue());
  }

  /**
   * Preferences in Opera is a messy business.  Some boolean values are stored as integers and
   * everything will be returned as strings due to the nature of Google protobuffers.  This is an
   * attempt at making sense of it all because we want the user-facing API to be nice:  If the user
   * queries a pref's value and the value is _similar_ to a boolean value, we will return it as a
   * boolean.
   *
   * Values stored as strings with a truthy value ("0" or "1") are considered boolean.  Values
   * stored as integers will always be returned as integers.  Other values will be returned as
   * objects and will rely on Java autoboxing.
   *
   * @return object, integer, or boolean depending on the nature of the value
   */
  public Object getValue() {
    Object value = super.getValue();

    if (getType() == Boolean.class && isTruthy(value)) {
      return value.equals("1");
    } else if (getType() == Integer.class && OperaStrings.isNumeric(value.toString())) {
      return Integer.valueOf(value.toString());
    }

    return value;
  }

  public void setValue(Object value) {
    super.setValue(value);  // update cache
    parent.service.setPrefs(getSection(), getKey(), super.getValue().toString());
  }

  public Object getDefaultValue() {
    return pref.getDefaultInstanceForType().getValue();
  }

  /**
   * Returns which type the preference is considered internally in Opera.
   *
   * @return Integer, String or Boolean type
   */
  public Type getType() {
    switch (pref.getType()) {
      case BOOLEAN:
        return Boolean.class;
      case COLOR:
        return OperaColor.class;
      case DIRECTORY:
      case FILE:
      case REQUIRED_FILE:
        return File.class;
      case INTEGER:
        return Integer.class;
      case STRING:
      default:
        return String.class;
    }
  }

  /**
   * Whether setting is overriden by global INI.  If this value is missing, it means global INI is
   * not supported.
   *
   * @return true if enabled, false otherwise
   */
  public boolean isEnabled() {
    return pref.getEnabled();
  }

}