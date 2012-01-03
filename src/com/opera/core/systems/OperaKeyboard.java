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
import org.openqa.selenium.Keyboard;
import org.openqa.selenium.Keys;

public class OperaKeyboard implements Keyboard {
  private final OperaDriver parent;

  public OperaKeyboard(OperaDriver parent) {
    this.parent = parent;
  }

  public void sendKeys(CharSequence... keysToSend) {
    parent.switchTo().activeElement().sendKeys(keysToSend);
  }

  public void pressKey(Keys keyToPress) {
    parent.keyDown(OperaKeys.get(((Keys) keyToPress).name()));
  }

  public void releaseKey(Keys keyToRelease) {
    parent.keyUp(OperaKeys.get(((Keys) keyToRelease).name()));
  }

}