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

import org.openqa.selenium.support.ui.Duration;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

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
  RESPONSE_TIMEOUT(new Duration(1, MINUTES)),

  /**
   * An implicit wait is to tell WebDriver to poll the DOM for a certain amount of time when trying
   * to find an element or elements if they are not immediately available.  The default setting is
   * 0.  Once set, the implicit wait is set for the life of the WebDriver object instance.
   */
  IMPLICIT_WAIT(new Duration(0, MILLISECONDS)),

  /**
   * The page load timeout specifies how long the driver waits for a page to finish loading before
   * returning the control to the user.
   */
  PAGE_LOAD_TIMEOUT(new Duration(30, SECONDS)),

  WINDOW_EVENT_TIMEOUT(new Duration(5, SECONDS)),
  OPERA_IDLE_TIMEOUT(new Duration(5, SECONDS)),
  POLL_INTERVAL(new Duration(10, MILLISECONDS)),

  /**
   * The handshake timeout defines how long the Scope server (OperaDriver) should wait for a
   * connection from a client (Opera) before shutting down.  If set to 0, it will wait
   * indefinitely.
   */
  HANDSHAKE_TIMEOUT(new Duration(1, MINUTES)),

  KILL_GRACE_TIMEOUT(new Duration(1, SECONDS)),

  DEFAULT_RESPONSE_TIMEOUT(new Duration(10, SECONDS)),

  QUIT_POLL_INTERVAL(new Duration(100, MILLISECONDS)),
  QUIT_RESPONSE_TIMEOUT(new Duration(10, SECONDS)),

  /**
   * Sets the amount of time to wait for an asynchronous script to finish execution before throwing
   * an error.  If the timeout is negative, then the script will be allowed to run indefinitely.
   */
  SCRIPT_TIMEOUT(new Duration(10, SECONDS)),

  /**
   * The interval at which an ECMAScript should be attempted reevaluated in the case of it for some
   * reason failing.  A script reevaluation will time out on {@link #SCRIPT_TIMEOUT}.
   */
  SCRIPT_RETRY_INTERVAL(new Duration(50, MILLISECONDS)),

  /**
   * After starting the launcher we need to wait for the launcher to connect to our listener.  If
   * the launcher does not connect within this timeout, we assume something has gone wrong.
   */
  LAUNCHER_CONNECT_TIMEOUT(new Duration(5, SECONDS)),

  /**
   * If anything goes wrong while connected to the launcher, don't block forever.
   */
  LAUNCHER_RESPONSE_TIMEOUT(new Duration(3, MINUTES)),

  /**
   * If programs such as the launcher exits immediately with an improper exit value (> 0) we can
   * assume something went wrong during initialization.  We need to wait for a short period before
   * checking the exit value as it may take some time to start the program.
   */
  PROCESS_START_SLEEP(new Duration(100, MILLISECONDS)),

  MENU_EVENT_TIMEOUT(new Duration(1, SECONDS)),

  /**
   * When clicking several times in a row (e.g. for a double, triple or quadruple click) 640 ms is
   * the hardcoded interval Opera must wait before loosing the previous click state, not joining
   * clicks together.
   *
   * This should be removed when support is added to the exec service for setValue() so that we can
   * tell Opera to override this interval.
   */
  MULTIPLE_CLICK_SLEEP(new Duration(640, MILLISECONDS)),

  /**
   * Different products have different animations when closing windows, and sometimes it might take
   * a little while.
   */
  WINDOW_CLOSE_SLEEP(new Duration(100, MILLISECONDS));

  private Duration duration;

  private OperaIntervals(Duration duration) {
    this.duration = duration;
  }

  public void setValue(Duration duration) {
    this.duration = duration;
  }

  public Duration getValue() {
    return duration;
  }

  /**
   * Converts the duration of the enum value as milliseconds.
   *
   * @return duration value in milliseconds
   */
  public long getMs() {
    return duration.in(MILLISECONDS);
  }

  public String toString() {
    return duration.toString();
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