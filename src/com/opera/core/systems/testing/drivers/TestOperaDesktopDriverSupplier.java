package com.opera.core.systems.testing.drivers;

import com.opera.core.systems.OperaSettings;

import java.util.logging.Level;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class TestOperaDesktopDriverSupplier extends DefaultOperaDriverSupplier {

  public TestOperaDesktopDriver get() {
    // Only override logging level if it has the default value
    if (new OperaSettings().logging().getLevel() == settings.logging().getLevel()) {
      settings.logging().setLevel(Level.FINE);
    }

    //settings.autostart(false);

    return new TestOperaDesktopDriver(settings);
  }

}