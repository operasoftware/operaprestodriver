/*
Copyright 2012 Opera Software ASA
Copyright 2012 Ajay Kemperaj

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

import com.opera.core.systems.common.lang.OperaStrings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OperaStringTest {

  @Test
  public void testRegularStringWithoutNumber() {
    assertEquals(false, OperaStrings.isNumeric("sample"));
  }

  @Test
  public void testRegularStringWithNumberOnly() {
    assertEquals(true, OperaStrings.isNumeric("9"));
  }

  @Test
  public void testRegularStringWithDecimalNumberOnly() {
    assertEquals(true, OperaStrings.isNumeric("9.00"));
  }

  @Test
  public void testEmptyString() {
    assertEquals(false, OperaStrings.isNumeric(""));
  }

  @Test
  public void testNullString() {
    assertEquals(false, OperaStrings.isNumeric(null));
  }

}
