/*
Copyright 2008-2012 Opera Software ASA

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Mouse;
import org.openqa.selenium.interactions.Actions;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

public class MouseTest extends OperaDriverTestCase {

  public Mouse mouse;
  public OperaWebElement log;
  public OperaWebElement test;

  @Before
  public void beforeEach() {
    mouse = driver.getMouse();
    driver.navigate().to(pages.mouse);
    log = (OperaWebElement) driver.findElementById("log");
    test = (OperaWebElement) driver.findElementById("test");
  }

  @After
  public void afterEach() {
    // Tests sometimes cause problems because a context menu is opened on Desktop, ensure that we
    // cancel the context menu if any.
    new Actions(driver).sendKeys(Keys.ESCAPE).perform();
  }

  @Test
  public void mouseOver() {
    String hash = test.getImageHash();
    mouse.mouseMove(test.getCoordinates());
    assertNotSame(hash, test.getImageHash());
  }

  @Test
  @Ignore(value = "Unknown why we're not triggering dblclick ES events, investigate")
  public void doubleClick() {
    mouse.doubleClick(test.getCoordinates());
    assertThat(log(), containsString("dblclick"));
  }

  @Test
  public void contextClick() {
    mouse.contextClick(test.getCoordinates());

    assertTrue(log().contains("mousedown 2"));
    assertTrue(log().contains("mouseup 2"));
  }

  private String log() {
    return log.getAttribute("value");
  }

}