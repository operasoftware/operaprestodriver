package com.opera.core.systems;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;

public class QuickWidgetTest extends DesktopTestBase {
  @Test
  public void testSomething() {
    driver.waitStart();

    List<ModifierPressed> modifiers = new ArrayList<ModifierPressed>();
    modifiers.add(ModifierPressed.CTRL);

    driver.keyPress("t", modifiers);
    System.out.println("Done keypress");
    // driver.waitForWindowShown("");
    // driver.close();

    int win_id = driver.waitForWindowShown("");
    System.out.println("Window shown = " + win_id);
    System.out.println("Window name = " + driver.getWindowName(win_id));

    // FIXME add asserts

    List<QuickWindow> windows = driver.getWindowList();
    QuickWindow window = driver.getWindow("Document window");
    List<QuickWidget> list = driver.getQuickWidgetList("Document window");

  }
}
