/*
Copyright 2012 Opera Software ASA

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

import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.drivers.TestDriver;
import com.opera.core.systems.testing.drivers.TestDriverBuilder;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

public class SessionTest extends OperaDriverTestCase {

  public static final String BROWSER_NOT_CONNECTED_STRING = "Browser is not connected to driver";

  @Before
  public void setup() {
    driver.get(pages.javascript);
  }

  @Test
  public void callingQuitMoreThanOnceOnASessionIsANoOp() {
    driver.quit();

    try {
      driver.quit();
    } catch (RuntimeException e) {
      fail("It should be possible to quit a session more than once, got exception:" + e);
    }
  }

  @Test
  public void getTitleAfterBrowserIsQuitThrowsCommunicationException() {
    driver.quit();

    try {
      driver.getTitle();
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(CommunicationException.class)));
      assertThat(e.getMessage(), containsString(BROWSER_NOT_CONNECTED_STRING));
    }
  }

  @Test
  public void getPageAfterBrowserIsQuitThrowsCommunicationException() {
    driver.quit();

    try {
      driver.get(pages.javascript);
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(CommunicationException.class)));
      assertThat(e.getMessage(),
                 containsString("Unable to open URL because Opera is not connected"));
    }
  }

  @Test
  public void getPageSourceAfterBrowserIsQuitThrowsCommunicationException() {
    driver.quit();

    try {
      driver.getPageSource();
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(CommunicationException.class)));
      assertThat(e.getMessage(), containsString(BROWSER_NOT_CONNECTED_STRING));
    }
  }

  @Test
  public void getWindowHandleAfterBrowserIsQuitThrowsCommunicationException() {
    driver.quit();

    try {
      driver.getWindowHandle();
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(CommunicationException.class)));
      assertThat(e.getMessage(), containsString(BROWSER_NOT_CONNECTED_STRING));
    }
  }

  @Test
  public void switchToAfterBrowserIsQuitThrowsCommunicationException() {
    driver.quit();

    try {
      driver.switchTo();
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(CommunicationException.class)));
      assertThat(e.getMessage(), containsString(BROWSER_NOT_CONNECTED_STRING));
    }
  }

  @Test
  public void findMultipleElementsAfterBrowserIsQuitThrowsCommunicationException() {
    driver.quit();

    try {
      driver.findElements(By.id("foo"));
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(CommunicationException.class)));
      assertThat(e.getMessage(), containsString(BROWSER_NOT_CONNECTED_STRING));
    }
  }

  @Test
  public void findSingleElementAfterBrowserIsQuitThrowsCommunicationException() {
    driver.quit();

    try {
      driver.findElement(By.id("foo"));
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(CommunicationException.class)));
      assertThat(e.getMessage(), containsString(BROWSER_NOT_CONNECTED_STRING));
    }
  }

  @Test
  public void multipleInstances() {
    TestDriver a;
    TestDriver b;
    TestDriver c;

    try {
      a = new TestDriverBuilder().get();
      b = new TestDriverBuilder().get();
      c = new TestDriverBuilder().get();
    } catch (WebDriverException e) {
      // If immediately exited, then it doesn't support the flags
      if (e.getMessage().contains("Opera exited immediately")) {
        return;
      } else {
        throw e;
      }
    }

    a.navigate().to(pages.test);
    b.navigate().to(pages.javascript);
    c.navigate().to(pages.keys);

    assertThat(a.getCurrentUrl(), containsString("test.html"));
    assertThat(b.getCurrentUrl(), containsString("javascript.html"));
    assertThat(c.getCurrentUrl(), containsString("keys.html"));

    a.quit();
    b.quit();
    c.quit();
  }

}