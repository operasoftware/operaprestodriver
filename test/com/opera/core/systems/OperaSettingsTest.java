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

import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.runner.AbstractOperaRunner;
import com.opera.core.systems.runner.OperaRunnerException;
import com.opera.core.systems.runner.inprocess.OperaInProcessRunner;
import com.opera.core.systems.runner.interfaces.OperaRunner;
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
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import static com.opera.core.systems.OperaBinary.OPERA_PATH_ENV_VAR;
import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;
import static com.opera.core.systems.OperaSettings.Capability.AUTOSTART;
import static com.opera.core.systems.runner.launcher.OperaLauncherRunner.LAUNCHER_ENV_VAR;
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_DEFAULT_PORT;
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_DEFAULT_PORT_IDENTIFIER;
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_RANDOM_PORT_IDENTIFIER;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.Platform.WINDOWS;

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
  public void binaryDefaultIsNull() {
    assertNull(settings.getBinary());
  }

  @Test
  public void binaryCanBeSet() {
    try {
      settings.setBinary(resources.fakeFile());
      fail("Expected IllegalStateException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(IllegalStateException.class)));
      assertThat(e.getMessage(), containsString("The executable does not exist"));
    }
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
    assertNotSame(SERVER_DEFAULT_PORT, settings.getPort());
  }

  @Test
  public void portSetToDefaultPortIdentifier() {
    settings.setPort(SERVER_DEFAULT_PORT_IDENTIFIER);
    assertThat(settings.getPort(), is(SERVER_DEFAULT_PORT));
  }

  @Test
  public void portSetToRandomPortIdentifier() {
    settings.setPort(SERVER_RANDOM_PORT_IDENTIFIER);
    assertThat(settings.getPort(), is(not(SERVER_DEFAULT_PORT)));
  }

  @Test
  public void portSetToSomethingElseIsRespected() {
    settings.setPort(1234);
    assertThat(settings.getPort(), is(1234));
  }

  @Test
  public void launcherReturnsLauncherLocationByDefault() {
    assertNotNull(settings.getLauncher());
    assertEquals(OperaLauncherRunner.LAUNCHER_DEFAULT_LOCATION, settings.getLauncher());
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
  public void profileIsOperaProfileObjectByDefault() {
    assertNotNull(settings.profile());
  }

  @Test
  public void profileCanBeSet() {
    OperaProfile profile = new OperaProfile();
    settings.setProfile(profile);
    assertEquals(profile, settings.profile());
  }

  @Test
  public void profileSetToNullThrows() {
    try {
      settings.setProfile((OperaProfile) null);
      fail("Expected NullPointerException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(NullPointerException.class)));
    }
  }

  @Test
  public void profileSetToNullStringThrows() {
    try {
      settings.setProfile((String) null);
      fail("Expected NullPointerException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(NullPointerException.class)));
    }
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
    try {
      settings.setDisplay(7);
      if (Platform.getCurrent().is(LINUX)) {
        fail("Expected exception on setting the framebuffer display");
      }
    } catch (RuntimeException e) {
      if (!Platform.getCurrent().is(LINUX)) {
        assertThat(e, is(instanceOf(UnsupportedOperationException.class)));
      }
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
    assertEquals(SERVER_DEFAULT_PORT, settings.getPort());
  }

  @Test
  public void proxyIsSetByDefault() {
    assertThat(settings.getProxy(), is(instanceOf(Proxy.class)));
  }

  @Test
  public void proxyCanBeSet() {
    Proxy proxy = new Proxy();
    proxy.setHttpProxy("4.4.4.4");

    settings.setProxy(proxy);
    assertEquals(proxy, settings.getProxy());
    assertEquals("4.4.4.4", settings.getProxy().getHttpProxy());
  }

  @Test
  public void proxyCanBeSetToNull() {
    settings.setProxy(null);
    assertNull(settings.getProxy());
  }

  @Test
  public void runnerReturnsInstanceOfOperaLauncherRunner() {
    AbstractOperaRunner runner = (AbstractOperaRunner) settings.getRunner();
    assertThat(runner, is(instanceOf(OperaLauncherRunner.class)));
    assertEquals(settings, runner.getSettings());
  }

  @Test
  public void runnerCanBeSet() {
    settings.setRunner(OperaInProcessRunner.class);

    AbstractOperaRunner runner = (AbstractOperaRunner) settings.getRunner();
    assertThat(runner, is(instanceOf(OperaInProcessRunner.class)));
    assertEquals(settings, runner.getSettings());
  }

  @Test
  public void runnerWithInvalidConstructor() {
    settings.setRunner(BadConstructorRunner.class);

    try {
      settings.getRunner();
      fail("Expected WebDriverException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(WebDriverException.class)));
      assertThat(e.getMessage(), containsString("Invalid constructor in runner"));
    }
  }

  @Test
  public void runnerThatThrowsDuringConstruction() {
    settings.setRunner(ExceptionOnConstructionRunner.class);

    try {
      settings.getRunner();
      fail("Expected WebDriverException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(WebDriverException.class)));
      assertThat(e.getMessage(), containsString("Runner threw exception on construction"));
    }
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
    settings.setPort(SERVER_DEFAULT_PORT_IDENTIFIER);
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

  /*
  @Test
  public void environmentalBinaryPathIsRespected() throws IOException {
    environment.set(OPERA_PATH_ENV_VAR, resources.executableBinary().getPath());
    OperaSettings settings = new OperaSettings();  // env var needs to be set before construction
    assertEquals(resources.executableBinary().getCanonicalPath(),
                 settings.getBinary().getCanonicalPath());
  }
  */

  public static abstract class MockRunner implements OperaRunner {

    public void startOpera() throws OperaRunnerException {
    }

    public void stopOpera() throws OperaRunnerException {
    }

    public boolean isOperaRunning() {
      return false;
    }

    public boolean hasOperaCrashed() {
      return false;
    }

    public String getOperaCrashlog() {
      return null;
    }

    public void shutdown() {
    }

    public ScreenShotReply saveScreenshot(long timeout, String... hashes)
        throws OperaRunnerException {
      return null;
    }

  }

  public static class BadConstructorRunner extends MockRunner {

  }

  public static class ExceptionOnConstructionRunner extends MockRunner {

    public ExceptionOnConstructionRunner(OperaSettings settings) {
      throw new UnsupportedOperationException("hoobaflooba");
    }

  }

}