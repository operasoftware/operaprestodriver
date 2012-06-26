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

import com.opera.core.systems.model.ColorResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class ColorResultTest {

  public ColorResult result;

  @Test
  public void construction() {
    result = new ColorResult(1, 2);
    assertNotNull(result);
    assertEquals(1, result.getId());
    assertEquals(2, result.getCount());
  }

  @Test
  public void equalityNotSame() {
    result = new ColorResult(1, 2);
    ColorResult secondResult = new ColorResult(3, 4);
    assertNotSame(result, secondResult);
  }

  @Test
  public void equalitySame() {
    result = new ColorResult(1, 2);
    ColorResult secondResult = new ColorResult(1, 2);
    assertEquals(result, secondResult);
  }

}