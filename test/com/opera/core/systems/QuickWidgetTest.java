package com.opera.core.systems;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.settings.OperaDriverSettings;


public class QuickWidgetTest  extends TestBase {
  private static OperaDesktopDriver driver;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    OperaDriverSettings settings = new OperaDriverSettings();
    // TODO make work everywhere
    settings.setOperaBinaryLocation(binary);
    settings.setOperaLauncherBinary(launcher);

    driver = new OperaDesktopDriver(settings);
  }

  // FIXME shutdown driver
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

    @Test
    public void testSomething(){
    	driver.waitStart();
       // driver.operaDesktopAction("Open url in new page", "1");
    	driver.keyPress("t", ModifierPressed.CTRL);
    	System.out.println("Done keypress");
        //driver.waitForWindowShown("");
        //driver.close();

        int win_id = driver.waitForWindowShown("");
        System.out.println("Window shown = " + win_id);
        System.out.println("Window name = " + driver.getWindowName(win_id));
        List<QuickWindow> windows = driver.getWindowList();
        QuickWindow window = driver.getWindow("Document window");
        List<QuickWidget> list = driver.getQuickWidgetList("Document window");

    }
}
