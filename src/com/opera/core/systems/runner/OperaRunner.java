/*
Copyright 2008-2012 Opera Software ASA

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

import com.opera.core.systems.model.ScreenCaptureReply;

/**
 * AbstractOperaRunner is an interface for controlling the Opera browser binary.  It allows you to
 * start, stop, take screenshots of, and catch crash logs from Opera.  AbstractOperaRunner
 * implements a pure-Java process manager, OperaLauncherRunner implements one in C++ with a Java
 * API.
 */
public interface OperaRunner {

  /**
   * Start Opera, does nothing if Opera is already started.
   *
   * @throws OperaRunnerException if runner has been shut down
   */
  void startOpera() throws OperaRunnerException;

  /**
   * Stops Opera, does nothing if Opera isn't running.  Typically we'll use the exec service to stop
   * Opera, but this method will _ensure_ that Opera is quit from an external process controller.
   *
   * @throws OperaRunnerException if runner has been shut down
   */
  void stopOpera() throws OperaRunnerException;

  /**
   * Is Opera running?
   *
   * @return true if running, false if not running or crashed
   */
  boolean isOperaRunning();

  /**
   * Did Opera crash?  This is reset on next call, so if you don't check you won't know.
   *
   * @return true if opera has crashed
   */
  boolean hasOperaCrashed();

  /**
   * If Opera crashed there should be a crashlog.
   *
   * @return the crashlog, or null if it does not exist
   */
  String getOperaCrashlog();

  /**
   * In case the com.opera.core.systems.runner.runner has open sockets, they need to be closed.
   */
  void shutdown();

  /**
   * Take a screenshot of the full screen.
   *
   * @return a reply with various information on the captured area
   * @throws OperaRunnerException if a communication error occurs
   */
  ScreenCaptureReply captureScreen() throws OperaRunnerException;

  /**
   * Take a screenshot of the full screen with a timeout.
   *
   * @param timeout attempt to take the screen capture until the timeout is reached
   * @return a reply with various information on the captured area
   * @throws OperaRunnerException if a communication error occurs
   */
  ScreenCaptureReply captureScreen(long timeout) throws OperaRunnerException;

  /**
   * Take a screenshot of the full screen.
   *
   * @param timeout attempt to take the screenshot until the timeout is reached
   * @param hashes  an arbitrary list of hashes to compare with
   * @return an object containing MD5 hash sums and bytes
   * @throws OperaRunnerException if launcher is shutdown or not running
   */

  /**
   * Take a screenshot of the full screen with a timeout and a list of previously known MD5 hash sum
   * results.  If the screen capture matches one of the MD5 hash sums, the capture's image data will
   * not be stored.
   *
   * @param timeout   attempt to take the screen capture until the timeout is reached
   * @param knownMD5s an arbitrary list of hashes of known MD5's, causing no image data to be
   *                  stored
   * @return a reply with various information on the captured area
   * @throws OperaRunnerException if a communication error occurs
   */
  ScreenCaptureReply captureScreen(long timeout, String... knownMD5s) throws OperaRunnerException;

}