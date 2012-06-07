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

package com.opera.core.systems.scope.services.ums;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.SystemInputCommand;
import com.opera.core.systems.scope.protos.SystemInputProtos.KeyPressInfo;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.services.ISystemInput;
import com.opera.core.systems.util.WatirUtils;

import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * SystemInputManager handles systemInput simulating real clicks, and key presses.
 *
 * @author Adam Minchinton, Karianne Ekern
 */
public class SystemInputManager extends AbstractService implements ISystemInput {

  protected final Logger logger = Logger.getLogger(this.getClass().getName());
  protected final ClickDelayer clickDelayer = new ClickDelayer();

  SystemInputManager(ScopeServices services, String version) {
    super(services, version);

    String serviceName = "system-input";

    if (!isVersionInRange(version, "2.0", serviceName)) {
      throw new UnsupportedOperationException(serviceName + " version "
                                              + version + " is not supported");
    }

    services.setSystemInputManager(this);
  }

  public void init() {
  }

  public void click(Point location, MouseButton button, int numClicks,
                    List<ModifierPressed> modifiers) {
    clickDelayer.delayClickIfNeeded(location, button, numClicks);

    MouseInfo.Builder actionBuilder = MouseInfo.newBuilder();
    actionBuilder.setX(location.x);
    actionBuilder.setY(location.y);
    actionBuilder.setButton(button);
    actionBuilder.setNumClicks(numClicks);
    int modifier = ModifierPressed.NONE.getNumber();
    for (ModifierPressed mod : modifiers) {
      modifier |= mod.getNumber();
    }
    actionBuilder.setModifier(modifier);
    executeCommand(SystemInputCommand.CLICK, actionBuilder.clone());
  }

  public void mouseMove(Point location, MouseButton button,
                        List<ModifierPressed> modifiers) {
    MouseInfo.Builder actionBuilder = MouseInfo.newBuilder();
    actionBuilder.setX(location.x);
    actionBuilder.setY(location.y);
    actionBuilder.setButton(button);
    actionBuilder.setNumClicks(1);
    int modifier = ModifierPressed.NONE.getNumber();
    for (ModifierPressed mod : modifiers) {
      modifier |= mod.getNumber();
    }
    actionBuilder.setModifier(modifier);
    executeCommand(SystemInputCommand.MOUSEMOVE, actionBuilder.clone());
  }

  public void mouseUp(Point location, MouseButton button,
                      List<ModifierPressed> modifiers) {
    MouseInfo.Builder actionBuilder = MouseInfo.newBuilder();
    actionBuilder.setX(location.x);
    actionBuilder.setY(location.y);
    actionBuilder.setButton(button);
    actionBuilder.setNumClicks(1);
    int modifier = ModifierPressed.NONE.getNumber();
    for (ModifierPressed mod : modifiers) {
      modifier |= mod.getNumber();
    }
    actionBuilder.setModifier(modifier);
    executeCommand(SystemInputCommand.MOUSEUP, actionBuilder.clone());
  }

  public void mouseDown(Point location, MouseButton button,
                        List<ModifierPressed> modifiers) {
    MouseInfo.Builder actionBuilder = MouseInfo.newBuilder();
    actionBuilder.setX(location.x);
    actionBuilder.setY(location.y);
    actionBuilder.setButton(button);
    actionBuilder.setNumClicks(1);
    int modifier = ModifierPressed.NONE.getNumber();
    for (ModifierPressed mod : modifiers) {
      modifier |= mod.getNumber();
    }
    actionBuilder.setModifier(modifier);
    executeCommand(SystemInputCommand.MOUSEDOWN, actionBuilder.clone());
  }

  public void keyPress(String key, List<ModifierPressed> modifiers) {
    KeyPressInfo.Builder actionBuilder = KeyPressInfo.newBuilder();
    actionBuilder.setKey(key);
    int modifier = ModifierPressed.NONE.getNumber();
    for (ModifierPressed mod : modifiers) {
      modifier |= mod.getNumber();
    }
    actionBuilder.setModifier(modifier);
    executeCommand(SystemInputCommand.KEYPRESS, actionBuilder.clone());
  }

  public void keyDown(String key, List<ModifierPressed> modifiers) {
    KeyPressInfo.Builder actionBuilder = KeyPressInfo.newBuilder();
    actionBuilder.setKey(key);
    int modifier = ModifierPressed.NONE.getNumber();
    for (ModifierPressed mod : modifiers) {
      modifier |= mod.getNumber();
    }
    actionBuilder.setModifier(modifier);
    executeCommand(SystemInputCommand.KEYDOWN, actionBuilder.clone());
  }

  public void keyUp(String key, List<ModifierPressed> modifiers) {
    KeyPressInfo.Builder actionBuilder = KeyPressInfo.newBuilder();
    actionBuilder.setKey(key);
    int modifier = ModifierPressed.NONE.getNumber();
    for (ModifierPressed mod : modifiers) {
      modifier |= mod.getNumber();
    }
    actionBuilder.setModifier(modifier);
    executeCommand(SystemInputCommand.KEYUP, actionBuilder.clone());
  }
}

/**
 * DSK-348590 shows that two single clicks might be interpreted as a double click.
 * Since we can't really expect the test authors to put a delay after each single click
 * we delay a click if less time than constitutes the OS double click interval passed
 * since the last click.
 *
 * This may introduce new failures to tests that actually expect that two clicks will
 * make a double click, but that should be fixed by the test authors.
 */
class ClickDelayer
{
  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  protected long lastClickTime = 0;
  protected Point lastClickLocation = null;
  protected MouseButton lastClickMouseButton = null;

  public final void delayClickIfNeeded(Point location, MouseButton button, int numClicks) {
    if (lastClickTime > 0) {
      if (numClicks == 1) {
        if (button.equals(lastClickMouseButton)) {
          if (location.equals(lastClickLocation)) {
            long currentTime = System.currentTimeMillis();
            long doubleClickTime = WatirUtils.getSystemDoubleClickTimeMs();
            long timeSinceLastClick = currentTime - lastClickTime;
            long remainingTime = doubleClickTime - timeSinceLastClick;

            if (remainingTime > 0) {
              logger.fine(String.format("Delaying click in order to avoid double-click - check your test (last click was %d ms ago, OS double click timeout is %d ms)", timeSinceLastClick, doubleClickTime));
              try {
                // 100 ms chosen as a safe value for avoiding the double click, this may need adapting.
                Thread.sleep(remainingTime + 100);
              }
              catch (InterruptedException e) {
                logger.warning("*** Delay was interrupted ***");
              }
            }
          }
        }
      }
    }

    lastClickMouseButton = button;
    lastClickLocation = location;
    lastClickTime = System.currentTimeMillis();
  }
}
