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

package com.opera.core.systems;

/**
 * A list of all Opera's core profiles.  Available core profiles are DESKTOP, SMARTPHONE, TV, MINI
 * and CUSTOM.
 *
 * @see {@link OperaProduct}
 */
public enum OperaCoreProfile {

  CUSTOM("custom"),
  DESKTOP("desktop"),
  SMARTPHONE("smartphone"),
  TV("tv"),
  MINI("mini"),

  /**
   * Never returned, but can be used to request any profile.
   */
  ANY("") {
    @Override
    public boolean is(OperaCoreProfile compareWith) {
      return true;
    }
  };

  OperaCoreProfile(String a) {
  }

  /**
   * Compares current core profile with given core profile.  {@link OperaCoreProfile#ANY} will match
   * any profile.
   *
   * @param compareWith the product to compare the current one with
   * @return true if the products are the same, false otherwise
   */
  public boolean is(OperaCoreProfile compareWith) {
    return this.equals(compareWith);
  }

}