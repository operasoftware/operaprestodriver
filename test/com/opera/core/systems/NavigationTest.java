/*
Copyright 2011 Opera Software ASA

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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NavigationTest extends OperaDriverTestCase {

  @Test
  public void testBack() {
    getFixture("javascript.html");
    getFixture("test.html");
    getFixture("keys.html");

    driver.navigate().back();
    assertTrue(driver.getCurrentUrl().indexOf("test.html") > 0);
  }

  @Test
  public void testForward() {
    driver.navigate().forward();
    assertTrue(driver.getCurrentUrl().indexOf("keys.html") > 0);
  }

  @Test
  public void testBack2() {
    driver.navigate().back();
    driver.navigate().back();
    assertTrue(driver.getCurrentUrl().indexOf("javascript.html") > 0);
  }

  @Test
  public void testForward2() {
    driver.navigate().forward();
    driver.navigate().forward();
    assertTrue(driver.getCurrentUrl().indexOf("keys.html") > 0);
  }

  @Test
  public void testHttpRedirect() throws Exception {
    driver.get("http://t/core/bts/javascript/CORE-26410/003-2.php");

    // Wait for redirect
    Thread.sleep(1000);

    assertEquals("http://t/core/bts/javascript/CORE-26410/001-3.php",
                 driver.getCurrentUrl());
  }

}