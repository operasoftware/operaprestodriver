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
import com.opera.core.systems.testing.FreshDriver;
import com.opera.core.systems.testing.IdleEnabled;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.Settings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.opera.core.systems.OperaProduct.MOBILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.openqa.selenium.Platform.LINUX;
import static org.openqa.selenium.Platform.MAC;
import static org.openqa.selenium.Platform.WINDOWS;

@IdleEnabled
@Settings(idle = true, logLevel = Settings.Level.FINE)  // idle messages are logged to FINE
public class IdleTest extends OperaDriverTestCase {

  // Timeout vars for every test
  private static long start, end;

  // Make sure we're actually using idle, and not hitting the timeout
  private static final long timeout = OperaIntervals.OPERA_IDLE_TIMEOUT.getMs();

  @Before
  public void beforeEach() {
    reset();
  }

  @After
  public void afterEach() {
    // Make sure the test hasn't passed because we hit the page load timeout instead of using idle
    assertTrue("Took less than idle timeout", end - start < timeout);
  }

  private void assertIdleEnabledAndAvailable() {
    assertTrue("Expected idle to be available and enabled",
               driver.getSettings().useIdle() && driver.getServices().isOperaIdleAvailable());
  }

  private void assertIdleDisabledButAvailable() {
    assertTrue("Expecting idle to be available, but disabled",
               !driver.getSettings().useIdle() && driver.getServices().isOperaIdleAvailable());
  }

  private void start() {
    start = System.currentTimeMillis();
  }

  private void stop() {
    end = System.currentTimeMillis();
  }

  private void reset() {
    start = end = 0;
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void get() {
    assertIdleEnabledAndAvailable();

    start();
    driver.navigate().to(pages.test);
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  @Ignore(platforms = WINDOWS, products = MOBILE)
  public void back() {
    assertIdleEnabledAndAvailable();

    driver.navigate().to(pages.test);
    driver.navigate().to(pages.javascript);

    start();
    driver.navigate().back();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  @Ignore(platforms = WINDOWS, products = MOBILE)
  public void forward() {
    assertIdleEnabledAndAvailable();

    driver.navigate().to(pages.javascript);
    driver.navigate().back();

    start();
    driver.navigate().forward();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("javascript.html"));
  }

  @Test
  @Ignore(platforms = WINDOWS, products = MOBILE)
  public void back2() {
    assertIdleEnabledAndAvailable();

    driver.navigate().to(pages.test);
    driver.navigate().to(pages.javascript);
    driver.navigate().back();
    driver.navigate().forward();

    start();
    driver.navigate().back();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void refresh() {
    assertIdleEnabledAndAvailable();

    driver.navigate().to(pages.test);
    driver.findElement(By.id("input_email")).sendKeys("before refresh");

    start();
    driver.navigate().refresh();
    stop();

    assertEquals("", driver.findElement(By.id("input_email")).getAttribute("value"));
  }

  @Test
  @Ignore(platforms = {WINDOWS, MAC})
  public void click() {
    assertIdleEnabledAndAvailable();

    driver.navigate().to(pages.test);

    start();
    driver.findElement(By.id("local")).click();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("two_input_fields.html"));
  }

  @Test
  public void keyEnter() {
    assertIdleEnabledAndAvailable();
    driver.navigate().to(pages.javascript);

    // submit form
    start();
    driver.findElement(By.id("one")).sendKeys(Keys.ENTER);
    stop();

    // +"?" for submitted query string
    assertThat(driver.getCurrentUrl(), containsString("test.html?"));
  }

  @Test
  public void sendKeysNewline() {
    assertIdleEnabledAndAvailable();

    driver.navigate().to(pages.javascript);

    // Focus text box
    start();
    driver.findElement(By.id("one")).sendKeys("\n");
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  @Test
  public void setSelected() {
    assertIdleEnabledAndAvailable();

    driver.navigate().to(pages.javascript);

    // Check checkbox, fires a submit even on the form
    start();
    driver.findElement(By.id("check")).click();
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  @Test
  public void submit() {
    assertIdleEnabledAndAvailable();

    driver.navigate().to(pages.javascript);

    // Check checkbox, fires a submit even on the form
    start();
    driver.findElement(By.id("test_form")).submit();
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  // Begin testing OperaIdle conditions

  @Test
  @Ignore(platforms = LINUX)
  public void ecmascriptLoop() {
    assertIdleEnabledAndAvailable();

    start();
    driver.navigate().to(pages.ecmascriptLoop);
    stop();

    assertEquals("done", driver.findElement(By.id("out")).getText());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void ecmascriptTimeout() {
    assertIdleEnabledAndAvailable();

    start();
    driver.navigate().to(pages.ecmascriptTimeout);
    stop();

    assertEquals("done", driver.findElement(By.id("out")).getText());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void ecmascriptTimeoutLoop() {
    assertIdleEnabledAndAvailable();

    start();
    driver.navigate().to(pages.ecmascriptTimeoutLoop);
    stop();

    assertEquals("done", driver.findElement(By.id("out")).getText());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void metaRefresh() {
    assertIdleEnabledAndAvailable();
    driver.navigate().to(pages.metaRefresh);
    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  @Ignore(value = "Should be made local")
  public void testCustomTimeout() {
    assertIdleEnabledAndAvailable();

    start = System.currentTimeMillis();
    driver.get("http://nytimes.com/", 500);
    end = System.currentTimeMillis();

    // Check we hit the timeout (+ a 100ms margin)
    assertTrue("Custom timeout", end - start < 500 + 100);
  }

  @Test
  @FreshDriver
  public void idleOff() {
    assertIdleDisabledButAvailable();

    driver.navigate().to(pages.timer);
    // Idle will wait for timeout before firing
    assertEquals("default", driver.findElement(By.id("one")).getAttribute("value"));
  }

}