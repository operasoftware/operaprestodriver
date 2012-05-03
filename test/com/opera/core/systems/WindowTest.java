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

import com.opera.core.systems.pages.WindowPage;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.TestingPageFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.opera.core.systems.OperaProduct.CORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.openqa.selenium.Platform.WINDOWS;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class WindowTest extends OperaDriverTestCase {

  public WindowPage windowPage;

  @Before
  public void bindPage() {
    windowPage = TestingPageFactory.initElements(driver, pages, WindowPage.class);
  }

  @After
  public void afterEach() {
    driver.closeAll();
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void testOpenNewTargetedWindow() {
    windowPage.openNewTargetedWindow();

    int windowCount = driver.getWindowCount();
    assertEquals(newWindows(1), windowCount);

    driver.switchTo().window("targeted");
    assertEquals("Window 2", driver.getTitle());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void testOpenMultipleTargetedWindows() {
    for (int i = 1; i <= 2; i++) {
      windowPage.openNewTargetedWindow();
    }

    assertEquals(newWindows(1), driver.getWindowCount());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void testOpenNewAnonymousWindow() {
    windowPage.openNewAnonymousWindow();

    int windowCount = driver.getWindowCount();
    assertEquals(newWindows(1), windowCount);
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void testOpenMultipleWindows() {
    int windowsToOpen = 3;

    for (int i = 1; i <= windowsToOpen; i++) {
      windowPage.openNewAnonymousWindow();
    }

    assertEquals(newWindows(windowsToOpen), driver.getWindowCount());
  }

  @Test
  @Ignore(products = CORE, value = "gogi does not quit when closing last window")
  public void testCloseShouldQuitBrowserIfLastWindow() {
    driver.close();
    assertFalse(driver.isRunning());
  }

  /**
   * Returns the total number of new windows expected.  Since we always want to keep the default
   * window open, we add + 1.
   *
   * @param totalNumberNewWindows windows opened
   * @return the number of total windows expected to be open
   */
  private int newWindows(int totalNumberNewWindows) {
    return totalNumberNewWindows + 1;
  }

}