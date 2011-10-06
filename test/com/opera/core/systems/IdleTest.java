package com.opera.core.systems;

import com.opera.core.systems.scope.internal.OperaIntervals;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IdleTest extends TestBase {
  // Timeout vars for every test
  private static long start, end;

  // Make sure we're actually using Idle, and not hitting the timeout
  private static long timeout = OperaIntervals.OPERA_IDLE_TIMEOUT.getValue();

  // Make sure these tests only run if OperaIdle is available
  @Rule
  public MethodRule random = new MethodRule() {
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
      // If Idle available return the test
      if (driver.isOperaIdleAvailable()) return base;
        // otherwise return an empty statement -> test doesn't run
      else return new Statement() {
        @Override
        public void evaluate() throws Throwable {
        }
      };
    }
  };

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("opera.idle", true);

    driver = new TestOperaDriver(caps);
    initFixtures();
  }

  ;

  @Before
  public void setUp() {
    start = end = 0;
  }

  @After
  public void tearDown() {
    // Make sure the test hasn't passed because we hit the page load
    // timeout instead of using OperaIdle
    assertTrue("Took less than Idle timeout", end - start < timeout);
  }

  private void start() {
    start = System.currentTimeMillis();
  }

  private void stop() {
    end = System.currentTimeMillis();
  }

  @Test
  public void testGet() throws Exception {
    start();
    getFixture("test.html");
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void testBack() throws Exception {
    getFixture("javascript.html");

    start();
    driver.navigate().back();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void testForward() throws Exception {
    start();
    driver.navigate().forward();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("javascript.html"));
  }

  @Test
  public void testBack2() throws Exception {
    start();
    driver.navigate().back();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  @Ignore
  public void testRefresh() throws Exception {
    getFixture("test.html");
    ((OperaWebElement) driver.findElementById("input_email")).sendKeys("before refresh");

    start();
    driver.navigate().refresh();
    stop();

    assertEquals("", driver.findElementById("input_email").getAttribute("value"));
  }

  @Test
  public void testClick() throws Exception {
    getFixture("test.html");

    start();
    driver.findElementById("local").click();
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("two_input_fields.html"));
  }

  @Test
  public void testClickXY() throws Exception {
    getFixture("test.html");

    start();
    ((OperaWebElement) driver.findElementById("local")).click(3, 5);
    stop();

    assertTrue(driver.getCurrentUrl().endsWith("two_input_fields.html"));
  }

  @Test
  @Ignore
  public void testKeyEnter() throws Exception {
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
  public void testSendKeysNewline() throws Exception {
    getFixture("javascript.html");

    // Focus textbox
    start();
    ((OperaWebElement) driver.findElementById("one")).sendKeys("\n");
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  @Test
  public void testSetSelected() throws Exception {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    start();
    ((OperaWebElement) driver.findElementById("check")).click();
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  @Test
  public void testSubmit() throws Exception {
    getFixture("javascript.html");

    // Check checkbox, fires a submit even on the form
    start();
    ((OperaWebElement) driver.findElementById("test_form")).submit();
    stop();

    // +"?" for submitted query string
    assertTrue(driver.getCurrentUrl().endsWith("test.html?"));
  }

  /* Begin testing OperaIdle conditions */

  @Test
  public void testEcmascriptLoop() throws Exception {
    start();
    getFixture("idle/ecmascript-loop.html");
    stop();

    assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testEcmascriptTimeout() throws Exception {
    start();
    getFixture("idle/ecmascript-timeout.html");
    stop();

    assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testEcmascriptTimeoutLoop() throws Exception {
    start();
    getFixture("idle/ecmascript-timeout-loop.html");
    stop();

    assertEquals("done", driver.findElementById("out").getText());
  }

  @Test
  public void testMetarefresh() throws Exception {
    getFixture("idle/metarefresh.html");

    assertTrue(driver.getCurrentUrl().endsWith("test.html"));
  }

  @Test
  public void testCustomTimeout() throws Exception {
    start = System.currentTimeMillis();
    driver.get(fixture("http://nytimes.com"), 500);
    end = System.currentTimeMillis();

    // Check we hit the timeout (+ a 100ms margin)
    assertTrue("Custom timout", end - start < 500 + 100);
  }

  @Test
  public void testIdleOff() throws Exception {
    driver.quit();
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("opera.idle", false);
    driver = new TestOperaDriver(caps);

    getFixture("timer.html");
    // Idle will wait for timeout before firing
    assertEquals("default", driver.findElementById("one").getAttribute("value"));
  }
}
