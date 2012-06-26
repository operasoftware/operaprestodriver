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

import com.opera.core.systems.model.OperaColor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OperaColorTest {

  @Test
  public void testSetHighRed() {
    OperaColor color = new OperaColor(120,110,100);
    color.setHighRed(255);
    assertEquals((Integer) 255, color.getHighRed());
  }

  @Test
  public void testSetLowRed() {
    OperaColor color = new OperaColor(120,110,100);
    color.setLowRed(255);
    assertEquals((Integer) 255, color.getLowRed());
  }

  @Test
  public void testSetHighGreen() {
    OperaColor color = new OperaColor(120,110,100);
    color.setHighGreen(255);
    assertEquals((Integer) 255, color.getHighGreen());
  }

  @Test
  public void testSetLowGreen() {
    OperaColor color = new OperaColor(120,110,100);
    color.setLowGreen(255);
    assertEquals((Integer) 255, color.getLowGreen());
  }

  @Test
  public void testSetHighBlue() {
    OperaColor color = new OperaColor(120,110,100);
    color.setHighBlue(255);
    assertEquals((Integer) 255, color.getHighBlue());
  }

  @Test
  public void testSetLowBlue() {
    OperaColor color = new OperaColor(120,110,100);
    color.setLowBlue(255);
    assertEquals((Integer) 255, color.getLowBlue());
  }

}