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

import com.opera.core.systems.mobile.OperaMobileEmulation;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.openqa.selenium.Dimension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class OperaMobileEmulationTest {

  @Test
  public void builderPopulates() {
    OperaMobileEmulation.Builder builder = OperaMobileEmulation.builder();

    builder.setResolution(new Dimension(100, 200));
    builder.setPPI(300);
    builder.setProfileName("hoobaflooba");
    builder.setIME(EmulationProfile.IME.KEYPAD);
    builder.setUserAgent(OperaMobileEmulation.UserAgent.MEEGO);

    EmulationProfile profile = builder.build();

    assertEquals(new Dimension(100, 200), profile.getResolution());
    assertEquals(300, profile.getPPI());
    assertEquals("hoobaflooba", profile.getProfileName());
    assertEquals(EmulationProfile.IME.KEYPAD, profile.getIME());
    assertEquals(OperaMobileEmulation.UserAgent.MEEGO.toString(), profile.getUserAgent());
  }

  @Test
  public void equality() {
    OperaMobileEmulation.Builder builder = OperaMobileEmulation.builder();

    builder.setResolution(new Dimension(100, 200));
    builder.setPPI(300);
    builder.setProfileName("hoobaflooba");
    builder.setIME(EmulationProfile.IME.KEYPAD);
    builder.setUserAgent(OperaMobileEmulation.UserAgent.MEEGO);

    EmulationProfile profile = builder.build();

    EmulationProfile expectedProfile = new EmulationProfile() {
      public String getProfileName() {
        return "hoobaflooba";
      }

      public Dimension getResolution() {
        return new Dimension(100, 200);
      }

      public int getPPI() {
        return 300;
      }

      public IME getIME() {
        return IME.KEYPAD;
      }

      public String getUserAgent() {
        return OperaMobileEmulation.UserAgent.MEEGO.toString();
      }

      public String toString() {
        return String
            .format(
                "EmulationProfile [profileName=%s, width=%d, height=%d, ppi=%d, ime=%s, userAgent=%s]",
                getProfileName(), getResolution().getWidth(), getPPI(), getResolution().getHeight(),
                getIME().toString().toLowerCase(), getUserAgent());
      }
    };

    assertThat(expectedProfile, is(equalTo(profile)));
  }

  @Test
  public void builderShorthandWidth() {
    assertEquals(123,
                 OperaMobileEmulation.builder().setWidth(123).build().getResolution().getWidth());
  }

  @Test
  public void builderShorthandHeight() {
    assertEquals(123,
                 OperaMobileEmulation.builder().setHeight(123).build().getResolution().getHeight());
  }

  @Test
  public void builderShorthandResolution() {
    assertEquals(new Dimension(100, 200),
                 OperaMobileEmulation.builder().setResolution(100, 200).build().getResolution());
  }

  @Test
  public void defaultsAreCorrect() {
    EmulationProfile profile = OperaMobileEmulation.builder().build();

    assertEquals(null, profile.getProfileName());
    assertEquals(new Dimension(480, 800), profile.getResolution());
    assertEquals(233, profile.getPPI());
    assertEquals(EmulationProfile.IME.TOUCH, profile.getIME());
    assertEquals(OperaMobileEmulation.UserAgent.ANDROID.toString(), profile.getUserAgent());
  }

  @Test
  public void toJsonSerializesProperly() throws JSONException {
    OperaMobileEmulation profile = (OperaMobileEmulation) OperaMobileEmulation.builder()
        .setProfileName("hoobaflooba").build();
    JSONObject json = profile.toJson();

    assertEquals(profile.getProfileName(), json.get("profileName"));
    assertEquals(profile.getResolution().getWidth(), json.get("width"));
    assertEquals(profile.getResolution().getHeight(), json.get("height"));
    assertEquals(profile.getPPI(), json.get("ppi"));
    assertEquals(profile.getIME().toString().toLowerCase(), json.get("ime"));
    assertEquals(profile.getUserAgent(), json.get("userAgent"));
  }

  @Test
  public void fromJsonDeserializesProperly() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("profileName", "hoobaflooba");
    json.put("width", 100);
    json.put("height", 200);
    json.put("ppi", 300);
    json.put("ime", "tablet");
    json.put("userAgent", "MeeGo");

    EmulationProfile profile = OperaMobileEmulation.fromJson(json);

    assertEquals("hoobaflooba", profile.getProfileName());
    assertEquals(new Dimension(100, 200), profile.getResolution());
    assertEquals(300, profile.getPPI());
    assertEquals(EmulationProfile.IME.TABLET, profile.getIME());
    assertEquals(OperaMobileEmulation.UserAgent.MEEGO.toString(), profile.getUserAgent());
  }

}