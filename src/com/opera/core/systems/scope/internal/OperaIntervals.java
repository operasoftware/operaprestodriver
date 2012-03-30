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

package com.opera.core.systems.scope.internal;

/**
 * Enumerator for delay values in milliseconds.
 */
public enum OperaIntervals {

  /**
   * The response timeout specifies for how long OperaDriver should wait before timing out when it
   * has not received a callback confirmation message from Scope after executing a command.  This
   * typically happens when it looses connection to Opera or when a triggered command takes too long
   * to finish.
   */
  RESPONSE_TIMEOUT(60000),

  /**
   * An implicit wait is to tell WebDriver to poll the DOM for a certain amount of time when trying
   * to find an element or elements if they are not immediately available.  The default setting is
   * 0.  Once set, the implicit wait is set for the life of the WebDriver object instance.
   */
  IMPLICIT_WAIT(0),

  /**
   * The page load timeout specifies how long the driver waits for a page to finish loading before
   * returning the control to the user.
   */
  PAGE_LOAD_TIMEOUT(30000),

  WINDOW_EVENT_TIMEOUT(5000),
  OPERA_IDLE_TIMEOUT(5000),
  POLL_INTERVAL(10),
  HANDSHAKE_TIMEOUT(30000),

  /**
   * The default Opera debug proxy server port for OperaDriver to connect to.
   */
  SERVER_PORT(7001),

  /**
   * For backwards compatibility with Operas without <code>-debugproxy</code> support, if the user
   * specifies 0 as the port it will use a random port.
   */
  SERVER_RANDOM_PORT_IDENTIFIER(0),

  /**
   * For backwards compatibility with Operas without <code>-debugproxy</code> support, if the user
   * specifies -1 as the port it will use the default server proxy port specified in
   * {@link #SERVER_PORT}.
   */
  SERVER_DEFAULT_PORT_IDENTIFIER(-1),


  KILL_GRACE_TIMEOUT(1000),
  BACKWARDS_COMPATIBLE(1),
  DEFAULT_RESPONSE_TIMEOUT(10000),

  QUIT_POLL_INTERVAL(100),
  QUIT_RESPONSE_TIMEOUT(10000),

  /**
   * Sets the amount of time to wait for an asynchronous script to finish execution before throwing
   * an error.  If the timeout is negative, then the script will be allowed to run indefinitely.
   */
  SCRIPT_TIMEOUT(10000),

  /**
   * The interval at which an ECMAScript should be attempted reevaluated in the case of it for some
   * reason failing.  A script reevaluation will time out on {@link #SCRIPT_TIMEOUT}.
   */
  SCRIPT_RETRY_INTERVAL(50),

  /**
   * After starting the launcher we need to wait for the launcher to connect to our listener.  If
   * the launcher does not connect within this timeout, we assume something has gone wrong.
   */
  LAUNCHER_TIMEOUT(5000),

  /**
   * If programs such as the launcher exits immediately with an improper exit value (> 0) we can
   * assume something went wrong during initialization.  We need to wait for a short period before
   * checking the exit value as it may take some time to start the program.
   */
  PROCESS_START_SLEEP(10),

  MENU_EVENT_TIMEOUT(1000),

  /**
   * When clicking several times in a row (e.g. for a double, triple or quadruple click) 640 ms is
   * the hardcoded interval Opera must wait before loosing the previous click state, not joining
   * clicks together.
   *
   * This should be removed when support is added to the exec service for setValue() so that we can
   * tell Opera to override this interval.
   */
  MULTIPLE_CLICK_SLEEP(640);

  private long value;

  private OperaIntervals(long value) {
    this.value = value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public long getValue() {
    return value;
  }

  public String toString() {
    return String.format("%s: %d", super.toString(), getValue());
  }

  @SuppressWarnings("unused")
  public static String toList() {
    StringBuilder intervals = new StringBuilder();
    for (OperaIntervals item : values()) {
      if (intervals.length() != 0) {
        intervals.append(", ");
      }
      intervals.append(item.toString());
    }
    return String.format("Intervals [%s]", intervals.toString());
  }

}