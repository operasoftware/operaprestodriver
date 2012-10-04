/*
Copyright 2012 Opera Software ASA

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

package com.opera.core.systems.testing.drivers;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.OperaSettings;
import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.services.Exec;

/**
 * Provides access to the {@link com.opera.core.systems.runner.AbstractOperaRunner}, so we can
 * detect crashes.
 */
public class TestOperaDriver extends OperaDriver implements TestDriver {

  /**
   * Creates a new TestOperaDriver with the given settings.
   *
   * @param settings the settings to use
   */
  public TestOperaDriver(OperaSettings settings) {
    super(settings);
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

  public Exec getExecService() {
    return getServices().getExec();
  }

  /**
   * Determines if Opera is running or not.  If Opera is not run locally using {@link
   * com.opera.core.systems.runner.AbstractOperaRunner} it will check for a Scope connection
   * instead.
   *
   * @return true if Opera is running, false otherwise
   */
  public boolean isRunning() {
    return runner != null ? runner.isOperaRunning() : getScopeServices().isConnected();
  }

  public void waitForLoadToComplete() throws ResponseNotReceivedException {
    super.waitForLoadToComplete();
  }

}