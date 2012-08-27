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
import com.opera.core.systems.testing.drivers.TestDriver;
import com.opera.core.systems.testing.drivers.TestDriverBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperaDriverTest extends OperaDriverTestCase {

  @Rule
  public TemporaryFolder temporaryProfile;

  @Before
  public void beforeEach() {
    temporaryProfile = new TemporaryFolder();
  }

  @Test
  public void testWithoutSettingsObject() {
    Assert.assertNotNull(driver);
  }

  @Test
  public void testDefaultWindowCount() {
    assertTrue(driver.getWindowHandles().size() >= 1);
  }

  @Test
  public void testGetTitle() {
    driver.navigate().to(pages.test);
    assertEquals("Generic fixture", driver.getTitle());
  }

  @Test
  public void testGetText() {
    driver.navigate().to(pages.none);
    assertEquals("You should see nothing below.",
                 driver.findElement(By.tagName("body")).getText().trim());
  }

  @Test
  @Ignore(products = MOBILE, value = "Needs investigation")
  public void testOperaAction() {
    driver.navigate().to(pages.javascript);
    driver.navigate().to(pages.test);
    driver.navigate().to(pages.keys);

    driver.operaAction("Back");
    assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  @Test
  @Ignore(products = MOBILE, value = "Needs investigation")
  public void testOperaActionCaseInsensitive() {
    driver.navigate().to(pages.javascript);
    driver.navigate().to(pages.test);
    driver.navigate().to(pages.keys);

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
  public void testDefaultPort() {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaSettings.Capability.PORT.getCapability(), -1);

    TestDriver a = new TestDriverBuilder().using(c).get();
    assertEquals(7001, a.preferences().get("Developer Tools", "Proxy Port").getValue());
    a.quit();
  }

  @Test
  public void testRandomPort() {
    DesiredCapabilities c = new DesiredCapabilities();
    c.setCapability(OperaSettings.Capability.PORT.getCapability(), 0);

    TestDriver a;
    try {
      a = new TestDriverBuilder().using(c).get();
    } catch (Exception e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        fail("Unexpected exception: " + e);
        return;
      }
    }
    Assert.assertNotSame("7001", a.preferences().get("Developer Tools", "Proxy Port").toString());
    a.quit();
  }

  @Test
  @Ignore(products = CORE,
          value = "Once this is set the autotestmode profile no longer connects on 7001, breaking future tests")
  public void testSetPort() {
    OperaSettings settings = new OperaSettings();
    settings.setPort(9876);

    TestDriver a;
    try {
      a = new TestDriverBuilder().using(settings).get();
    } catch (Exception e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        fail("Unexpected exception: " + e);
        return;
      }
    }

    assertEquals(9876, a.preferences().get("Developer Tools", "Proxy Port").getValue());
    a.quit();
  }

}