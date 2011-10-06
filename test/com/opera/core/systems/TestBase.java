package com.opera.core.systems;

import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.settings.OperaDriverSettings;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static org.junit.Assert.assertTrue;

abstract public class TestBase {

  protected static TestOperaDriver driver;

  private static String fixtureDirectory;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability(OperaDriver.LOGGING_LEVEL, "FINE");
    driver = new TestOperaDriver(caps);
    Assert.assertNotNull(driver);

    initFixtures();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    driver.quit();
  }

  // Easy access to fixtures

  /**
   * Setup the fixture directory
   */
  protected static void initFixtures() {
    String separator = System.getProperty("file.separator");
    fixtureDirectory = System.getProperty("user.dir");
    fixtureDirectory = separator + fixtureDirectory + separator + separator + "test" +
                       separator + "fixtures" + separator;

    assertTrue(new File(fixtureDirectory).isDirectory());
  }

  // / Get the URL of the given fixture file
  protected String fixture(String file) {
    return "file://localhost" + fixtureDirectory + file;
  }

  // / Navigate to the given fixture file
  protected void getFixture(String file) {
    driver.get(fixture(file));
  }
}

// Provides access to the Opera Runner, so we can detect crashes
class TestOperaDriver extends OperaDriver {

  public TestOperaDriver() {
    super();
  }

  public TestOperaDriver(OperaDriverSettings settings) {
    this(settings.getCapabilities());
  }

  public TestOperaDriver(Capabilities capabilities) {
    super(capabilities);
  }

  public OperaRunner getRunner() {
    return operaRunner;
  }

}
