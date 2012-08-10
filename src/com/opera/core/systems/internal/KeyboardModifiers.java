/*
Copyright 2007-2010 Selenium committers
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

package com.opera.core.systems.internal;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.openqa.selenium.Keys;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Holds teh state of the keyboard's modifier keys (shift, control, meta keys) for OperaDriver.
 */
public class KeyboardModifiers extends HashSet<Keys> implements Iterable<Keys> {

  public static final Set<Keys> MODIFIERS = ImmutableSet.of(Keys.LEFT_SHIFT, Keys.SHIFT,
                                                            Keys.LEFT_CONTROL, Keys.CONTROL,
                                                            Keys.LEFT_ALT, Keys.ALT);

  //private final Set<Keys> pressed; // = Sets.newHashSet();

  public KeyboardModifiers() {
    super(new HashSet<Keys>());
  }

  public void keyDown(Keys key) {
    checkArgument(MODIFIERS.contains(key), "Key is not a modifier");
    add(key);
  }

  public void keyUp(Keys key) {
    checkArgument(MODIFIERS.contains(key), "Key is not a modifier");
    remove(key);
  }

  /*
  private boolean shiftPressed = false;
  private boolean controlPressed = false;
  private boolean metaPressed = false;

  public boolean isShiftPressed() {
    return shiftPressed;
  }

  public boolean isControlPressed() {
    return controlPressed;
  }

  public boolean isMetaPressed() {
    return metaPressed;
  }

  public void storeKeyDown(Keys key) {
    storeIfEqualsShift(key, true);
    storeIfEqualsControl(key, true);
    storeIfEqualsMeta(key, true);
  }

  public void storeKeyUp(Keys key) {
    storeIfEqualsShift(key, false);
    storeIfEqualsControl(key, false);
    storeIfEqualsMeta(key, false);
  }

  private void storeIfEqualsShift(Keys key, boolean keyState) {
    if (key.equals(Keys.SHIFT)) {
      shiftPressed = keyState;
    }
  }

  private void storeIfEqualsControl(Keys key, boolean keyState) {
    if (key.equals(Keys.CONTROL)) {
      controlPressed = keyState;
    }
  }

  private void storeIfEqualsMeta(Keys key, boolean keyState) {
    if (key.equals(Keys.ALT)) {
      metaPressed = keyState;
    }
  }
  */

  public static boolean isModifier(Keys key) {
    return MODIFIERS.contains(key);
  }

}