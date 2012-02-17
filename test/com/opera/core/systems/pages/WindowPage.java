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

package com.opera.core.systems.pages;

import com.opera.core.systems.testing.Pages;
import com.opera.core.systems.testing.drivers.TestOperaDriver;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WindowPage extends Page {

  @FindBy(linkText = "Open new targeted window")
  public WebElement targetedWindow;

  @FindBy(linkText = "Open new anonymous window")
  public WebElement anonymousWindow;

  public WindowPage(TestOperaDriver driver, Pages pages) {
    super(driver, pages);
    driver.navigate().to(pages.windows);
  }

  /**
   * Opens a new anonymous window.  The WebDriver implementation should assign a new window handle
   * to it.
   */
  public void openNewAnonymousWindow() {
    int openWindows = driver.getWindowCount();
    openNewWindow(anonymousWindow);
    assertEquals("One more window should be present", openWindows + 1, driver.getWindowCount());
  }

  /**
   * Opens a new targeted window.  This means no new windows will be opened if the link is clicked
   * several times.  By "targeted window" we refer to the DOMString specified in the attribute
   * "target" on the element.
   */
  public void openNewTargetedWindow() {
    openNewWindow(targetedWindow);
  }

  /**
   * Opens a new window by link text.  It will navigate to the window control page, attempt to find
   * the web element with the specified link text, click that and wait for a new window to appear.
   * If the new window failed to appear, it will cause the test to fail.  It will then switch back
   * to the window you were in when performing this call, so that the current state is not
   * modified.
   *
   * @param windowLink the link element to click
   */
  private void openNewWindow(WebElement windowLink) {
    String currentWindow = driver.getWindowHandle();

    // Switch window if we're not on the default window
    if (!currentWindow.equals(driver.getControlWindow())) {
      driver.switchTo().window(driver.getControlWindow());
    }

    // Trigger new window load and wait for window to open
    windowLink.click();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      fail("Interrupted");
    }

    // Switch back to the page we were on
    driver.switchTo().window(currentWindow);
    assertEquals("Window control", driver.getTitle());
  }

}