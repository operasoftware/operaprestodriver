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

    // The keys to send resembles what you could roughly call a multidimensional map.  To make the
    // algorithm for sending keys and key combinations to the browser easier we first want to
    // normalize array we receive.

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
      //exec.key(string.toUpperCase());
      //pressKey(Keys.SHIFT);
      exec.key(OperaKey.SHIFT.toScope(), false);
      exec.key(string.toUpperCase());
      //releaseKey(Keys.SHIFT);
      exec.key(OperaKey.SHIFT.toScope(), true);
    } else {
      exec.key(string);
    }
  }

  /*
  public void sendKeys(CharSequence... keysToSend) {
    // The keys to send resembles what you could roughly call a multidimensional map.  To make the
    // algorithm for sending keys and key combinations to the browser easier we first want to
    // normalize array we receive.
    ImmutableList.Builder<Character> normalized = ImmutableList.builder();

    for (CharSequence sequence : keysToSend) {
      if (sequence instanceof Keys) {
        normalized.add(sequence.charAt(0));
      } else {
        for (final char c : sequence.toString().toCharArray()) {
          normalized.add(c);
        }
      }
    }

    sendKeys(normalized.build());
  }

  private void sendKeys(List<Character> characters) {
    for (final char c : characters) {
      Keys key = OperaKey.getKeyFromUnicode(c);
      OperaKey operaKey = OperaKey.get(key);

      if (c == '\n') {
        exec.key(OperaKey.ENTER.getValue());
      } else if (operaKey != null) {
        if (key == Keys.NULL) {
          for (Keys modifierKey : modifiers) {
            releaseKey(modifierKey);
          }
        } else if (KeyboardModifiers.isModifier(key)) {
          if (modifiers.contains(key)) {
            releaseKey(key);
          } else {
            pressKey(key);
          }
        } else {
          exec.key(operaKey.getValue());
        }
      } else {
        String keyAsString = String.valueOf(c);
        if (modifiers.contains(Keys.SHIFT) || modifiers.contains(Keys.LEFT_SHIFT)) {
          keyAsString = keyAsString.toUpperCase();
        }
        exec.key(keyAsString);
      }
    }

    for (Keys modifierKey : modifiers) {
      releaseKey(modifierKey);
    }
  }
  */

  /*

  public void bsendKeys(CharSequence... keysToSend) {

    // The keys to send is an array, and might look like this: [ Keys.SHIFT, "foo" + Keys.SHIFT ]
    for (CharSequence sequence : keysToSend) {
      if (sequence instanceof Keys) {
        Keys key = (Keys) sequence;
        modifiers.storeKeyDown(key);
        exec.key(OperaKey.get(key).getValue(), false);
      } else {
        for (final char c : sequence.toString().toCharArray()) {
          if (c == '\n') {
            exec.key(OperaKey.ENTER.getValue());
            continue;
          }

          if (specialKeysLookup.containsKey(c)) {
            Keys key = specialKeysLookup.get(c);

            if (key == Keys.NULL) {
              for (Keys modifierKey : modifiers) {
                exec.key(OperaKey.get(modifierKey).getValue(), true);
              }
            } else if (!modifiers.isPressed(key)) {
              exec.key(OperaKey.get(key).getValue(), false);
              modifiers.storeKeyDown(key);
            }

            continue;
          }

          exec.key(String.valueOf(c));
        }
      }
    }


    for (final char c : builder.toString().toCharArray()) {

    }

  }


  */

  /*

public void asendKeys(CharSequence... keysToSend) {
  for (CharSequence seq : keysToSend) {
    if (seq instanceof Keys) {
      String key = OperaKey.get(((Keys) seq).name());
      // Check if this is a key we hold down, and haven't already pressed, and press, but don't
      // release it. That's done at the end of this method.
      if (holdKeys.contains(key) && !heldKeys.contains(key) && !exec.keyIsPressed(key)) {
        exec.key(key, false);
        heldKeys.add(key);
      } else if (key.equals("null")) {
        for (String hkey : heldKeys) {
          exec.key(hkey, true);
        }
      } else {
        exec.key(key);
      }
    } else if (seq.toString().equals("\n")) {
      exec.key("enter");
    } else {
      // We need to check each character to see if it is a "special" key
      for (int i = 0; i < seq.length(); i++) {
        Character c = seq.charAt(i);
        String keyName = charToKeyName(c);

        // Buffer normal keys for a single type() call
        if (keyName == null) {
          exec.type(c.toString());
        } else {
          String key = OperaKey.get(keyName);
          // TODO: Code repeated from above
          if (holdKeys.contains(key) && !heldKeys.contains(key) && !exec
              .keyIsPressed(key)) {
            exec.key(key, false);
            heldKeys.add(key);
          } else if (key.equals("null")) {
            for (String hkey : heldKeys) {
              exec.key(hkey, true);
            }
          } else {
            exec.key(key);
          }
        }
      }
    }
  }
  */
  /*
  if (heldKeys.size() > 0) {
    for (String key : heldKeys) {
      exec.key(key, true);
    }
  }
  */
  //}

  public void pressKey(Keys keyToPress) {
    //exec.key(OperaKey.get((keyToPress).name()), false);
    //modifiers.keyDown(keyToPress);
    modifiers.add(keyToPress);
    exec.key(OperaKey.get(keyToPress).toScope(), false);
  }

  public void releaseKey(Keys keyToRelease) {
    //exec.key(OperaKey.get((keyToRelease).name()), true);
    //modifiers.keyUp(keyToRelease);
    modifiers.remove(keyToRelease);
    exec.key(OperaKey.get(keyToRelease).toScope(), true);
  }

}