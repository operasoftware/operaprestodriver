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
import com.google.common.collect.Maps;

import com.opera.core.systems.internal.KeyboardModifiers;
import com.opera.core.systems.scope.internal.OperaKey;
import com.opera.core.systems.scope.services.IOperaExec;

import org.openqa.selenium.Keyboard;
import org.openqa.selenium.Keys;

import java.util.List;
import java.util.Map;

/**
 * Opera uses the "exec" Scope service to trigger key events in Opera.  This service is available on
 * all Opera products, on all devices and follows the same code paths as a user would when pressing
 * a key.
 */
public class OperaKeyboard implements Keyboard {

  //private static final List<OperaKey> holdKeys = ImmutableList.of(OperaKey.SHIFT, OperaKey.CONTROL);

  /*
  private static final Map<Character, Keys> specialKeysLookup =
      Maps.uniqueIndex(ImmutableList.copyOf(Keys.values()), new Function<Keys, Character>() {
        public Character apply(Keys key) {
          return key.charAt(0);
        }
      });
      */

  private final Map<Character, Keys> specialKeysLookup = Maps.newHashMap();

  private final IOperaExec exec;
  //private final List<String> heldKeys = Lists.newArrayList();
  private final KeyboardModifiers modifiers = new KeyboardModifiers();

  public OperaKeyboard(OperaDriver parent) {
    exec = parent.getExecService();

    for (Keys key : Keys.values()) {
      specialKeysLookup.put(key.charAt(0), key);
    }
  }


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
      if (c == '\n') {
        exec.key(OperaKey.ENTER.getValue());
      } else if (specialKeysLookup.containsKey(c)) {
        Keys key = specialKeysLookup.get(c);
        OperaKey operaKey = OperaKey.get(key);

        if (key == Keys.NULL) {
          for (Keys modifierKey : modifiers) {
            exec.key(operaKey.getValue(), true);
            modifiers.keyUp(modifierKey);
          }
        //} else if (KeyboardModifiers.MODIFIERS.contains(key) && !modifiers.contains(key)) {
        } else if (KeyboardModifiers.isModifier(key) && !modifiers.contains(key)) {
          exec.key(operaKey.getValue(), false);
          modifiers.keyDown(key);
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
  }

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
    exec.key(OperaKey.get(keyToPress).getValue(), false);
  }

  public void releaseKey(Keys keyToRelease) {
    //exec.key(OperaKey.get((keyToRelease).name()), true);
    exec.key(OperaKey.get(keyToRelease).getValue(), true);
  }

  private static final Map<Character, Keys> keysLookup = Maps.newHashMap();

  /**
   * Converts a character in the PUA to the name of the key, as given by {@link
   * org.openqa.selenium.Keys}. If the character doesn't appear in that class then null is
   * returned.
   *
   * @param c the character that may be a special key
   * @return a string containing the name of the "special" key or null
   */
  private static Keys charToKeyName(char c) { // TODO(andreastt): Move this to OperaKeyboard?
    if (keysLookup.isEmpty()) {
      for (Keys k : Keys.values()) {
        keysLookup.put(k.charAt(0), k);
      }
    }
    return keysLookup.get(c);
  }

}