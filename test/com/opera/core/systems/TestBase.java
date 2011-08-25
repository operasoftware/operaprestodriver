package com.opera.core.systems;

import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.settings.OperaDriverSettings;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

abstract public class TestBase {
  protected static TestOperaDriver driver;

  private static String fixture_dir;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    driver = new TestOperaDriver();
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
    fixture_dir = System.getProperty("user.dir");
    fixture_dir = separator + fixture_dir + separator + separator + "test" +
      separator + "fixtures" + separator;

    Assert.assertTrue(new File(fixture_dir).isDirectory());
  }

  // / Get the URL of the given fixture file
  protected String fixture(String file) {
    return "file://localhost" + fixture_dir + file;
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

  public TestOperaDriver(DesiredCapabilities capabilities) {
    super(capabilities);
  }

  public OperaRunner getRunner() {
    return operaRunner;
  }
}
