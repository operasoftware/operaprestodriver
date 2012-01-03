/*
Copyright 2011-2012 Opera Software ASA

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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static com.opera.core.systems.OperaProduct.CORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OperaDriverTest extends OperaDriverTestCase {

  // Replace the TestBase setup and teardown so that we don't launch Opera
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    initFixtures();
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
  public void testDefaultWindowCount() {
    driver = new TestOperaDriver();
    assertTrue(driver.getWindowCount() >= 1);
  }

  @Test
  public void testGetTitle() {
    driver.get("http://t/core/standards/dom0/link/pathname/002.html");
    assertEquals("URL with explicit pathname and hash", driver.getTitle());
  }

  @Test
  public void testGetText() {
    driver.get("http://t/core/standards/quotes/none.html");
    assertEquals("you should see nothing below",
                 driver.findElementByTagName("body").getText().trim());
  }

  @Test
  public void testGetURL() {
    driver.get("www.ebay.co.uk");
    assertTrue(driver.getCurrentUrl().indexOf("www.ebay.co.uk") > 0);
  }

  @Test
  public void testGetURL2() {
    driver.get("www.nyt.com", 15000);
    assertTrue(driver.getCurrentUrl().indexOf("www.nytimes.com") > 0);
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
    assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  @Test
  public void testOperaActionCaseInsensitive() throws Exception {
    getFixture("javascript.html");
    getFixture("test.html");
    getFixture("keys.html");

    driver.operaAction("bACk");
    assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  @Test
  public void testOperaDriverShutdown() {
    // leave with a fast loading page
    driver.get("about:blank");
    driver.quit();
  }

  @Test
  @Ignore(products = CORE, value = "core does not reset port number if -debugproxy is ommitted")
  public void testDefaultPort() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PORT, -1);

    OperaDriver a = new OperaDriver(c);
    assertEquals("7001", a.preferences().get("Developer Tools", "Proxy Port").toString());
    a.quit();
  }

  @Test
  public void testRandomPort() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PORT, 0);

    OperaDriver a;
    try {
      a = new OperaDriver(c);
    } catch (Exception e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        throw e;
      }
    }
    Assert.assertNotSame("7001", a.preferences().get("Developer Tools", "Proxy Port").toString());
    a.quit();
  }

  @Test
  @Ignore(products = CORE,
          value = "Once this is set the autotestmode profile no longer connects on 7001, breaking future tests")
  public void testSetPort() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PORT, 9876);

    OperaDriver a;
    try {
      a = new OperaDriver(c);
    } catch (Exception e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        throw e;
      }
    }
    assertEquals("9876", a.preferences().get("Developer Tools", "Proxy Port").toString());
    a.quit();
  }

  @Test
  public void testDefaultProfile() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PROFILE, "");

    OperaDriver a = new OperaDriver(c);
    String profile = a.preferences().get("User Prefs", "Opera Directory").toString();
    String
        defaultProfile =
        a.preferences().get("User Prefs", "Opera Directory").getDefaultValue().toString();
    assertTrue("'" + profile + "' contains '" + defaultProfile + "'",
               profile.contains(defaultProfile)
    );
    a.quit();
  }

  @Test
  @Ignore(products = CORE, value = "core does not support -pd")
  public void testSetProfile() throws Exception {
    if (Platform.getCurrent() != Platform.LINUX) {
      return;
    }

    FileHandler.delete(new File("/tmp/opera-test-profile/"));

    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PROFILE, "/tmp/opera-test-profile/");

    OperaDriver a;
    try {
      a = new OperaDriver(c);
    } catch (Exception e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        throw e;
      }
    }
    String profile = a.preferences().get("User Prefs", "Opera Directory").toString();
    assertEquals("/tmp/opera-test-profile/", profile);
    a.quit();
  }

  @Test
  @Ignore(products = CORE, value = "core does not support -pd")
  public void testRandomProfile() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PROFILE, (String) null);

    OperaDriver a;
    try {
      a = new OperaDriver(c);
    } catch (Exception e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        throw e;
      }
    }
    String profile = a.preferences().get("User Prefs", "Opera Directory").toString();
    assertTrue("'" + profile + "' (case insensitively) should contain 'tmp' or 'temp'",
               profile.toLowerCase().contains("tmp") || profile.toLowerCase().contains("temp"));
    a.quit();
  }

  @Test
  @Ignore // TODO(andreastt): No good reason why we're ignoring this, investigate
  public void testProfileDeleted() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PROFILE, (String) null);

    OperaDriver a;
    try {
      a = new OperaDriver(c);
    } catch (Exception e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        throw e;
      }
    }
    String profile = a.preferences().get("User Prefs", "Opera Directory").toString();
    assertTrue("Temporary directory exists", (new File(profile)).exists());
    a.quit();
    assertFalse("Temporary directory does not exist after quit",
                (new File(profile)).exists()
    );
    a.quit();
  }

  @Test
  public void testMultipleOperas() throws Exception {
    DesiredCapabilities cap = new DesiredCapabilities();
    cap.setCapability(OperaDriver.PROFILE, (String) null);
    cap.setCapability(OperaDriver.PORT, 0);

    OperaDriver a;
    OperaDriver b;
    OperaDriver c;
    try {
      a = new OperaDriver(cap);
      b = new OperaDriver(cap);
      c = new OperaDriver(cap);
    } catch (WebDriverException e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        throw e;
      }
    }

    a.get(fixture("test.html"));
    b.get(fixture("javascript.html"));
    c.get(fixture("keys.html"));

    assertTrue("Instance a has url test.html", a.getCurrentUrl().endsWith("test.html"));
    assertTrue("Instance a has url javascript.html", b.getCurrentUrl().endsWith("javascript.html"));
    assertTrue("Instance a has url keys.html", c.getCurrentUrl().endsWith("keys.html"));

    a.quit();
    b.quit();
    c.quit();
  }

}