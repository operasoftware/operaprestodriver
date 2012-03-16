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

import com.opera.core.systems.model.OperaColor;
import com.opera.core.systems.scope.protos.PrefsProtos;
import com.opera.core.systems.scope.services.IPrefs;

import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.lang.reflect.Type;

/**
 * OperaScopePreferences allows updating preferences inside Opera using the Scope protocol using the
 * common {@link OperaPreferences} interface.  The prefs service will be written to upon alteration
 * of any preference.
 *
 * @see OperaFilePreferences
 */
public class OperaScopePreferences extends AbstractOperaPreferences {

  protected IPrefs service;

  /**
   * Opera stores preferences in more field types than what is considered idiomatic to Java.  This
   * is a lookup enum for converting from Opera-specific field types to Java types.
   */
  private enum PrefType {

    BOOLEAN, COLOR, FILE, INTEGER, STRING;

    public static PrefType toJava(PrefsProtos.Pref.Type type) {
      switch (type) {
        case BOOLEAN:
          return BOOLEAN;
        case COLOR:
          return COLOR;
        case DIRECTORY:
        case FILE:
        case REQUIRED_FILE:
          return FILE;
        case INTEGER:
          return INTEGER;
        case STRING:
        default:
          return STRING;
      }
    }

  }

  public OperaScopePreferences(IPrefs prefsService) {
    service = prefsService;
    updatePreferenceCache();  // might be slow on cores without speed patch
  }

  public void set(OperaPreference preference) {
    // Update locally stored value if given value is different from cache's.  We don't care about
    // whether the preference passed into this method is a different type, e.g. GenericPreference,
    // since we have all preferences stored locally.

    for (OperaPreference p : this) {
      if (p.getSection().equalsIgnoreCase(preference.getSection()) &&
          p.getKey().equalsIgnoreCase(preference.getKey())) {
        if (!p.getValue().equals(preference.getValue())) {
          p.setValue(((OperaGenericPreferences.GenericPreference) preference).getValue(true));
        }
        return;
      }
    }

    throw new WebDriverException("Unknown preference: " + preference.toString());
  }

  public void set(String section, String key, Object value) {
    set(new OperaGenericPreferences.GenericPreference(section, key, value));
  }

  public ScopePreference get(String section, String key) {
    return (ScopePreference) super.get(section, key);
  }

  /**
   * Resets all preferences' values to their default value.
   */
  public void resetAll() {
    for (OperaPreference p : this) {
      ((ScopePreference) p).reset();
    }
  }

  /**
   * Invalidates the preferences cache stored locally in the driver and requests a new list of all
   * preferences from Opera.  This should typically only be called the first time {@link
   * OperaScopePreferences} is instantiated.
   *
   * Following that, whenever a preference is updated the driver will be responsible for keeping the
   * local cache up-to-date.  The drawback of this is that if the user manually updates a preference
   * in Opera, this will not be reflected when {@link ScopePreference#getValue()} is run.  We
   * circumvent that issue by fetching the value individually.
   *
   * The reason for this is that we cannot fetch a single message "Pref" through Scope, but only
   * individual values.
   */
  private void updatePreferenceCache() {
    for (PrefsProtos.Pref pref : service.listPrefs(true, null)) {
      preferences.add(new ScopePreference(this, pref));
    }
  }

  /**
   * Allows setting Opera preferences in Opera in addition to keeping the local cache of preferences
   * up to date.
   */
  public static class ScopePreference extends AbstractPreference {

    private OperaScopePreferences parent;
    private PrefsProtos.Pref pref;

    public ScopePreference(OperaScopePreferences parent, PrefsProtos.Pref pref) {
      super(pref.getSection(), pref.getKey(), pref.getValue());
      this.parent = parent;
      this.pref = pref;
    }

    /**
     * Gets the value of the preference in an idiomatic type.  We fall back to {@link
     * java.lang.String} if no type is specified for this preference.
     *
     * @return the value of the string, as an idiomatic Java type
     */
    @Override
    public Object getValue() {
      String value = super.getValue().toString();

      switch (PrefType.toJava(pref.getType())) {
        case BOOLEAN:
          return Boolean.parseBoolean(value);
        case COLOR:
          return OperaColor.decode(value);
        case FILE:
          return new File(value);
        case INTEGER:
          // TODO(andreastt): Opera sometimes stores prefs as integers when they are in fact not integers, file core bug
          try {
            return Integer.parseInt(value);
          } catch (NumberFormatException e) {
            return value;
          }
        case STRING:
        default:
          return value;
      }
    }

    @Override
    public void setValue(Object value) {
      super.setValue(value);  // update cache
      parent.service.setPrefs(getSection(), getKey(), super.getValue(true).toString());
    }

    @Override
    public Object getDefaultValue() {
      return parent.service.getPref(getSection(), getKey(), PrefsProtos.GetPrefArg.Mode.DEFAULT);
    }

    /**
     * Gets the type of the preference.  Since Opera uses more internal field types than what is
     * accessible to Java, we reduce the type specified by Opera (in a lossy way) to one of either
     * {@link java.lang.Boolean}, {@link OperaColor}, {@link java.io.File}, {@link
     * java.lang.Integer} or {@link java.lang.String}.
     *
     * @return the idiomatic Java type for this preference
     */
    public Type getType() {
      switch (PrefType.toJava(pref.getType())) {
        case BOOLEAN:
          return Boolean.class;
        case COLOR:
          return OperaColor.class;
        case FILE:
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

    /**
     * Resets preference's value to its default value.  This method is a short-hand method for doing
     * <code>preference.setValue(preference.getDefaultValue)</code>.
     */
    public void reset() {
      setValue(getDefaultValue());
    }

  }

}