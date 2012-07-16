package com.opera.core.systems;

import com.google.common.io.Files;

import com.opera.core.systems.common.io.ProcessManager;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.NeedsLocalEnvironment;
import com.opera.core.systems.testing.NoDriver;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.drivers.TestOperaDriver;

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
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_DEFAULT_PORT;
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_DEFAULT_PORT_IDENTIFIER;
import static com.opera.core.systems.scope.internal.OperaDefaults.SERVER_RANDOM_PORT_IDENTIFIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.Platform.WINDOWS;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
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
    settings.setBinary(new File(OperaPaths.operaPath()));
    TestOperaDriver driver = new TestOperaDriver(settings);

    assertNotNull(driver);
    assertTrue("Expected Opera to run", driver.isRunning());

    driver.quit();
  }

  @Test(expected = WebDriverException.class)
  public void binaryInvalidThrowsException() {
    settings.setBinary(resources.fakeFile());
    new TestOperaDriver(settings);
  }

  @Test(expected = WebDriverException.class)
  public void autostartIsRespected() {
    settings.autostart(false);
    new TestOperaDriver(settings);
  }

  @Test
  public void launcherIsUsedWhenSet() throws IOException {
    tmp.create();
    File newLauncher = tmp.newFile("newLauncher");
    Files.copy(OperaLauncherRunner.launcherDefaultLocation(), newLauncher);
    if (!Platform.getCurrent().is(WINDOWS)) {
      newLauncher.setExecutable(true);
    }

    settings.setLauncher(newLauncher);
    TestOperaDriver driver = new TestOperaDriver(settings);

    assertNotNull(driver);
    assertEquals(newLauncher, driver.getSettings().getLauncher());

    driver.quit();
  }

  @Test
  public void loggingFileReceivesOutput() throws IOException {
    tmp.create();
    File log = tmp.newFile("operadriver.log");

    settings.logging().setFile(log);
    settings.logging().setLevel(Level.FINER);

    TestOperaDriver driver = new TestOperaDriver(settings);
    driver.quit();

    assertNotSame(0, log.length());
    assertTrue(log.length() > 0);
  }

  @Test
  @Ignore(products = CORE, value = "core does not support -pd")
  public void profileIsRespected() throws IOException {
    tmp.create();
    File profile = tmp.newFolder();
    settings.setProfile(profile.getPath());
    TestOperaDriver driver = new TestOperaDriver(settings);

    assertNotNull(driver);
    assertEquals(profile, driver.preferences().get("User Prefs", "Opera Directory").getValue());
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
    TestOperaDriver driver = new TestOperaDriver(settings);

    assertNotNull(driver);
    assertEquals(host, driver.getSettings().getHost());
    assertEquals(host, driver.preferences().get("Developer Tools", "Proxy Host").getValue());

    driver.quit();
  }

  @Test
  public void portCanBeSet() {
    settings.setPort(1234);
    TestOperaDriver driver = new TestOperaDriver(settings);
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
  @Ignore(products = CORE, value = "core does not reset port number if -debugproxy is omitted")
  public void settingPort() {
    settings.setPort(-1);
    TestOperaDriver driver = new TestOperaDriver(settings);
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
    File binary = new File(OperaPaths.findOperaInstallationPath());
    environment.set(OperaPaths.OPERA_PATH_ENV_VAR, binary.getPath());

    TestOperaDriver driver = new TestOperaDriver();

    assertEquals(binary.getCanonicalPath(), driver.getSettings().getBinary().getCanonicalPath());
    assertEquals(binary.getCanonicalPath(), driver.utils().getBinaryPath());

    driver.quit();
  }

  @Test
  @NeedsLocalEnvironment
  public void detachBrowser() {
    OperaSettings settings = new OperaSettings();
    settings.setDetach(true);

    TestOperaDriver driver = new TestOperaDriver(settings);
    int processID = driver.utils().getPID();
    driver.quit();

    // Driver should be shut down, and there should be no connection to browser
    assertFalse(driver.getServices().isConnected());

    // But browser should be running
    assertTrue(ProcessManager.isPidRunning(processID));

    ProcessManager.killPID(processID);
  }

  private void assertDriverCreated(OperaSettings settings) {
    TestOperaDriver driver = new TestOperaDriver(settings);
    assertNotNull(driver);
    driver.quit();
  }

}