/*
Copyright 2012 Opera Software ASA

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

import org.openqa.selenium.Dimension;

/**
 * Represents an emulation profile for Opera.  It allows Opera to emulate another device with
 * radically different characteristics, such as a different input mechanism (IME), different
 * resolution, or a different user agent string.
 */
public interface EmulationProfile {

  enum IME {NATIVE, TOUCH, KEYPAD, TABLET}

  /**
   * Gets a unique name describing the emulation profile.
   */
  String getProfileName();

  /**
   * Gets the resolution of the device's screen.
   */
  Dimension getResolution();

  /**
   * Gets the pixel density of the device's screen.
   */
  int getPPI();

  /**
   * Gets the input mechanism of this profile.
   */
  IME getIME();

  /**
   * Gets the user agent string.
   */
  String getUserAgent();

}