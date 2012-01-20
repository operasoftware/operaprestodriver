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

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import static com.opera.core.systems.OperaProduct.CORE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class WindowTest extends OperaDriverTestCase {
  
  public static String defaultWindowHandle;
  
  @BeforeClass
  public static void beforeAll() {
    getFixture("windows.html");
    defaultWindowHandle = driver.getWindowHandle();
  }

  @After
  public void afterEach() {
    int windowCountBeforeClosing = driver.getWindowCount();

    // If there are no windows open, we don't need to close any windows
    if (windowCountBeforeClosing == 0) {
      return;
    } else if (windowCountBeforeClosing > 1) {
      // Close all windows apart from the our first control window
      for (String windowHandle : driver.getWindowHandles()) {
        if (!windowHandle.equals(defaultWindowHandle)) {
          driver.switchTo().window(windowHandle);
          driver.close();
        }
      }
      
      assertEquals("Should have " + (windowCountBeforeClosing - 1) + " less window(s)",
                   1, driver.getWindowCount());
    }
    
    // Make sure we're in the control window before continuing
    assertEquals(defaultWindowHandle, driver.getWindowHandle());
  }

  @Test
  public void testOpenNewTargettedWindow() {
    openNewTargettedWindow();

    int windowCount = driver.getWindowCount();
    assertEquals(newWindows(1), windowCount);
    
    driver.switchTo().window("targetted");
    assertEquals("Window 2", driver.getTitle());
  }

  @Test
  public void testOpenMultipleTargettedWindows() {
    for (int i = 1; i <= 2; i++) {
      openNewTargettedWindow();
    }
    
    assertEquals(newWindows(1), driver.getWindowCount());
  }

  @Test
  public void testOpenNewAnonymousWindow() {
    openNewAnonymousWindow();
    
    int windowCount = driver.getWindowCount();
    assertEquals(newWindows(1), windowCount);
  }
  
  @Test
  public void testOpenMultipleWindows() {
    int windowsToOpen = 3;
    
    for (int i = 1; i <= windowsToOpen; i++) {
      openNewAnonymousWindow();
    }
    
    assertEquals(newWindows(windowsToOpen), driver.getWindowCount());
  }

  @Test
  @Ignore(products = CORE, value = "window-manager service is not coupled to gogi tabs")
  public void testCloseShouldQuitBrowserIfLastWindow() {
    driver.close();
    assertFalse(driver.isRunning());
  }

  /**
   * Opens a new anonymous window.  The WebDriver implementation should assign a new window handle
   * to it.
   */
  private void openNewAnonymousWindow() {
    int openWindows = driver.getWindowCount();
    openNewWindow("Open new anonymous window");
    assertEquals("One more window should be present", openWindows + 1, driver.getWindowCount());
  }

  /**
   * Opens a new targetted window.  This means no new windows will be opened if the link is clicked
   * several times.  By "targetted window" we refer to the DOMString specified in the attribute
   * "target" on the element.
   */
  private void openNewTargettedWindow() {
    openNewWindow("Open new targetted window");
  }

  /**
   * Opens a new window by link text.  It will navigate to the window control page, attempt to find
   * the web element with the specified link text, click that and wait for a new window to appear.
   * If the new window failed to appear, it will cause the test to fail.  It will then switch back
   * to the window you were in when performing this call, so that the current state is not modified.
   *
   * @param linkText the link text to click
   */
  private void openNewWindow(String linkText) {
    String currentWindow = driver.getWindowHandle();
    
    // Switch window if we're not on the default window
    if (!currentWindow.equals(defaultWindowHandle)) {
      driver.switchTo().window(defaultWindowHandle);
    }
    
    // Trigger new window load and wait for window to open
    driver.findElement(By.linkText(linkText)).click();
    try {
      Thread.sleep(1000);      
    } catch (InterruptedException e) {
      fail("Interrupted");
    }
    
    // Switch back to the page we were on
    driver.switchTo().window(currentWindow);
    assertEquals("Window control", driver.getTitle());
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