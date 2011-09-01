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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaDriverTest extends TestBase {

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
    Assert.assertEquals("you should see nothing below",
                        driver.findElementByTagName("body").getText().trim());
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

  @Test
  public void testDefaultPort() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PORT, -1);

    OperaDriver a = new OperaDriver(c);
    Assert.assertEquals("7001", a.getPref("Developer Tools", "Proxy Port"));
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
    Assert.assertNotSame("7001", a.getPref("Developer Tools", "Proxy Port"));
    a.quit();
  }

  @Test
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
    Assert.assertEquals("9876", a.getPref("Developer Tools", "Proxy Port"));
    a.quit();
  }

  @Test
  public void testDefaultProfile() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaDriver.PROFILE, "");

    OperaDriver a = new OperaDriver(c);
    String profile = a.getPref("User Prefs", "Opera Directory");
    String defaultProfile = a.getDefaultPref("User Prefs", "Opera Directory");
    Assert.assertTrue("'"+profile+"' contains '"+defaultProfile+"'" ,
        profile.contains(defaultProfile)
    );
    a.quit();
  }

  @Test
  public void testSetProfile() throws Exception {
    if (Platform.getCurrent() != Platform.LINUX) return;

    FileUtils.deleteDirectory(new File("/tmp/opera-test-profile/"));

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
    String profile = a.getPref("User Prefs", "Opera Directory");
    Assert.assertEquals("/tmp/opera-test-profile/", profile);
    a.quit();
  }

  @Test
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
    String profile = a.getPref("User Prefs", "Opera Directory");
    Assert.assertTrue("'"+profile+"' contains 'tmp' or 'temp'" ,
        profile.contains("tmp") || profile.contains("temp")
    );
    a.quit();
  }

  @Ignore
  @Test
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
    String profile = a.getPref("User Prefs", "Opera Directory");
    Assert.assertTrue("Temporary directory exists", (new File(profile)).exists());
    a.quit();
    Assert.assertFalse("Temporary directory does not exist after quit",
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

    Assert.assertEquals(fixture("test.html"), a.getCurrentUrl());
    Assert.assertEquals(fixture("javascript.html"), b.getCurrentUrl());
    Assert.assertEquals(fixture("keys.html"), c.getCurrentUrl());

    a.quit();
    b.quit();
    c.quit();
  }
}
