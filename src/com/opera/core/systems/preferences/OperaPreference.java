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
 *
 */
public interface OperaPreference {

  /**
   * Gets the section this preference belongs to.
   *
   * @return the parent section of this preference
   */
  public String getSection();

  /**
   * Gets the key of this preference.
   *
   * @return the key of this preference
   */
  public String getKey();

  /**
   * Gets the value of this preference.
   *
   * @return the value of this preference
   */
  public Object getValue();

  /**
   * Sets the value of this preference.
   *
   * @param value the new value
   */
  public void setValue(Object value);

  /**
   * Gets the default value of the preference.
   *
   * @return the default value of this preference
   */
  public Object getDefaultValue();

}