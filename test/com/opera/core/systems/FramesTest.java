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

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FramesTest extends OperaDriverTestCase {
  List<String> frames;

  @Before
  public void setUp() throws Exception {
    getFixture("frames.html");
    frames = driver.listFrames();
  }

  @Test
  public void testList() throws Exception {
    // frameset page, and three frames
    assertEquals(4, frames.size());
  }

  @Test
  public void testSwitchString() throws Exception {
    driver.switchTo().frame("a");

    // first frame
    assertNotNull(driver.findElementById("one"));
  }

  @Test
  public void testSwitchString2() throws Exception {
    driver.switchTo().frame("b");

    // second frame
    assertNotNull(driver.findElementById("radio_little"));
  }

  @Test
  public void testSwitchInt() throws Exception {
    driver.switchTo().frame(0);

    // first frame
    assertNotNull(driver.findElementById("one"));
  }

  @Test
  public void testSwitchIntUnnamed() throws Exception {
    driver.switchTo().frame(2);

    // only exists in third frame
    assertNotNull(driver.findElementById("img_container"));
  }

}