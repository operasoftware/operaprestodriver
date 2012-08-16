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

package com.opera.core.systems.scope.handlers;

import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemID;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.SelftestProtos.SelftestOutput;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;

/**
 * Event handler for Scope events and network exceptions.
 */
public interface EventHandler {

  /**
   * Fired when a new runtime started is received Runtime-started is needed for tracking EcmaScript
   * injections.
   *
   * @param started information on the runtime that was started
   */
  void onRuntimeStarted(RuntimeInfo started);

  /**
   * Fired when a runtime is stopped and no longer injectable.
   *
   * @param id ID of the runtime is stopped
   */
  void onRuntimeStopped(Integer id);

  /**
   * Fired on new console messages.
   *
   * @param message the incoming message
   */
  void onConsoleMessage(ConsoleMessage message);

  /**
   * Fired when a new window is created or window has incoming changes (such as title change).
   *
   * @param window information about the window
   */
  void onWindowUpdated(WindowInfo window);

  /**
   * Fired when a window becomes active (steals focus).
   *
   * @param id ID of the window that becomes active
   */
  void onWindowActivated(Integer id);

  /**
   * Fired when a window instance is closed.
   *
   * @param id ID of the closed window
   */
  void onWindowClosed(Integer id);

  /**
   * Fired when a window load is complete.
   *
   * @param windowId ID of the window that is loaded
   */
  void onWindowLoaded(int windowId);

  /**
   * Fired when a desktop window is shown at the last possible moment so the window should be fully
   * visible.
   *
   * @param window information about the window
   */
  void onDesktopWindowShown(DesktopWindowInfo window);

  /**
   * Fired when Opera is idle.
   */
  void onOperaIdle();

  /**
   * Fired when a new window is created or window has incoming changes (such as title change).
   *
   * @param window information about the window that is updated
   */
  void onDesktopWindowUpdated(DesktopWindowInfo window);

  /**
   * Fired when a window becomes active (steals focus).
   *
   * @param window information about the window that is activated
   */
  void onDesktopWindowActivated(DesktopWindowInfo window);

  /**
   * Fired when a window instance is closed.
   *
   * @param window information about the window that is closed
   */
  void onDesktopWindowClosed(DesktopWindowInfo window);

  /**
   * Fired when loading Finished event.
   *
   * @param window information about the window that is loaded
   */
  void onDesktopWindowLoaded(DesktopWindowInfo window);

  void onDesktopWindowPageChanged(DesktopWindowInfo window);

  /**
   * Fired when menu shown.
   *
   * @param menuInfoShown menu information of the recently appeared menu
   */
  void onQuickMenuShown(QuickMenuInfo menuInfoShown);

  /**
   * Fired when menu closed.
   *
   * @param menuId ID of the recently closed menu
   */
  void onQuickMenuClosed(QuickMenuID menuId);

  /**
   * Fired when menu item is pressed.
   *
   * @param menuItemID ID of the menu item that was pressed
   */
  void onQuickMenuItemPressed(QuickMenuItemID menuItemID);

  /**
   * Fired when receiving the output of a selftest.
   *
   * @param output the output of the selftest
   */
  void onSelftestOutput(SelftestOutput output);

  /**
   * Fired when all selftests are done.
   */
  void onSelftestDone();

  /**
   * Fired when an HTTP response code is received.
   *
   * @param responseCode the response code
   */
  @SuppressWarnings("unused")
  void onHttpResponse(int responseCode);

  /**
   * Fired when the ready state of a document is communicated.
   *
   * @param change the ready state change
   */
  void onReadyStateChange(ReadyStateChange change);

  void onRequest(int windowId);

}