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

import org.openqa.selenium.Keys;

/**
 * A soft key is a button flexibly programmable to invoke any of a number of functions rather than
 * being associated with a single fixed function or a fixed set of functions.
 *
 * OperaMobileKeys can be used in combination with WebDriver's {@link Keys} for special key input.
 * An example:
 *
 * <pre><code>
 *   element.sendKeys("foo", OperaMobileKeys.SOFT_KEY_1);
 * </code></pre>
 *
 * @since 0.19
 */
public enum OperaMobileKeys implements CharSequence {

  SOFT_KEY_1(Keys.F1),
  SOFT_KEY_2(Keys.F2),
  SOFT_KEY_3(Keys.F3),
  SOFT_KEY_4(Keys.F4);

  private final char keyCode;

  OperaMobileKeys(Keys alias) {
    keyCode = alias.charAt(0);
  }

  public int length() {
    return 1;
  }

  public char charAt(int index) {
    if (index == 0) {
      return keyCode;
    }

    return 0;
  }

  public CharSequence subSequence(int start, int end) {
    if (start == 0 && end == 1) {
      return String.valueOf(keyCode);
    }

    throw new IndexOutOfBoundsException();
  }

  @Override
  public String toString() {
    return String.valueOf(keyCode);
  }

}