package com.opera.core.systems;

import com.opera.core.systems.scope.services.IConsoleLogger;
import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertTrue;

public class ConsoleLoggerTest extends OperaDriverTestCase {

  @Test
  public void receivesConsoleMessages() {
    driver.get(pages.console);

    driver.findElement(By.id("trigger_log")).click();

    try {
      Thread.sleep(1000);
    } catch (Exception e) {
      // nothing
    }

    assertTrue(getConsole().listMessages().size() > 0);
  }

  private IConsoleLogger getConsole() {
    return driver.getServices().getConsoleLogger();
  }

  private void log(String message) {
    driver.executeScript(String.format("console.logger('%s');", message));
  }

}