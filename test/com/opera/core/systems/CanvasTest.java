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

import com.opera.core.systems.model.Canvas;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

public class CanvasTest {

  public Canvas canvas;

  @Test
  public void construction() {
    canvas = new Canvas();

    assertEquals(0, canvas.getX());
    assertEquals(0, canvas.getY());
    assertEquals(0, canvas.getWidth());
    assertEquals(0, canvas.getHeight());

    assertFalse(canvas.isViewPortRelative());
  }

  @Test
  public void constructionWithArguments() {
    canvas = new Canvas(1, 2, 3, 4);

    assertEquals(1, canvas.getX());
    assertEquals(2, canvas.getY());
    assertEquals(3, canvas.getWidth());
    assertEquals(4, canvas.getHeight());

    assertFalse(canvas.isViewPortRelative());
  }

  @Test
  public void settingViewPortRelative() {
    canvas = new Canvas();
    canvas.setViewPortRelative(true);
    assertTrue(canvas.isViewPortRelative());
  }

  @Test
  public void equalityNull() {
    canvas = new Canvas();
    assertFalse(canvas.equals(null));
  }

  @Test
  public void equalityOtherObject() {
    canvas = new Canvas();
    assertFalse(canvas.equals(""));
  }

  @Test
  public void equalityEmptyCanvas() {
    canvas = new Canvas();
    Canvas secondCanvas = new Canvas();
    assertEquals(canvas, secondCanvas);
  }

  @Test
  public void equalityDifferentCanvases() {
    canvas = new Canvas(1, 2, 3, 4);
    Canvas secondCanvas = new Canvas();
    assertNotSame(canvas, secondCanvas);
  }

  @Test
  public void equalityDifferentCanvasesWithViewPortRelative() {
    canvas = new Canvas();
    canvas.setViewPortRelative(true);
    Canvas secondCanvas = new Canvas();
    assertNotSame(canvas, secondCanvas);
  }

  @Test
  public void stringification() {
    canvas = new Canvas();
    assertNotNull(canvas.toString());
    assertThat(canvas.toString(), containsString("Canvas"));
    assertThat(canvas.toString(), containsString("x=0"));
    assertThat(canvas.toString(), containsString("y=0"));
    assertThat(canvas.toString(), containsString("width=0"));
    assertThat(canvas.toString(), containsString("height=0"));
    assertThat(canvas.toString(), containsString("viewPortRelative=false"));
  }

}