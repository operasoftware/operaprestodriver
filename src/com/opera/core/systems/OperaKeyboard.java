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

import com.opera.core.systems.scope.internal.OperaKeys;
import com.opera.core.systems.scope.services.IOperaExec;

import org.openqa.selenium.Keyboard;
import org.openqa.selenium.Keys;

/**
 * Opera uses the "exec" Scope service to trigger key events in Opera.  This service is available on
 * all Opera products, on all devices and follows the same code paths as a user would when pressing
 * a key.
 */
public class OperaKeyboard implements Keyboard {

  private final OperaDriver parent;
  private final IOperaExec exec;

  public OperaKeyboard(OperaDriver parent) {
    this.parent = parent;
    this.exec = parent.getExecService();
  }

  public void sendKeys(CharSequence... keysToSend) {
    parent.switchTo().activeElement().sendKeys(keysToSend);
  }

  public void pressKey(Keys keyToPress) {
    exec.key(OperaKeys.get(((Keys) keyToPress).name()), false);
  }

  public void releaseKey(Keys keyToRelease) {
    exec.key(OperaKeys.get(((Keys) keyToRelease).name()), true);
  }

}