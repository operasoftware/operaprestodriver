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

import org.junit.Test;
import org.openqa.selenium.Keys;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class OperaMobileKeysTest {

  @Test
  public void softKey1MapsToF1() {
    assertEquals(Keys.F1.charAt(0), OperaMobileKeys.SOFT_KEY_1.charAt(0));
  }

  @Test
  public void softKey2MapsToF2() {
    assertEquals(Keys.F2.charAt(0), OperaMobileKeys.SOFT_KEY_2.charAt(0));
  }

  @Test
  public void softKey3MapsToF3() {
    assertEquals(Keys.F3.charAt(0), OperaMobileKeys.SOFT_KEY_3.charAt(0));
  }

  @Test
  public void softKey4MapsToF4() {
    assertEquals(Keys.F4.charAt(0), OperaMobileKeys.SOFT_KEY_4.charAt(0));
  }

  @Test
  public void charAtPosition0ReturnsKeyCode() {
    assertNotSame(OperaMobileKeys.SOFT_KEY_1.charAt(0), 0);
  }

  @Test
  public void charAtOtherPositionReturnsZero() {
    assertEquals(OperaMobileKeys.SOFT_KEY_1.charAt(10), 0);
  }

  @Test
  public void lengthIsAlwaysOne() {
    assertThat(OperaMobileKeys.SOFT_KEY_1.length(), is(1));
  }

  @Test
  public void validSubSequence() {
    assertEquals(String.valueOf(OperaMobileKeys.SOFT_KEY_1),
                 OperaMobileKeys.SOFT_KEY_1.subSequence(0, 1));
  }

  @Test
  public void invalidSubSequenceThrows() {
    try {
      OperaMobileKeys.SOFT_KEY_1.subSequence(-1, 10);
      fail("Expected IndexOutOfBoundsException");
    } catch (RuntimeException e) {
      assertThat(e, is(instanceOf(IndexOutOfBoundsException.class)));
    }
  }

}