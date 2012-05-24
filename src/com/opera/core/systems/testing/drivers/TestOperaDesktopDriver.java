package com.opera.core.systems.testing.drivers;

import com.opera.core.systems.OperaDesktopDriver;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.runner.interfaces.OperaRunner;

public class TestOperaDesktopDriver extends OperaDesktopDriver {

  public TestOperaDesktopDriver(OperaSettings settings) {
    super(settings);
  }

  public OperaRunner getRunner() {
    return runner;
  }

  public OperaSettings getSettings() {
    return settings;
  }

}