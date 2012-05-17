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

import com.opera.core.systems.runner.OperaRunner;

import com.opera.core.systems.scope.protos.SystemInputProtos;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

/**
 * You can extend OperaDriverTestCase in your test case to gain access to convenience methods
 * related to finding the current product used, auto-starting Opera before the test, quitting Opera
 * after the test, and gaining access to the fixtures directory.
 *
 * It also holds an extension of {@link OperaDriver}, called {@link TestOperaDriver}, that exposes
 * the {@link OperaRunner} and a method for determining whether the constructor and
 * {@link OperaDriver#quit()} methods has been called, {@link TestOperaDriver#isRunning()}.
 */
@RunWith(OperaDriverTestRunner.class)
public abstract class OperaDesktopDriverTestCase {

  protected static TestOperaDesktopDriver driver = null;
  protected static OperaProduct currentProduct = OperaProduct.DESKTOP;
  protected static Platform currentPlatform = Platform.getCurrent();
  protected static TemporaryFolder tmpFolder = new TemporaryFolder();
  protected static File profileTmpFolder = null;
  protected static List<SystemInputProtos.ModifierPressed> ctrlModifier = new ArrayList<SystemInputProtos.ModifierPressed>();

  private static String fixtureDirectory;

  @BeforeClass
  public static void beforeAll() throws Exception {
    ctrlModifier.add(SystemInputProtos.ModifierPressed.CTRL);

    tmpFolder.create();

    initProduct();
    initFixtures();
  }

  @AfterClass
  public static void afterAll() throws Exception {
    if (driver.isRunning()) {
      driver.quit();
    }

    tmpFolder.delete();
  }

  public String setOperaPath() {
    String binaryPath = OperaPaths.findOperaInstallationPath();
    assertNotNull("Could not find an Opera installation on this machine.", binaryPath);
    setEnvVariable("OPERA_PATH", binaryPath);
    return binaryPath;
  }

  public void killPid(int pidToKill) throws IOException, InterruptedException {
    Platform currentPlatform = Platform.getCurrent();
    String commandLine = null;
    switch (currentPlatform)
    {
      case WINDOWS:
      case XP:
      case VISTA:
        commandLine = String.format("taskkill /PID %d", pidToKill);
        break;
      case LINUX:
      case UNIX:
      case MAC:
        commandLine = String.format("kill %d", pidToKill);
        break;
      default:
        break;
    }

    Runtime runTime = Runtime.getRuntime();
    Process process = runTime.exec(commandLine);
    process.waitFor();
  }

  public boolean isPidRunning(int pidToCheck) throws IOException, InterruptedException
  {
    Platform currentPlatform = Platform.getCurrent();
    String commandLine = null;
    switch (currentPlatform)
    {
      case WINDOWS:
      case XP:
      case VISTA:
        commandLine = String.format("tasklist /FI \"PID eq %d\"", pidToCheck);
        break;
      case LINUX:
      case UNIX:
      case MAC:
        commandLine = String.format("ps -p %d", pidToCheck);
        break;
      default:
        break;
    }

    Runtime runTime = Runtime.getRuntime();
    Process process = runTime.exec(commandLine);
    process.waitFor();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    // Assume that the if the PID shows in the output, then the process is running, this is true for
    // both the tasklist command on Windows 7 and the ps command on Mac
    String line = bufferedReader.readLine();
    while (line != null)
    {
      String regExp = String.format(".*%d.*", pidToCheck);
      if (line.matches(regExp))
        return true;

      line = bufferedReader.readLine();
    }
    return false;
  }

  public void assertMenuItemListByFullString(List<QuickMenuItem> items)
  {
    // No assert for items.size() as it is an error rather than a failure.
    QuickMenuItem referenceItem = items.get(0);
    Integer i = 0;

    for (QuickMenuItem item: items)
    {
      String assertMessage = String.format("Menu item is NULL [%d]", i++);
      assertNotNull(assertMessage, item);

      if (item == referenceItem)
        continue;

      assertEquals(referenceItem.toFullString(), item.toFullString());
    }
  }

  public void checkCaps(Capabilities currentCapabilities, Map<String, String> expectedValuesMap)
  {
    assertNotNull("The driver capabilities object is null.", currentCapabilities);

    for (Map.Entry<String, String> expectedValueEntry : expectedValuesMap.entrySet())
    {
      String expectedKey = expectedValueEntry.getKey();
      String expectedValue = expectedValueEntry.getValue();
      assertNotNull(expectedKey);
      assertNotNull(expectedValue);
      String assertMessage;
      if (expectedValue.equals("(null)"))
      {
        assertMessage =  String.format("The '%s' capability was expected to BE null.", expectedKey);
        assertNull(assertMessage, currentCapabilities.getCapability(expectedKey));
      }
      else
      {
        assertMessage =  String.format("The '%s' capability was expected to be NOT null.", expectedKey);
        assertNotNull(assertMessage, currentCapabilities.getCapability(expectedKey));
        String capabilityValue = currentCapabilities.getCapability(expectedKey).toString();
        assertMessage = String.format("The '%s' capability was expected to be '%s' but is '%s'.", expectedKey, expectedValue, capabilityValue);
        assertTrue(assertMessage, expectedValue.equals(capabilityValue));
      }
    }
  }

  /*
  This is how the capabilities are set by desktopwatir.
  The default values from below are modified accordingly in the tests cases.
  */
  public DesiredCapabilities getDefaultCaps()
  {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability("opera.guess_binary_path", false);
    c.setCapability("opera.no_quit", false);
    c.setCapability("opera.no_restart", false);

/*
    c.setCapability('opera.binary', self.settings[:path]) if self.settings[:path]
    c.setCapability('opera.arguments', self.settings[:args].to_s) if self.settings[:args]

    c.setCapability('opera.profile', self.settings[:profile]) if self.settings[:profile]
    c.setCapability('opera.idle', true) if self.settings[:opera_idle] or ENV['OPERA_IDLE'].truthy?
*/
    return c;
  }

  protected void setEnvVariable(String varName, String varValue)
  {
    Map<String, String> env = new HashMap<String, String>();
    env.put(varName, varValue);
    setEnv(env);
  }

  protected void setEnv(Map<String, String> newenv)
  {
    try
    {
      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
      Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
      theEnvironmentField.setAccessible(true);
      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
      env.putAll(newenv);
      Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
      theCaseInsensitiveEnvironmentField.setAccessible(true);
      Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
      cienv.putAll(newenv);
    }
    catch (NoSuchFieldException e)
    {
      try {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for(Class cl : classes) {
          if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Object obj = field.get(env);
            Map<String, String> map = (Map<String, String>) obj;
            map.clear();
            map.putAll(newenv);
          }
        }
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  /**
   * Fetch the product from the connected driver or override it using the OPERA_PRODUCT
   * environmental variable.
   */
  protected static void initProduct() {
/*
    if (driver == null || !driver.isRunning()) {
      return;
    }

    String requestedProduct = System.getenv("OPERA_PRODUCT");
    if (requestedProduct == null || requestedProduct.isEmpty()) {
      requestedProduct = driver.getCapabilities().getCapability(OperaDriver.PRODUCT).toString();
    }

    if (requestedProduct != null && !requestedProduct.isEmpty()) {
      try {
        currentProduct = OperaProduct.valueOf(requestedProduct);
      } catch (IllegalArgumentException e) {
        // product not found, defaulting to CORE
      }
    }
*/
  }

  /**
   * Setup the fixture directory.
   */
  protected static void initFixtures() {
    String separator = System.getProperty("file.separator");
    fixtureDirectory = System.getProperty("user.dir");
    fixtureDirectory = separator + fixtureDirectory + separator + separator + "test" +
                       separator + "fixtures" + separator;

    assertTrue(new File(fixtureDirectory).isDirectory());
  }

  /**
   * Get the URL of the given fixture file.
   *
   * @param file the filename to get
   * @return the URL to the fixture file
   */
  protected String fixture(String file) {
    return "file://localhost" + fixtureDirectory + file;
  }

  /**
   * Navigate to the given fixture file.
   *
   * @param file the filename from the fixture directory to navigate to
   */
  protected void getFixture(String file) {
    driver.get(fixture(file));
  }

  private static boolean isDriverRunning() {
    return (driver != null) && driver.isRunning();
  }

}

/**
 * Provides access to the {@link OperaRunner}, so we can detect crashes.
 */
class TestOperaDesktopDriver extends OperaDesktopDriver {

  private boolean isRunning = false;

  public TestOperaDesktopDriver() {
    super();
  }

  public TestOperaDesktopDriver(Capabilities capabilities) {
    super(capabilities);
    isRunning = true;
  }

  public OperaRunner getRunner() {
    return runner;
  }

  public Capabilities getCapabilities() {
    return capabilities;
  }

  public void quit() {
    super.quit();
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

}