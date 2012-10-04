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

import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ScopeWindowTest extends OperaDriverTestCase {

  @After
  public void prepareNextTest() {
    try {
      while (driver.getWindowHandles().size() > 1) {
        closeWindow();
      }
    } catch (TimeoutException e) {
      fail("Failed to close windows after test: " + e.getMessage());
    }
  }

  @Test
  public void openNewWindow() {
    createWindow();
    assertEquals(newWindows(1), driver.getWindowHandles().size());
  }

  @Test
  public void openMultipleWindows() {
    int toOpen = 3;

    for (int i = driver.getWindowHandles().size(); i <= toOpen; ++i) {
      createWindow();
    }

    assertEquals(newWindows(toOpen), driver.getWindowHandles().size());
  }

  @Test
  public void closeLastWindowQuitsBrowser() {
    driver.close();
    assertFalse(driver.isRunning());
  }

  private void createWindow() {
    driver.getServices().getWindowManager().createWindow();
  }

  private void closeWindow() {
    driver.close();
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