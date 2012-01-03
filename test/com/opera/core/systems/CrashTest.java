/*
Copyright 2011-2012 Opera Software ASA

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

import com.opera.core.systems.scope.exceptions.CommunicationException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// TODO(stuartk): Make tests pass
public class CrashTest extends OperaDriverTestCase {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    setUpBeforeClass();
  }

  @After
  public void tearDown() throws Exception {
    tearDownAfterClass();
    // Make sure Opera is gone
    Runtime.getRuntime().exec("kill `pgrep opera").waitFor();
  }

  @Ignore
  @Test
  public void testCore34284() {
    assertTrue(driver.getRunner().isOperaRunning());

    try {
      driver.navigate().to("http://t/core/bts/crashers/visual/CORE-34284/001.html");
    } catch (CommunicationException e) {
      // nothing
    }

    assertFalse(driver.getRunner().isOperaRunning());
  }

  @Ignore
  @Test
  public void testCore32224() {
    assertTrue(driver.getRunner().isOperaRunning());

    try {
      driver.navigate().to("http://t/core/bts/crashers/visual/CORE-32224/001.html");
    } catch (CommunicationException e) {
      // nothing
    }

    assertFalse(driver.getRunner().isOperaRunning());
  }

}