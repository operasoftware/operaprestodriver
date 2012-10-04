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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;

import java.util.logging.Level;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LogsTest extends OperaDriverTestCase {

  public WebElement triggerLog;
  public static Level oldLoggingLevel;

  @Before
  public void setup() {
    oldLoggingLevel = driver.getSettings().logging().getLevel();
    driver.getSettings().logging().setLevel(Level.FINE);
    driver.get(pages.console);
    triggerLog = driver.findElement(By.id("trigger_log"));
  }

  @After
  public void resetLoggingLevel() {
    driver.getSettings().logging().setLevel(oldLoggingLevel);
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