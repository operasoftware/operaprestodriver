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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FramesTest extends OperaDriverTestCase {

  @Before
  public void beforeEach() {
    driver.navigate().to(pages.frames);
  }

  @Test
  public void listOfFrames() {
    // frameset page, and three frames
    assertEquals(4, driver.listFrames().size());
  }

  @Test
  public void switchById() throws Exception {
    driver.switchTo().frame("a");
    assertNotNull(driver.findElement(By.id("one")));
  }

  @Test
  public void switchById2() throws Exception {
    driver.switchTo().frame("b");
    assertNotNull(driver.findElement(By.id("radio_little")));
  }

  @Test
  public void switchByIndex() {
    driver.switchTo().frame(0);
    assertNotNull(driver.findElement(By.id("one")));
  }

  @Test
  public void switchByIndex2() {
    driver.switchTo().frame(2);
    assertNotNull(driver.findElement(By.id(("img_container"))));
  }

}