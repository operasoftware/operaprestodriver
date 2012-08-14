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

package com.opera.core.systems.internal.input;

import com.google.common.collect.ImmutableSet;

import org.openqa.selenium.Keys;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the state of the keyboard's modifier keys (shift, control, meta keys) for OperaDriver.
 */
public class KeyboardModifiers extends HashSet<Keys> implements Iterable<Keys> {

  public static final Set<Keys> MODIFIERS = new ImmutableSet.Builder<Keys>() {{
    add(Keys.LEFT_SHIFT);
    add(Keys.SHIFT);
    add(Keys.LEFT_CONTROL);
    add(Keys.CONTROL);
    add(Keys.LEFT_ALT);
    add(Keys.ALT);
  }}.build();

  public KeyboardModifiers() {
    super(new HashSet<Keys>());
  }

  /*
  public void keyDown(Keys key) {
    if (!KeyboardModifiers.isModifier(key)) {
      return;
    }

    add(key);
  }

  public void keyUp(Keys key) {
    if (!KeyboardModifiers.isModifier(key)) {
      return;
    }

    remove(key);
  }
  */

  public static boolean isModifier(Keys key) {
    return MODIFIERS.contains(key);
  }

}