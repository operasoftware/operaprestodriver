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

import com.opera.core.systems.scope.internal.OperaIntervals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NavigationTest extends OperaDriverTestCase {

  @Before
  public void beforeEach() {
    getFixture("javascript.html");
    getFixture("test.html");
    getFixture("keys.html");
  }

  @After
  public void afterEach() {
    // Make sure page load timeout is reset after each test, if one happens to fail
    driver.manage().timeouts().pageLoadTimeout(OperaIntervals.PAGE_LOAD_TIMEOUT.getValue(),
                                               TimeUnit.MILLISECONDS);

  }

  @Test
  public void testBack() {
    driver.navigate().back();
    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void testForward() {
    testBack();
    driver.navigate().forward();
    assertTrue(driver.getCurrentUrl().endsWith("keys.html"));
  }

  @Test
  public void testBack2() {
    driver.navigate().back();
    driver.navigate().back();
    assertTrue(driver.getCurrentUrl().endsWith("javascript.html"));
  }

  @Test
  public void testForward2() {
    testBack2();
    driver.navigate().forward();
    driver.navigate().forward();
    assertTrue(driver.getCurrentUrl().endsWith("keys.html"));
  }

  @Test
  // TODO(andreastt): Should be made local
  public void testHttpRedirect() {
    final String fetchedUrl = "http://t/core/bts/javascript/CORE-26410/003-2.php";
    driver.navigate().to(fetchedUrl);

    // Wait for redirect
    Wait<WebDriver> wait = new WebDriverWait(driver, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
    wait.until(new ExpectedCondition<Object>() {
      public Boolean apply(WebDriver driver) {
        return !driver.getCurrentUrl().equals(fetchedUrl);
      }
    });

    assertEquals("http://t/core/bts/javascript/CORE-26410/001-3.php", driver.getCurrentUrl());
  }

  @Test
  public void testHandBackControlAfterPageLoadTimeout() {
    driver.manage().timeouts().pageLoadTimeout(100, TimeUnit.MILLISECONDS);
    getFixture("idle/ecmascript-timeout.html");
    assertEquals("Waiting", driver.findElementById("out").getText());
    sleep(1000);
    assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testHandBackControlAfterPageLoadTimeoutByOverload() {
    driver.get(fixture("idle/ecmascript-timeout.html"), 100);
    assertEquals("Waiting", driver.findElementById("out").getText());
    sleep(1000);
    assertEquals("done", driver.findElementById("out").getText());
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (Exception e) {
      // do nothing
    }
  }

}