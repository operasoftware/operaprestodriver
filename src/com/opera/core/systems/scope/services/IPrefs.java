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

package com.opera.core.systems.scope.services;

import com.opera.core.systems.scope.protos.PrefsProtos.GetPrefArg.Mode;
import com.opera.core.systems.scope.protos.PrefsProtos.Pref;

import java.util.List;

public interface IPrefs {

  void init();

  /**
   * Gets a preference from the <var>operaprefs.ini</var> from a specific section. Optionally it can
   * get the default setting of the preference.
   *
   * @param section section to get the preference from
   * @param key     key in the section
   * @param mode    get the current or default setting of the preference
   * @return returns the preference in a string
   */
  String getPref(String section, String key, Mode mode);

  /**
   * Lists the preferences in a specific section from <var>operaprefs.ini</var>.
   *
   * @param sort    if true returned list is sorted
   * @param section list prefs in this section
   * @return a list of preferences in a particular section
   */
  List<Pref> listPrefs(Boolean sort, String section);

  /**
   * Sets a preference in <var>operaprefs.ini</var> in a specific section.
   *
   * @param section the section name
   * @param key     the key in the section
   * @param value   value to set this preference to
   */
  void setPrefs(String section, String key, String value);

}