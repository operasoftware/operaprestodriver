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
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class OperaStringsTest {

  public static class IsInteger {

    @Test
    public void testRegularStringWithoutNumber() {
      assertEquals(false, OperaStrings.isInteger("sample"));
    }

    @Test
    public void testRegularStringWithNumberOnly() {
      assertEquals(true, OperaStrings.isInteger("9"));
    }

    @Test
    public void testRegularStringWithDecimalNumberOnly() {
      assertEquals(false, OperaStrings.isInteger("9.00"));
    }

    @Test
    public void testEmptyString() {
      assertEquals(false, OperaStrings.isInteger(""));
    }

    @Test
    public void testNullString() {
      assertEquals(false, OperaStrings.isInteger(null));
    }

  }

  public static class IsDouble {

    @Test
    public void testRegularStringWithoutNumber() {
      assertEquals(false, OperaStrings.isDouble("sample"));
    }

    @Test
    public void testRegularStringWithNumberOnly() {
      assertEquals(true, OperaStrings.isDouble("9"));
    }

    @Test
    public void testRegularStringWithDecimalNumberOnly() {
      assertEquals(true, OperaStrings.isDouble("9.00"));
    }

    @Test
    public void testEmptyString() {
      assertEquals(false, OperaStrings.isDouble(""));
    }

    @Test
    public void testNullString() {
      assertEquals(false, OperaStrings.isDouble(null));
    }

  }

  public static class IsNumeric {

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

  public static class EscapeJsString {

    @Test
    public void testEscapesDoubleQuotes() {
      assertEquals("return \\\"foo\\\" 'bar'", OperaStrings.escapeJsString("return \"foo\" 'bar'"));
    }

    @Test
    public void testEscapesSingleQuotes() {
      assertEquals("return \"foo\" \\'bar\\'",
                   OperaStrings.escapeJsString("return \"foo\" 'bar'", "'"));
    }

  }

}                                           