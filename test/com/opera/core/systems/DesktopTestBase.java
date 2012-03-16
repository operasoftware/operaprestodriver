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

import com.opera.core.systems.testing.OperaDriverTestCase;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class DesktopTestBase extends OperaDriverTestCase {

  protected static OperaDesktopDriver driver;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    driver = new OperaDesktopDriver();
  }


  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    driver.quit();
  }

}