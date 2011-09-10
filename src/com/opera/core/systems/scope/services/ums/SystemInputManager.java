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
package com.opera.core.systems.scope.services.ums;

import java.awt.Point;
import java.util.List;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.SystemInputCommand;
import com.opera.core.systems.scope.protos.SystemInputProtos.KeyPressInfo;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo.MouseButton;
import com.opera.core.systems.scope.protos.SystemInputProtos.MouseInfo;
import com.opera.core.systems.scope.services.ISystemInput;

/**
 * SystemInputManager handles systemInput simulating real clicks, and key presses.
 *
 * @author Adam Minchinton, Karianne Ekern
 */
public class SystemInputManager extends AbstractService implements ISystemInput {

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
