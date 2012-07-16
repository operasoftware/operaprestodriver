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
import com.opera.core.systems.testing.OperaDriverTestCase;

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
  public void navigateToThreePages() {
    driver.navigate().to(pages.javascript);
    driver.navigate().to(pages.test);
    driver.navigate().to(pages.keys);
  }

  @After
  public void resetPageLoadTimeout() {
    // Make sure page load timeout is reset after each test, if one happens to fail
    driver.manage().timeouts().pageLoadTimeout(OperaIntervals.PAGE_LOAD_TIMEOUT.getMs(),
                                               TimeUnit.MILLISECONDS);
  }

  @Test
  public void back() {
    driver.navigate().back();
    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void forward() {
    back();
    driver.navigate().forward();
    assertTrue(driver.getCurrentUrl().endsWith("keys.html"));
  }

  @Test
  public void backTwice() {
    driver.navigate().back();
    driver.navigate().back();
    assertTrue(driver.getCurrentUrl().endsWith("javascript.html"));
  }

  @Test
  public void forwardTwice() {
    backTwice();
    driver.navigate().forward();
    driver.navigate().forward();
    assertTrue(driver.getCurrentUrl().endsWith("keys.html"));
  }

  @Test
  public void httpRedirect() {
    final String fetchedUrl = server.whereIs("redirect");
    driver.navigate().to(fetchedUrl);

    // Wait for redirect
    Wait<WebDriver> wait = new WebDriverWait(driver, OperaIntervals.PAGE_LOAD_TIMEOUT.getMs());
    wait.until(new ExpectedCondition<Object>() {
      public Boolean apply(WebDriver driver) {
        return !driver.getCurrentUrl().equals(fetchedUrl);
      }
    });

    assertEquals(pages.result, driver.getCurrentUrl());
  }

  // If this fails, there's a high likelihood that idle is enabled
  @Test
  public void handBackControlAfterPageLoadTimeout() {
    driver.manage().timeouts().pageLoadTimeout(100, TimeUnit.MILLISECONDS);
    driver.navigate().to(pages.ecmascriptTimeout);
    assertEquals("Waiting", driver.findElementById("out").getText());
    sleep(1000);
    assertEquals("done", driver.findElementById("out").getText());
  }

  // If this fails, there's a high likelihood that idle is enabled
  @Test
  public void handBackControlAfterPageLoadTimeoutByOverload() {
    driver.get(pages.ecmascriptTimeout, 100);
    assertEquals("Waiting", driver.findElementById("out").getText());
    sleep(1000);
    assertEquals("done", driver.findElementById("out").getText());
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      // fall through
    }
  }

}