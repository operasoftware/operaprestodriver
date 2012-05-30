package com.opera.core.systems.testing.drivers;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.runner.interfaces.OperaRunner;

import org.openqa.selenium.WebDriver;

public interface TestDriver extends WebDriver {

  boolean isRunning();

  OperaRunner getRunner();

  OperaSettings getSettings();

  ScopeServices getServices();

  // TODO(andreastt): Create interface
  OperaDriver.OperaUtils utils();

}