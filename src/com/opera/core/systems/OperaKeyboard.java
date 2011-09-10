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