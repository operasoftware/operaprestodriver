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

package com.opera.core.systems.mobile;

import com.opera.core.systems.EmulationProfile;

import static com.opera.core.systems.EmulationProfile.IME.KEYPAD;
import static com.opera.core.systems.EmulationProfile.IME.TABLET;
import static com.opera.core.systems.mobile.OperaMobileEmulation.UserAgent.DESKTOP;
import static com.opera.core.systems.mobile.OperaMobileEmulation.UserAgent.MEEGO;
import static com.opera.core.systems.mobile.OperaMobileEmulation.UserAgent.S60;
import static com.opera.core.systems.mobile.OperaMobileEmulation.builder;

/**
 * A convenience list of different mobile device's emulation settings for use with {@link
 * com.opera.core.systems.OperaSettings#emulate(MobileDevices)}.
 *
 * The list is kept in sync with the profile list in the <code>omlauncher</code> binary shipped
 * alongside the Opera Mobile Emulator.
 */
public enum MobileDevices {

  AMAZON_KINDLE_FIRE(builder().setResolution(600, 1024).setPPI(169).setIME(TABLET).build()),

  ASUS_EEE_PAD_SLIDER_SL101(builder().setWidth(1280).setIME(TABLET).build()),

  ASUS_EEE_PAD_TRANSFORMER_PRIME(builder().setWidth(1280).setPPI(149).setIME(TABLET).build()),

  ASUS_EEE_PAD_TRANSFORMER_TF101(ASUS_EEE_PAD_TRANSFORMER_PRIME.getProfile()),

  EXOPC_SLATE(
      builder().setResolution(1366, 768).setPPI(135).setIME(TABLET).setUserAgent(DESKTOP).build()),

  HTC_DESIRE(builder().setPPI(252).build()),

  HTC_EVO_3D(builder().setResolution(540, 960).setPPI(256).build()),

  HTC_EVO_4G(builder().setPPI(216).build()),

  HTC_FLYER(builder().setResolution(1024, 600).setPPI(170).setIME(TABLET).build()),

  HTC_HERO(builder().setResolution(320, 480).setPPI(180).build()),

  HTC_INSPIRE_4G(builder().setPPI(216).build()),

  HTC_SENSATION_4G(builder().setResolution(540, 960).setPPI(256).build()),

  HTC_SENSATION(builder().setResolution(540, 960).setPPI(253).build()),

  HTC_TATTOO(builder().setResolution(240, 320).setPPI(142).build()),

  HTC_WILDFIRE(builder().setResolution(240, 320).setPPI(120).build()),

  LG_OPTIMUS_3D(builder().setPPI(216).build()),

  LG_OPTIMUS_ONE(builder().setResolution(320, 480).setPPI(160).build()),

  LG_T_MOBILE_G2X(builder().setPPI(233).build()),

  LENOVO_IDEAPAD_K1(builder().setResolution(1280, 149).setPPI(149).setIME(TABLET).build()),

  MOTOROLA_ATRIX_4G(builder().setResolution(540, 960).setPPI(267).build()),

  MOTOROLA_DROID_X(builder().setResolution(854, 228).setPPI(228).build()),

  MOTOROLA_PHOTON_4G(builder().setResolution(540, 960).setPPI(256).build()),

  MOTOROLA_XOOM(builder().setResolution(1280, 149).setPPI(149).setIME(TABLET).build()),

  NOKIA_5530_XPRESSMUSIC(
      builder().setResolution(360, 640).setPPI(253).setIME(KEYPAD).setUserAgent(S60).build()),

  NOKIA_5800_XPRESSMUSIC(builder().setResolution(360, 640).setPPI(229).setUserAgent(S60).build()),

  NOKIA_C7_00(builder().setResolution(360, 640).setPPI(210).setUserAgent(S60).build()),

  NOKIA_E63(builder().setResolution(240, 320).setPPI(170).setIME(KEYPAD).setUserAgent(S60).build()),

  NOKIA_N800(builder().setResolution(640, 360).setPPI(225).setUserAgent(MEEGO).build()),

  NOKIA_N9(builder().setHeight(854).setPPI(252).setUserAgent(MEEGO).build()),

  NOKIA_N900_MAEMO(builder().setResolution(800, 480).setPPI(267).build()),

  SEMC_XPERIA_RAY(builder().setHeight(854).setPPI(297).build()),

  SEMC_XPERIA_X10(builder().setHeight(854).setPPI(240).build()),

  SAMSUNG_EPIC_4G(builder().setPPI(233).build()),

  SAMSUNG_GALAXY_NEXUS(builder().setResolution(720, 1280).setPPI(316).build()),

  SAMSUNG_GALAXY_NOTE(builder().setResolution(800, 1280).setPPI(285).build()),

  SAMSUNG_GALAXY_S_II(builder().setPPI(216).build()),

  SAMSUNG_GALAXY_S(builder().setPPI(240).build()),

  SAMSUNG_GALAXY_TAB_10_1(builder().setWidth(1280).setPPI(149).setIME(TABLET).build()),

  SAMSUNG_GALAXY_TAB_7_10_PLUS(
      builder().setResolution(1024, 600).setPPI(170).setIME(TABLET).build()),

  SAMSUNG_GALAXY_TAB_8_9(builder().setWidth(1280).setPPI(169).setIME(TABLET).build()),

  SAMSUNG_GALAXY_TAB(builder().setResolution(1024, 600).setPPI(240).setIME(TABLET).build()),

  SONY_TABLET_S(builder().setWidth(1280).setPPI(160).setIME(TABLET).build()),

  TOSHIBA_THRIVE_10_1(builder().setWidth(1024).setPPI(160).setIME(TABLET).build());

  private final EmulationProfile profile;

  MobileDevices(EmulationProfile profile) {
    this.profile = profile;
  }

  /**
   * Retrieve the emulation profile of the device.
   *
   * @return emulation profile matching the settings of the device
   */
  public EmulationProfile getProfile() {
    return profile;
  }

  /**
   * Unique string identifying the device.  This is used for {@link com.opera.core.systems.EmulationProfile#getProfileName()}.
   * It will also be the basis of the emulation profile identification if the user provides a string
   * to the <code>opera.emulationProfile</code> capability.
   *
   * @return unique string describing this device
   * @see #find(String)
   */
  @Override
  public String toString() {
    return name().toLowerCase().replace('_', ' ');
  }

  /**
   * Matches the given string to an Opera Mobile emulation profile.  Spaces and dashes will be
   * converted to underscores to match an enum value in {@link MobileDevices}.
   *
   * @param identifier the name of an emulation profile
   * @return a mobile device
   */
  public static MobileDevices find(String identifier) {
    identifier = identifier.toUpperCase().replace('.', '_').replace('-', '_');

    for (MobileDevices device : values()) {
      if (device.name().equals(identifier)) {
        return device;
      }
    }

    return null;
  }

}