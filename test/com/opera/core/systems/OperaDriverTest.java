package com.opera.core.systems;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaDriverTest extends TestBase {
  // Replace the TestBase setup and teardown so that we don't launch opera
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testWithoutSettingsObject() {
    driver = new TestOperaDriver();

    Assert.assertNotNull(driver);

    driver.quit();
  }

  @Test
  public void testNullOperaArguments() throws Exception {
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability(OperaDriver.ARGUMENTS, (String) null);

    OperaDriver driver = new TestOperaDriver(caps);
    Assert.assertNotNull(driver);
    driver.quit();
  }

  @Test
  public void testNullOperaArgumentsSettings() throws Exception {
    OperaDriverSettings settings = new OperaDriverSettings();
    settings.setOperaBinaryArguments(null);

    OperaDriver driver = new TestOperaDriver(settings);
    Assert.assertNotNull(driver);
    driver.quit();
  }

  @Test
  public void testDefaultWindowCount() {
    driver = new TestOperaDriver();
    Assert.assertTrue(driver.getWindowCount() >= 1);
  }

  @Test
  public void testGetWindowHandle() {
    driver.get("http://t/core/standards/dom0/link/pathname/002.html");
    Assert.assertEquals("URL with explicit pathname and hash", driver.getWindowHandle());
  }

  @Test
  public void testGetTitle() {
    Assert.assertEquals("URL with explicit pathname and hash", driver.getTitle());
  }

  @Test
  public void testGetText() {
    driver.get("http://t/core/standards/quotes/none.html");
    Assert.assertEquals("you should see nothing below", driver.findElementByTagName("body").getText().trim());
  }

  @Test
  public void testGetURL() {
    driver.get("www.ebay.co.uk");
    Assert.assertTrue(driver.getCurrentUrl().indexOf("www.ebay.co.uk") > 0);
  }

  @Test
  public void testGetURL2() {
    driver.get("www.nyt.com", 15000);
    Assert.assertTrue(driver.getCurrentUrl().indexOf("www.nytimes.com") > 0);
  }

  @Test
  public void testGetVersion() throws Exception {
    String version = driver.getVersion();
    Assert.assertNotNull(version);
    Assert.assertNotSame("(Unknown)", version);
  }

  @Test
  public void testOperaAction() throws Exception {
    getFixture("javascript.html");
    getFixture("test.html");
    getFixture("keys.html");

    driver.operaAction("Back");
    Assert.assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  @Test
  public void testOperaActionCaseInsensitive() throws Exception {
    getFixture("javascript.html");
    getFixture("test.html");
    getFixture("keys.html");

    driver.operaAction("bACk");
    Assert.assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  @Test
  public void testOperaDriverShutdown() {
    // leave with a fast loading page
    driver.get("about:blank");
    driver.quit();
  }
}
