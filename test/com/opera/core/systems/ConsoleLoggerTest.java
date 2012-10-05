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

import com.opera.core.systems.scope.services.ConsoleLogger;
import com.opera.core.systems.testing.Ignore;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.opera.core.systems.OperaProduct.MOBILE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore(products = MOBILE)
public class ConsoleLoggerTest extends OperaDriverTestCase {

  public WebElement triggerLog;

  @Before
  public void setup() {
    driver.get(pages.console);
    triggerLog = driver.findElement(By.id("trigger_log"));
  }

  @Test
  public void receivesConsoleMessages() {
    triggerLog.click();
    assertTrue(getConsole().listMessages().size() > 0);
  }

  @Test
  public void clear() {
    triggerLog.click();

    getConsole().clear();
    assertEquals(0, getConsole().listMessages().size());
  }

  private ConsoleLogger getConsole() {
    return driver.getServices().getConsoleLogger();
  }

  private void log(String message) {
    driver.executeScript(String.format("console.logger('%s');", message));
  }

}