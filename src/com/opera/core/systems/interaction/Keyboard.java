/*
Copyright 2008-2011 Opera Software ASA

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
package com.opera.core.systems.interaction;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.scope.internal.OperaKeys;

/**
 * Keyboard user interactions, part of OperaUserInteraction to support typing
 * and key strokes
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */
public abstract class Keyboard implements UserInteraction {

  public static Keyboard keyDown(final Keys keys) {
    return new Keyboard() {

      public void execute(WebDriver driver) {
        String key = OperaKeys.get(keys.name());
        keyUp(key);
      }

    };
  }

  public static Keyboard keyUp(final Keys keys) {
    return new Keyboard() {

      public void execute(WebDriver driver) {
        String key = OperaKeys.get(keys.name());
        keyDown(key);
      }

    };
  }

  public static Keyboard keyDown(final String key) {
    return new Keyboard() {

      public void execute(WebDriver driver) {
        ((OperaDriver) driver).keyDown(key);
      }

    };
  }

  public static Keyboard keyUp(final String key) {
    return new Keyboard() {

      public void execute(WebDriver driver) {
        ((OperaDriver) driver).keyUp(key);
      }

    };
  }

  public static Keyboard key(final Keys keys) {
    return new Keyboard() {

      public void execute(WebDriver driver) {
        String key = OperaKeys.get(keys.name());
        key(key);
      }

    };
  }

  public static Keyboard key(final String key) {
    return new Keyboard() {

      public void execute(WebDriver driver) {
        ((OperaDriver) driver).key(key);
      }

    };
  }

  public static Keyboard type(final String using) {
    return new Keyboard() {
      public void execute(WebDriver driver) {
        ((OperaDriver) driver).type(using);
      }

    };
  }

  public static Keyboard releaseKeys() {
    return new Keyboard() {
      public void execute(WebDriver driver) {
        ((OperaDriver) driver).releaseKeys();
      }

    };
  }

}
