package com.opera.core.systems.testing.drivers;

import com.opera.core.systems.OperaDesktopDriver;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.runner.interfaces.OperaRunner;

public class TestOperaDesktopDriver extends OperaDesktopDriver implements TestDriver {

  public TestOperaDesktopDriver(OperaSettings settings) {
    super(settings);
  }

  /**
   * Determines if Opera is running or not.  If Opera is not run locally using {@link com.opera.core.systems.runner.OperaRunner}
   * it will check for a Scope connection instead.
   *
   * @return true if Opera is running, false otherwise
   */
  public boolean isRunning() {
    return runner != null ? runner.isOperaRunning() : getScopeServices().isConnected();
  }

  public OperaRunner getRunner() {
    return runner;
  }

  public OperaSettings getSettings() {
    return settings;
  }

  public ScopeServices getServices() {
    return getScopeServices();
  }

}