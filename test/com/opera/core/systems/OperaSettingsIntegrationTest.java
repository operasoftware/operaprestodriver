package com.opera.core.systems;

import com.google.common.io.Files;

import com.opera.core.systems.common.io.ProcessManager;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.NeedsLocalEnvironment;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.drivers.TestDriver;
import com.opera.core.systems.testing.drivers.TestDriverBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.net.NetworkUtils;
import org.openqa.selenium.support.ui.Duration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_DEFAULT_PORT;
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_DEFAULT_PORT_IDENTIFIER;
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_RANDOM_PORT_IDENTIFIER;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.Platform.WINDOWS;

@NoDriver
public class OperaSettingsIntegrationTest extends OperaDriverTestCase {

  public static final NetworkUtils NETWORK_UTILS = new NetworkUtils();
  public final Duration defaultHandshakeTimeout = OperaIntervals.HANDSHAKE_TIMEOUT.getValue();

  public OperaSettings settings;

  @Rule
  public TemporaryFolder tmp;

  @Before
  public void beforeEach() {
    tmp = new TemporaryFolder();
    settings = new OperaSettings();
  }

  @Before
  public void setup() {
    OperaIntervals.HANDSHAKE_TIMEOUT.setValue(new Duration(2, TimeUnit.SECONDS));
  }

  @After
  public void reset() {
    OperaIntervals.HANDSHAKE_TIMEOUT.setValue(defaultHandshakeTimeout);
  }

  @Test
  public void binaryIsCorrectlyLaunched() {
    settings.setBinary(OperaBinary.find());
    TestDriver driver = new TestDriverBuilder().using(settings).get();

    assertNotNull(driver);
    assertTrue("Expected Opera to run", driver.isRunning());

    driver.quit();
  }

  @Test
  public void binaryInvalidThrowsException() {
    try {
      settings.setBinary(resources.fakeFile());
      fail("Expected IllegalStateException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(IllegalStateException.class)));
      assertThat(e.getMessage(), containsString("The executable does not exist"));
    }
  }

  @Test
  public void binaryRegistersProduct() {
    settings.setBinary(OperaBinary.find(OperaProduct.MOBILE));

    TestDriver driver = new TestDriverBuilder().using(settings).get();

    assertEquals(OperaProduct.MOBILE, driver.utils().getProduct());
    assertEquals(OperaProduct.MOBILE, driver.getSettings().getProduct());

    driver.quit();
  }

  @Test
  public void autostartIsRespected() {
    settings.autostart(false);

    try {
      new TestDriverBuilder().using(settings).get();
      fail("Expected WebDriverException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(WebDriverException.class)));
      assertThat(e.getMessage(), containsString("No response in a timely fashion"));
    }
  }

  @Test
  public void launcherIsUsedWhenSet() throws IOException {
    tmp.create();
    File newLauncher = tmp.newFile("newLauncher");
    Files.copy(OperaLauncherRunner.LAUNCHER_DEFAULT_LOCATION, newLauncher);
    if (!Platform.getCurrent().is(WINDOWS)) {
      newLauncher.setExecutable(true);
    }

    settings.setLauncher(newLauncher);
    TestDriver driver = new TestDriverBuilder().using(settings).get();

    assertNotNull(driver);
    assertEquals(newLauncher, driver.getSettings().getLauncher());

    driver.quit();
  }

  @Test
  @Ignore(value = "Needs investigation")
  public void loggingFileReceivesOutput() throws IOException {
    tmp.create();
    File log = tmp.newFile("operadriver.log");

    settings.logging().setFile(log);
    settings.logging().setLevel(Level.FINER);

    TestDriver driver = new TestDriverBuilder().using(settings).get();
    driver.quit();

    assertTrue(log.length() > 0);
  }

  @Test
  @Ignore(products = CORE, value = "core does not support -pd")
  public void profileIsRespected() throws IOException {
    tmp.create();
    File profile = tmp.newFolder();
    settings.setProfile(profile.getPath());
    TestDriver driver = new TestDriverBuilder().using(settings).get();

    assertNotNull(driver);
    assertEquals(profile, driver.preferences().get("User ScopePrefs", "Opera Directory").getValue());
    assertEquals(profile, driver.getSettings().profile().getDirectory());

    driver.quit();
  }

  @Test
  public void profileCanBeSetUsingString() throws IOException {
    tmp.create();
    File profileDirectory = tmp.newFolder("profile");
    settings.setProfile(profileDirectory.getPath());
    assertEquals(profileDirectory.getPath(), settings.profile().getDirectory().getPath());
  }

  @Test
  @Ignore("CORE-44852: Unable to automatically connect debugger to non-loopback address")
  public void hostIsRespectedOnLaunch() {
    String host = NETWORK_UTILS.getIp4NonLoopbackAddressOfThisMachine().getHostAddress();
    settings.setHost(host);
    TestDriver driver = new TestDriverBuilder().using(settings).get();

    assertNotNull(driver);
    assertEquals(host, driver.getSettings().getHost());
    assertEquals(host, driver.preferences().get("Developer Tools", "Proxy Host").getValue());

    driver.quit();
  }

  @Test
  public void portCanBeSet() {
    settings.setPort(1234);
    TestDriver driver = new TestDriverBuilder().using(settings).get();
    assertNotNull(driver);
    assertEquals(1234, driver.getSettings().getPort());
    driver.quit();
  }

  @Test
  public void portSetToRandomIdentifier() {
    settings.setPort(SERVER_RANDOM_PORT_IDENTIFIER);
    assertNotSame(SERVER_DEFAULT_PORT, settings.getPort());
    assertDriverCreated(settings);
  }

  @Test
  public void portSetToDefaultIdentifier() {
    settings.setPort(SERVER_DEFAULT_PORT_IDENTIFIER);
    assertEquals(SERVER_DEFAULT_PORT, settings.getPort());
    assertDriverCreated(settings);
  }

  @Test
  public void runnerIsUsed() {
    settings.setRunner(OperaLauncherRunner.class);
    TestDriver driver = new TestDriverBuilder().using(settings).get();
    assertNotNull(driver);
    assertEquals(driver.getRunner().getClass(), settings.getRunner().getClass());
  }

  @Test
  @Ignore(products = {CORE, MOBILE},
          value = "core does not reset port number if -debugproxy is omitted")
  public void settingPort() {
    settings.setPort(SERVER_DEFAULT_PORT_IDENTIFIER);
    TestDriver driver = new TestDriverBuilder().using(settings).get();
    assertNotNull(driver);
    driver.quit();
  }

  /**
   * This test is known to fail if OPERA_PATH points to a shell script or a symlink, as is the case
   * on Debian.
   */
  @Test
  @Ignore(platforms = LINUX)
  public void environmentalBinaryPathWorks() throws IOException {
    File binary = OperaBinary.find();
    environment.set(OperaBinary.OPERA_PATH_ENV_VAR, binary.getPath());

    TestDriver driver = new TestDriverBuilder().get();

    assertEquals(binary.getCanonicalPath(), driver.getSettings().getBinary().getCanonicalPath());
    assertEquals(binary.getCanonicalPath(), driver.utils().getBinaryPath());

    driver.quit();
  }

  @Test
  @NeedsLocalEnvironment
  public void detachBrowser() {
    OperaSettings settings = new OperaSettings();
    settings.setDetach(true);

    TestDriver driver = new TestDriverBuilder().using(settings).get();

    Integer processID = driver.utils().getPID();
    assertNotNull("Failed to get process ID from driver", processID);

    driver.quit();

    assertFalse("OperaDriver should not be connected to Opera", driver.getServices().isConnected());
    assertTrue("Opera should be running", ProcessManager.isPidRunning(processID));

    ProcessManager.killPID(processID);
  }

  private void assertDriverCreated(OperaSettings settings) {
    TestDriver driver = new TestDriverBuilder().using(settings).get();
    assertNotNull(driver);
    driver.quit();
  }

}