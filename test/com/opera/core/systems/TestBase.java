package com.opera.core.systems;

import java.io.File;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.settings.OperaDriverSettings;

abstract public class TestBase {
  protected static TestOperaDriver driver;
  protected static OperaDriverSettings settings;

  protected static String binary = "/usr/bin/opera";
  protected static String launcher = "/home/stuart/bin/opera-launcher";

  private static String fixture_dir;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    driver = new TestOperaDriver();
    Assert.assertNotNull(driver);

    // Setup the fixture directory
    String separator = System.getProperty("file.separator");
    fixture_dir = System.getProperty("user.dir");
    fixture_dir = fixture_dir + separator + "test" + separator + "fixtures"
        + separator;

    Assert.assertTrue(new File(fixture_dir).isDirectory());
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    driver.shutdown();
  }

  // Easy access to fixtures

  // / Get the URL of the given fixture file
  protected String fixture(String file) {
    return "file://localhost"+fixture_dir + file;
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
    super(settings);
  }

  public OperaRunner getRunner() {
    return operaRunner;
  }

  public OperaDriverSettings getSettings() {
    return settings;
  }
}
