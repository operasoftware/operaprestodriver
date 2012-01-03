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
import java.util.List;

/**
 * OperaScopePreferences allows updating preferences inside Opera using the Scope protocol using the
 * common {@link OperaPreferences} interface.  The prefs service will be written to upon alteration
 * of any preference.
 *
 * @see OperaFilePreferences
 */
public class OperaScopePreferences extends AbstractOperaPreferences {

  protected IPrefs service;

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
          p.setValue(preference.getValue());
        }
        return;
      }
    }

    throw new WebDriverException("Unknown preference: [section: '" +
                                 preference.getSection() + "', key: '" +
                                 preference.getKey() + "']");
  }

  public void set(String section, String key, Object value) {
    set(new OperaGenericPreferences.GenericPreference(section, key, value));
  }

  /**
   * Resets all preferences values to their default value.
   */
  public void resetAll() {
    for (OperaPreference p : this) {
      p.setValue(p.getDefaultValue());
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
    List<PrefsProtos.Pref> allPrefs = service.listPrefs(true, null);

    for (PrefsProtos.Pref pref : allPrefs) {
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

    public void setValue(Object value) {
      super.setValue(value);  // update cache
      parent.service.setPrefs(getSection(), getKey(), super.getValue(true).toString());
    }

    public Object getDefaultValue() {
      return parent.service.getPref(getSection(), getKey(), PrefsProtos.GetPrefArg.Mode.DEFAULT);
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

}