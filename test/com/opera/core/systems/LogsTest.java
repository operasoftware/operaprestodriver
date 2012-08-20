package com.opera.core.systems;

import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LogsTest extends OperaDriverTestCase {

  public WebElement triggerLog;

  @Before
  public void setup() {
    driver.get(pages.console);
    triggerLog = driver.findElement(By.id("trigger_log"));
  }

  @Test
  public void receiveOperaLogsInstance() {
    assertThat(logs(), is(instanceOf(OperaLogs.class)));
  }

  @Test
  public void registersDriverLogType() {
    assertTrue(logs().getAvailableLogTypes().contains(LogType.DRIVER));
  }

  @Test
  public void registersEcmascriptLogType() {
    logEcmascript();
    assertTrue(logs().getAvailableLogTypes().contains(OperaLogType.ECMASCRIPT));
  }

  @Test
  public void registersCssLogType() {
    assertTrue(logs().getAvailableLogTypes().contains(OperaLogType.CSS));
  }

  @Test
  public void receivesEcmascriptLogEntries() {
    logEcmascript();
    assertTrue(logs().get(OperaLogType.ECMASCRIPT).getAll().size() > 0);
  }

  @Test
  public void receivesCssLogEntries() {
    assertTrue(logs().get(OperaLogType.CSS).getAll().size() > 0);
  }

  @Test
  @Ignore("Not implemented in LocalLogs yet")
  public void gettingAllLogEntriesClearsList() {
    // Make sure preconditions are set
    assertTrue(logs().get(OperaLogType.CSS).getAll().size() > 0);
    assertEquals(0, logs().get(OperaLogType.CSS).getAll().size());
  }

  private void logEcmascript() {
    triggerLog.click();
  }

  private Logs logs() {
    return driver.manage().logs();
  }

}