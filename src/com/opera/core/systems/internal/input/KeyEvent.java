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

import org.openqa.selenium.Keys;

/**
 * Represents a single key event.
 */
public class KeyEvent {

  private final Character character;
  private final Keys unicodeKey;

  public KeyEvent(char ch) {
    character = ch;
    unicodeKey = Keys.getKeyFromUnicode(character);
  }

  /**
   * Gets the event as its character representation.
   *
   * @return the character
   */
  public char getCharacter() {
    return character;
  }

  /**
   * Gets the event as its Unicode PUA key representation.
   *
   * @return the key
   */
  public Keys getKey() {
    return unicodeKey;
  }

  /**
   * Keys that are pressable but which doesn't yield any text are special, and a keyboard
   * implementation must likely treat them accordingly.
   *
   * @return true if event represents a "special", non-textual key; false otherwise
   */
  public boolean isSpecial() {
    return getKey() != null;
  }

  /**
   * Determines whether event is a modifier key.
   *
   * @return true if event is a modifier key, false otherwise
   */
  public boolean isModifier() {
    return KeyboardModifiers.isModifier(getKey());
  }

  /**
   * The Unicode PUA character for <code>NULL</code> signalises that all active modifier keys should
   * be released via a key up event.
   *
   * @return true if event is a modifier release, false otherwise
   */
  public boolean isModifierRelease() {
    return getKey() == Keys.NULL;
  }

  /**
   * The newline character, <code>\n</code>, must be handled as using the <kbd>Enter</kbd> key.
   *
   * @return true if event is a newline character, false otherwise
   */
  public boolean isNewLine() {
    return getCharacter() == '\n';
  }

}