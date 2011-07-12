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

package com.opera.core.systems.runner;

import com.opera.core.systems.model.ScreenShotReply;

/**
 * Defines interface for controlling the Opera binary.
 */
public interface OperaRunner {

  /**
   * Start Opera, does nothing if Opera is already started.
   */
  public void startOpera();

  /**
   * Stops Opera, does nothing if opera isn't running.
   */
  public void stopOpera();

  /**
   * Is Opera running?
   *
   * @return true if running, false if not running or crashed.
   */
  public boolean isOperaRunning();

  public boolean isOperaRunning(int processId);

  /**
   * Did Opera crash?  This is reset on next call, so if you don't check
   * you won't know.
   *
   * @return true if opera has crashed.
   */
  public boolean hasOperaCrashed();

  /**
   * If Opera crashed there should be a crashlog.
   *
   * @return the crashlog, or null if it does not exist.
   */
  public String getOperaCrashlog();

  /**
   * In case the runner has sockets and stuff, it needs to be shut down.
   */
  public void shutdown();

  /**
   * Take a screenshot of the full screen.
   *
   * @param timeout attempt to take the screenshot until the timeout is reached.
   * @param hashes an arbitrary list of hashes to compare with.
   * @return a ScreenshotReply object containing MD5 hash sums and bytes.
   */
  public ScreenShotReply saveScreenshot(long timeout, String... hashes);
}
