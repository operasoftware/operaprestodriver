/*
Copyright 2008-2012 Opera Software ASA

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

import com.google.common.collect.ImmutableList;

import com.opera.core.systems.internal.input.KeyEvent;
import com.opera.core.systems.internal.input.KeyboardModifiers;
import com.opera.core.systems.scope.internal.OperaKey;
import com.opera.core.systems.scope.services.IOperaExec;

import org.openqa.selenium.Keyboard;
import org.openqa.selenium.Keys;

import java.util.List;

/**
 * Opera uses the "exec" Scope service to trigger key events in Opera.  This service is available on
 * all Opera products, on all devices and follows the same code paths as a user would when pressing
 * a key.
 */
public class OperaKeyboard implements Keyboard {

  private final IOperaExec exec;
  private final KeyboardModifiers modifiers = new KeyboardModifiers();

  public OperaKeyboard(OperaDriver parent) {
    exec = parent.getExecService();
  }


  public void sendKeys(CharSequence... keysToSend) {
    ImmutableList.Builder<KeyEvent> builder = ImmutableList.builder();

    // The array with keysToSend is a multidimensional list.  To make the algorithm for sending keys
    // and key combinations to the browser easier we flatten it first.
    for (CharSequence sequence : keysToSend) {
      for (int i = 0; i < sequence.length(); ++i) {
        builder.add(new KeyEvent(sequence.charAt(i)));
      }
    }

    sendKeys(builder.build());
    releaseModifiers();
  }

  private void sendKeys(List<KeyEvent> events) {
    for (KeyEvent event : events) {
      if (event.isNewLine()) {
        exec.key(OperaKey.ENTER.toScope());
      } else if (event.isModifierRelease()) {
        releaseModifiers();
      } else if (event.isModifier()) {
        pressOrReleaseModifier(event.getKey());
      } else if (event.isSpecial()) {
        exec.key(OperaKey.get(event.getKey()).toScope());
      } else {
        type(event.getCharacter());
      }
    }
  }

  private void releaseModifiers() {
    for (Keys modifierKey : modifiers) {
      releaseKey(modifierKey);
    }
  }

  private void pressOrReleaseModifier(Keys modifier) {
    if (modifiers.contains(modifier)) {
      releaseKey(modifier);
    } else {
      pressKey(modifier);
    }
  }

  private void type(char key) {
    String string = String.valueOf(key);

    if (modifiers.contains(Keys.LEFT_SHIFT) || modifiers.contains(Keys.SHIFT)) {
      string = string.toUpperCase();
    }

    exec.key(string);
  }

  public void pressKey(Keys keyToPress) {
    modifiers.add(keyToPress);
    exec.key(OperaKey.get(keyToPress).toScope(), false);
  }

  public void releaseKey(Keys keyToRelease) {
    modifiers.remove(keyToRelease);
    exec.key(OperaKey.get(keyToRelease).toScope(), true);
  }

}