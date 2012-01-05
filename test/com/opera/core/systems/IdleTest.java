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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.DesiredCapabilities;

import static com.opera.core.systems.OperaProduct.DESKTOP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IdleTest extends OperaDriverTestCase {

  // Timeout vars for every test
  private static long start, end;

  // Make sure we're actually using idle, and not hitting the timeout
  private static long timeout = OperaIntervals.OPERA_IDLE_TIMEOUT.getValue();

  // Make sure these tests only run if idle is available
  @Rule
  public MethodRule random = new MethodRule() {
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
      // If idle available return the test
      if (driver.isOperaIdleAvailable()) {
        return base;
      }
      // otherwise return an empty statement, meaning test doesn't run
      else {
        return new Statement() {
          @Override
          public void evaluate() throws Throwable {
          }
        };
      }
    }
  };

  @BeforeClass
  public static void setUpBeforeClass() {
    DesiredCapabilities capabilities = OperaDriverTestCase.getDefaultCapabilities();
    capabilities.setCapability(OperaDriver.OPERAIDLE, true);

    driver = new TestOperaDriver(capabilities);
    init();
  }

  @Before
  public void setUp() {
    reset();
  }

  @After
  public void tearDown() {
    // Make sure the test hasn't passed because we hit the page load timeout instead of using idle
    assertTrue("Took less than idle timeout", end - start < timeout);
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
  public void testGet() {
    start();
    getFixture("test.html");
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void testBack() {
    getFixture("javascript.html");

    start();
    driver.navigate().back();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void testForward() {
    start();
    driver.navigate().forward();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("javascript.html"));
  }

  @Test
  public void testBack2() {
    start();
    driver.navigate().back();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  //@Ignore
  public void testRefresh() {
    getFixture("test.html");
    driver.findElementById("input_email").sendKeys("before refresh");

    start();
    driver.navigate().refresh();
    stop();

    assertEquals("", driver.findElementById("input_email").getAttribute("value"));
  }

  @Test
  public void testClick() {
    getFixture("test.html");

    start();
    driver.findElementById("local").click();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("two_input_fields.html"));
  }

  @Test
  public void testClickXY() {
    getFixture("test.html");

    start();
    ((OperaWebElement) driver.findElementById("local")).click(3, 5);
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("two_input_fields.html"));
  }

  @Test
  @Ignore
  public void testKeyEnter() {
    getFixture("javascript.html");

    // Focus textbox
    driver.findElementById("one").click();

    // submit form
    start();
    driver.key("Enter");
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  @Test
  @Ignore(products = DESKTOP)
  public void testSendKeysNewline() {
    getFixture("javascript.html");

    // Focus textbox
    start();
    driver.findElementById("one").sendKeys("\n");
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  @Test
  public void testSetSelected() {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    start();
    driver.findElementById("check").click();
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  @Test
  public void testSubmit() {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    start();
    driver.findElementById("test_form").submit();
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  /* Begin testing OperaIdle conditions */

  @Test
  @Ignore
  //@Ignore(products = DESKTOP, value = "DSK-347592")  // TODO(andreastt): Not working because desktop returns "CORE"
  public void testEcmascriptLoop() {
    start();
    getFixture("idle/ecmascript-loop.html");
    stop();

    assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testEcmascriptTimeout() {
    start();
    getFixture("idle/ecmascript-timeout.html");
    stop();

    assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testEcmascriptTimeoutLoop() {
    start();
    getFixture("idle/ecmascript-timeout-loop.html");
    stop();

    assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testMetarefresh() {
    getFixture("idle/metarefresh.html");

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void testCustomTimeout() {
    start = System.currentTimeMillis();
    driver.get(fixture("http://nytimes.com"), 500);
    end = System.currentTimeMillis();

    // Check we hit the timeout (+ a 100ms margin)
    assertTrue("Custom timout", end - start < 500 + 100);
  }

  @Test
  public void testIdleOff() {
    driver.quit();
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("opera.idle", false);
    driver = new TestOperaDriver(caps);

    getFixture("timer.html");
    // Idle will wait for timeout before firing
    assertEquals("default", driver.findElementById("one").getAttribute("value"));
  }

}