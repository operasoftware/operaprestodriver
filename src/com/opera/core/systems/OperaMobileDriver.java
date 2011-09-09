package com.opera.core.systems;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.services.ISystemInput;

public class OperaMobileDriver extends OperaDriver {
  private ISystemInput systemInputManager;

  public OperaMobileDriver() {
    super();
  }

  public OperaMobileDriver(DesiredCapabilities c) {
    super(c);
  }

  @Override
  protected void init() {
    super.init();
    System.out.println(services.getVersions());
    systemInputManager = services.getSystemInputManager();
  }

  @Override
  protected Map<String, String> getServicesList() {
    Map<String, String> versions = super.getServicesList();
    versions.put("system-input", "1.0");
    return versions;
  }

  /**
   * Press Key with modifiers held down.
   *
   * @param key key to press
   * @param modifiers modifiers held
   */
  public void keyPress(String key) {
    systemInputManager.keyPress(key, null);
  }

  /**
   * Release key.
   *
   * @param key key to press
   * @param modifiers modifiers held
   */
  public void keyUp(String key) {
    systemInputManager.keyUp(key, null);
  }

  /**
   * Press Key.
   *
   * @param key key to press
   * @param modifiers modifiers held
   */
  public void keyDown(String key) {
    System.out.println("mobile keydown");
    System.out.println(systemInputManager);
    systemInputManager.keyDown(key, null);
  }
}
