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
import com.opera.core.systems.testing.FreshDriver;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;
import com.opera.core.systems.testing.TestingPageFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;
import static com.opera.core.systems.OperaProduct.MINI;
import static com.opera.core.systems.OperaProduct.SDK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.openqa.selenium.Platform.WINDOWS;

public class WindowTest extends OperaDriverTestCase {

  public WindowPage windowPage;

  @Before
  public void bindPage() {
    createDriverIfNecessary();
    windowPage = TestingPageFactory.initElements(driver, pages, WindowPage.class);
  }

  @After
  public void prepareNextTest() {
    try {
      while (driver.getWindowHandles().size() > 1) {
        driver.close();
      }
    } catch (TimeoutException e) {
      fail("Failed to close windows after test: " + e.getMessage());
    }
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void openNewTargetedWindow() {
    windowPage.openNewTargetedWindow();

    assertEquals(newWindows(1), driver.getWindowHandles().size());

    driver.switchTo().window("targeted");
    assertEquals("Window 2", driver.getTitle());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void openMultipleTargetedWindows() {
    for (int i = 1; i <= 2; i++) {
      windowPage.openNewTargetedWindow();
    }

    assertEquals(newWindows(1), driver.getWindowHandles().size());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void openNewAnonymousWindow() {
    windowPage.openNewAnonymousWindow();
    assertEquals(newWindows(1), driver.getWindowHandles().size());
  }

  @Test
  @Ignore(platforms = WINDOWS)
  public void openMultipleWindows() {
    int windowsToOpen = 3;

    for (int i = 1; i <= windowsToOpen; i++) {
      windowPage.openNewAnonymousWindow();
    }

    assertEquals(newWindows(windowsToOpen), driver.getWindowHandles().size());
  }

  @Test
  public void closeLastWindowQuitsBrowser() {
    driver.close();
    assertFalse(driver.isRunning());
  }

  @Test
  public void twoClosesDoesNotThrow() {
    driver.close();
    driver.close();
  }

  @Test
  @FreshDriver
  @Ignore(products = CORE,
          value = "Not able to differentiate between popup windows and actual windows on core-gogi")
  public void closeLastWindowWithHTTPAuthQuitsBrowser() {
    driver.navigate().to(pages.basicAuth);
    driver.close();
    assertEquals(0, driver.getWindowHandles().size());
    assertFalse(driver.isRunning());
  }

  @Test
  @FreshDriver
  @Ignore(products = {MINI, SDK})
  public void resetWindowState() {
    createWindow();
    assertEquals(2, driver.getWindowHandles().size());

    driver.navigate().to(pages.basicAuth);

    // core-gogi uses non-native popup dialogues with runtimes.  These windows are not hidden from
    // the window manager and must be taken into account.
    if (currentProduct().is(CORE)) {
      assertEquals(3, driver.getWindowHandles().size());
    } else {
      assertEquals(2, driver.getWindowHandles().size());
    }

    closeAllWindows();

    if (currentProduct().is(DESKTOP)) {
      createWindow();
    }

    assertEquals(1, driver.getWindowHandles().size());
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

  private void closeAllWindows() {
    driver.getServices().getWindowManager().closeAllWindows();
  }

  private void createWindow() {
    driver.getServices().getWindowManager().createWindow();
  }

}