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

import com.opera.core.systems.model.OperaColor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OperaColorTest {

  @Test
  public void testConstructingOperaColorWithRGBString() {
    OperaColor color = new OperaColor("rgb(120, 110, 100)");

    // java.awt.Color
    assertEquals(120, color.getRed());
    assertEquals(110, color.getGreen());
    assertEquals(100, color.getBlue());

    // OperaColor
    assertEquals((Integer) 120, color.getHighRed());
    assertEquals((Integer) 120, color.getLowRed());
    assertEquals((Integer) 110, color.getHighGreen());
    assertEquals((Integer) 110, color.getLowGreen());
    assertEquals((Integer) 100, color.getHighBlue());
    assertEquals((Integer) 100, color.getLowBlue());
  }

  @Test
  public void testToHex() {
    String color = OperaColor.toHex(120,110,100);
    assertEquals(7, color.length());
    assertEquals("#786E64", color);
  }

  @Test
  public void testToHexWithBlack() {
    assertEquals("#00FFFF", OperaColor.toHex(0,255,255));
    assertEquals("#FF00FF", OperaColor.toHex(255,0,255));
    assertEquals("#FFFF00", OperaColor.toHex(255,255,0));
  }

  @Test
  public void testToHexWithComplexColour() {
    assertEquals("#00FF96", OperaColor.toHex(0,255,150));
  }

  @Test
  public void testGetHex() {
    OperaColor color = new OperaColor("rgb(120, 110, 100");
    String hex = color.getHex();
    assertEquals(7, hex.length());
    assertEquals("#786E64", hex);
  }

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