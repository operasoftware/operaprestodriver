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

import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import static com.opera.core.systems.OperaPaths.OPERA_PATH_ENV_VAR;
import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;
import static com.opera.core.systems.OperaSettings.Capability.AUTOSTART;
import static com.opera.core.systems.runner.launcher.OperaLauncherRunner.LAUNCHER_ENV_VAR;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_DEFAULT_PORT;
import static com.opera.core.systems.scope.internal.OperaIntervals.SERVER_DEFAULT_PORT_IDENTIFIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.Platform.WINDOWS;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
@NoDriver
public class OperaSettingsTest extends OperaDriverTestCase {

  public OperaSettings settings;

  @Before
  public void beforeEach() {
    settings = new OperaSettings();
  }

  @After
  public void afterEach() {
    environment.unset(LAUNCHER_ENV_VAR);
    environment.unset(OPERA_PATH_ENV_VAR);
  }

  @Test
  public void construction() {
    assertNotNull(settings);
  }

  @Test
  public void loggingLevelIsInfo() {
    assertEquals(Level.INFO, settings.logging().getLevel());
  }

  @Test
  public void loggingLevelCanBeSet() {
    settings.logging().setLevel(Level.WARNING);
    assertEquals(Level.WARNING, settings.logging().getLevel());
  }

  @Test
  public void loggingFileIsNull() {
    assertNull(settings.logging().getFile());
  }

  @Test
  public void loggingFileCanBeSet() throws IOException {
    settings.logging().setFile(resources.textFile());
    assertEquals(resources.textFile(), settings.logging().getFile());
  }

  @Test(expected = WebDriverException.class)
  public void loggingFileToInvalidFileThrowsException() {
    settings.logging().setFile(resources.fakeFile());
  }

  @Test
  public void binaryDefaultOpera() {
    assertNotNull(settings.getBinary());
    assertTrue(settings.getBinary().exists());
  }

  @Test
  public void binaryCanBeSet() {
    settings.setBinary(resources.fakeFile());
    assertEquals(resources.fakeFile(), settings.getBinary());
  }

  @Test
  public void argumentsIsOperaArgumentsObject() {
    assertNotNull(settings.arguments());
  }

  @Test
  public void hostIsLoopbackAddressByDefault() {
    assertEquals("127.0.0.1", settings.getHost());
  }

  @Test
  public void hostCanBeSet() {
    settings.setHost("1.2.3.4");
    assertEquals("1.2.3.4", settings.getHost());
  }

  @Test
  public void portIsRandom() {
    // Of course, it might still happen that it assigns it to 7001...
    assertNotSame((int) SERVER_DEFAULT_PORT.getValue(), settings.getPort());
  }

  @Test
  public void launcherReturnsLauncherLocationByDefault() {
    assertNotNull(settings.getLauncher());
    assertEquals(OperaLauncherRunner.launcherDefaultLocation(), settings.getLauncher());
  }

  @Test
  @Ignore(platforms = WINDOWS, value = "Unable to set env vars on runtime on Windows")
  public void launcherReturnsEnvironmentalLauncherIfSet() {
    environment.set(LAUNCHER_ENV_VAR, resources.fakeFile().getPath());
    assertEquals(resources.fakeFile(), new OperaSettings().getLauncher());
  }

  @Test
  @Ignore(platforms = WINDOWS, value = "Unable to set env vars on runtime on Windows")
  public void launcherPrioritizesEnvironmentalLauncher() throws IOException {
    environment.set(LAUNCHER_ENV_VAR, resources.fakeFile().getPath());
    OperaSettings settings = new OperaSettings();
    settings.setLauncher(resources.executableBinary());
    assertEquals(resources.fakeFile(), settings.getLauncher());
  }

  @Test
  public void launcherCanBeSet() throws IOException {
    settings.setLauncher(resources.executableBinary());
    assertEquals(resources.executableBinary(), settings.getLauncher());
  }

  @Test(expected = IOException.class)
  public void launcherThrowsExceptionIfInvalid() throws IOException {
    settings.setLauncher(resources.fakeFile());
  }

  @Test
  public void profileIsOperaProfileObject() {
    assertNotNull(settings.profile());
  }

  @Test
  public void profileCanBeSet() {
    OperaProfile profile = new OperaProfile();
    settings.setProfile(profile);
    assertEquals(profile, settings.profile());
  }

  @Test
  public void profileCanBeSetUsingNull() {
    settings.setProfile((String) null);
    assertNotNull(settings.profile());
  }

  @Test
  public void profileCanBeSetUsingEmptyString() {
    settings.setProfile("");
    assertFalse(settings.supportsPd());
    assertNotNull(settings.profile());
  }

  @Test
  public void idleIsFalse() {
    assertEquals(false, settings.useIdle());
  }

  @Test
  public void idleCanBeSet() {
    settings.setIdle(true);
    assertEquals(true, settings.useIdle());
  }

  @Test
  public void displayIsNull() {
    assertNull(settings.getDisplay());
  }

  @Test
  public void displayCanBeSetOnLinuxOnly() {
    Exception ex = null;
    try {
      settings.setDisplay(7);
    } catch (UnsupportedOperationException e) {
      ex = e;
    }

    if (!Platform.getCurrent().is(LINUX) && ex == null) {
      fail("Expected exception on setting the framebuffer display");
    }
  }

  @Test
  public void autostartIsTrueByDefault() {
    assertTrue(settings.autostart());
  }

  @Test
  public void autostartCanBeSet() {
    settings.autostart(false);
    assertFalse(settings.autostart());
  }

  @Test
  public void autostartSetToFalseSetsPortToDefault() {
    settings.autostart(false);
    assertEquals(SERVER_DEFAULT_PORT.getValue(), settings.getPort());
  }

  @Test
  public void noRestartIsFalse() {
    assertFalse(settings.noRestart());
  }

  @Test
  public void noRestartCanBeSet() {
    settings.noRestart(true);
    assertTrue(settings.noRestart());
  }

  @Test
  public void detachIsFalse() {
    assertFalse(settings.hasDetach());
  }

  @Test
  public void detachCanBeSet() {
    settings.setDetach(true);
    assertTrue(settings.hasDetach());
  }

  @Test
  public void productIsDesktop() {
    assertEquals(DESKTOP, settings.getProduct());
  }

  @Test
  public void productCanBeSet() {
    settings.setProduct(CORE);
    assertEquals(CORE, settings.getProduct());
  }

  @Test
  public void backendIsSoftware() {
    assertEquals("software", settings.getBackend());
  }

  @Test
  public void backendCanBeSet() {
    settings.setBackend("hardware");
    assertEquals("hardware", settings.getBackend());
  }

  @Test
  public void supportsDebugProxyIsTrueByDefault() {
    assertTrue(settings.supportsDebugProxy());
  }

  @Test
  public void supportsDebugProxyIsFalseIfDefaultServerPort() {
    settings.setPort((int) SERVER_DEFAULT_PORT_IDENTIFIER.getValue());
    assertFalse(settings.supportsDebugProxy());
  }

  @Test
  public void supportsPdIsTrueByDefault() {
    assertTrue(settings.supportsPd());
  }

  @Test
  public void supportsPdIsFalseIfProfileIsEmptyString() {
    settings.setProfile("");
    assertFalse(settings.supportsPd());
  }

  @Test
  public void supportsPdIsFalseIfProductIsCore() {
    settings.setProduct(CORE);
    assertFalse(settings.supportsPd());
  }

  @Test
  public void supportsPdIsFalseIfProductIsCoreAndProfileIsSet() {
    settings.setProduct(CORE);
    settings.setProfile(new OperaProfile());
    assertFalse(settings.supportsPd());
  }

  @Test
  public void canBeConvertedToCapabilities() {
    Capabilities capabilities = settings.toCapabilities();

    assertNotNull(capabilities);
    assertTrue(capabilities.asMap().size() > 0);
    assertEquals(true, capabilities.getCapability(AUTOSTART.getCapability()));
  }

  @Test
  public void surplusCapabilitiesAreIncludedWhenConvertedToCapabilities() {
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability("foo", "bar");

    settings.merge(capabilities);

    for (Map.Entry<String, ?> capability : settings.toCapabilities().asMap().entrySet()) {
      if (capability.getKey().equals("foo") && capability.getValue().equals("bar")) {
        return;
      }
    }

    fail("Surplus capabilities not included in OperaSettings#toCapabilities()");
  }

  @Test
  public void toJsonConvertsBooleansToIntegers() throws IOException, JSONException {
    assertEquals(1, settings.toJson().get("autostart"));
  }

  @Test
  public void toJsonConvertsProfileToJson() throws IOException, JSONException {
    assertFalse(settings.toJson().get("profile") instanceof OperaProfile);
  }

  @Test
  public void toJsonConvertsArgumentsToJson() throws IOException, JSONException {
    assertFalse(settings.toJson().get("arguments") instanceof OperaArguments);
  }

  @Test
  public void toJsonConvertsEverythingElseToStrings() throws IOException, JSONException {
    assertEquals("127.0.0.1", settings.toJson().get("host"));
  }

  @Test
  public void stringRepresentation() {
    assertNotNull(settings.toString());
  }

  @Test
  public void environmentalBinaryPathIsRespected() throws IOException {
    environment.set(OPERA_PATH_ENV_VAR, resources.executableBinary().getPath());
    OperaSettings settings = new OperaSettings();  // env var needs to be set before construction
    assertEquals(resources.executableBinary().getCanonicalPath(),
                 settings.getBinary().getCanonicalPath());
  }

}