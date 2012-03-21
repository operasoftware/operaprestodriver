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

import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.drivers.TestOperaDriver;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import static com.opera.core.systems.OperaProduct.CORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperaDriverTest extends OperaDriverTestCase {

  @Rule
  public TemporaryFolder temporaryProfile = new TemporaryFolder();

  @Test
  public void testWithoutSettingsObject() {
    Assert.assertNotNull(driver);
  }

  @Test
  public void testNullOperaArguments() throws Exception {
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability(OperaSettings.Capability.ARGUMENTS.getCapability(), (String) null);

    TestOperaDriver a = new TestOperaDriver(caps);
    Assert.assertNotNull(driver);
    a.quit();
  }

  @Test
  public void testDefaultWindowCount() {
    assertTrue(driver.getWindowCount() >= 1);
  }

  @Test
  // TODO(andreastt): Should be made local
  public void testGetTitle() {
    getFixture("test.html");
    assertEquals("Generic fixture", driver.getTitle());
  }

  @Test
  public void testGetText() {
    getFixture("none.html");
    assertEquals("You should see nothing below.",
                 driver.findElementByTagName("body").getText().trim());
  }

  @Test
  public void testGetVersion() {
    String version = driver.getVersion();
    Assert.assertNotNull(version);
    Assert.assertNotSame("(Unknown)", version);
  }

  @Test
  public void testOperaAction() {
    getFixture("javascript.html");
    getFixture("test.html");
    getFixture("keys.html");

    driver.operaAction("Back");
    assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  @Test
  public void testOperaActionCaseInsensitive() {
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
  }

  @Test
  @Ignore(products = CORE, value = "core does not reset port number if -debugproxy is omitted")
  public void testDefaultPort() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaSettings.Capability.PORT.getCapability(), -1);

    TestOperaDriver a = new TestOperaDriver(c);
    assertEquals(7001, a.preferences().get("Developer Tools", "Proxy Port").getValue());
    a.quit();
  }

  @Test
  public void testRandomPort() throws Exception {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaSettings.Capability.PORT.getCapability(), 0);

    TestOperaDriver a;
    try {
      a = new TestOperaDriver(c);
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
    OperaSettings settings = new OperaSettings();
    settings.setPort(9876);

    TestOperaDriver a;
    try {
      a = new TestOperaDriver(settings);
    } catch (Exception e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        throw e;
      }
    }

    assertEquals(9876, a.preferences().get("Developer Tools", "Proxy Port").getValue());
    a.quit();
  }

  @Test
  public void testMultipleOperas() throws Exception {
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setCapability(OperaSettings.Capability.PROFILE.getCapability(), (String) null);  // random profile
    capabilities.setCapability(OperaSettings.Capability.PORT.getCapability(), 0);  // random port

    OperaDriver a;
    OperaDriver b;
    OperaDriver c;
    try {
      a = new OperaDriver(capabilities);
      b = new OperaDriver(capabilities);
      c = new OperaDriver(capabilities);
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