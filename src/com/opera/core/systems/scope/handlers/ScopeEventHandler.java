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

import com.opera.core.systems.ScopeServices;
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
 * Provides an event handler to the Scope protocol.
 */
public class ScopeEventHandler implements EventHandler {

  protected ScopeServices services;

  public ScopeEventHandler(ScopeServices services) {
    this.services = services;
  }

  /**
   * Remove any runtime that has been stopped.  This call can be quite late and hence is resolved by
   * cleanup method in onRuntimeStarted.
   */
  public void onRuntimeStopped(Integer id) {
    services.getDebugger().removeRuntime(id);
  }

  public void onRequest(int windowId) {
    services.onRequest(windowId);
  }

  /**
   * Changes the window-manager's active window on active window event.
   */
  public void onWindowActivated(Integer id) {
    services.getWindowManager().setActiveWindowId(id);
  }

  /**
   * Handles windows that have been closed.  Removes it from the list and removes the runtimes that
   * are associated with it.
   */
  public void onWindowClosed(Integer id) {
    services.onWindowClosed(id);
    services.getWindowManager().removeWindow(id);
    services.getDebugger().cleanUpRuntimes(id);
  }

  public void onWindowLoaded(int windowId) {
    services.getDebugger().cleanUpRuntimes(windowId);
    services.onWindowLoaded(windowId);
  }

  public void onOperaIdle() {
    services.onOperaIdle();
  }

  public void onRuntimeStarted(RuntimeInfo info) {
    services.getDebugger().addRuntime(info);
  }

  public void onWindowUpdated(WindowInfo info) {
    services.getWindowManager().addWindow(info);
  }

  public void onConsoleMessage(ConsoleMessage message) {
    services.getConsoleLogger().addMessage(message);
  }

  public void onHttpResponse(int responseCode) {
    services.getWindowManager().getLastHttpResponseCode().compareAndSet(0, responseCode);
  }

  public void onReadyStateChange(ReadyStateChange change) {
    services.getDebugger().readyStateChanged(change);
  }

  public void onDesktopWindowShown(DesktopWindowInfo info) {
    services.onDesktopWindowShown(info);
  }

  public void onDesktopWindowUpdated(DesktopWindowInfo info) {
    services.onDesktopWindowUpdated(info);
  }

  public void onDesktopWindowActivated(DesktopWindowInfo info) {
    services.onDesktopWindowActivated(info);
  }

  public void onDesktopWindowClosed(DesktopWindowInfo info) {
    services.onDesktopWindowClosed(info);
  }

  public void onDesktopWindowLoaded(DesktopWindowInfo info) {
    services.onDesktopWindowLoaded(info);
  }

  public void onDesktopWindowPageChanged(DesktopWindowInfo info) {
    services.onDesktopWindowPageChanged(info);
  }

  public void onQuickMenuShown(QuickMenuInfo menuInfoShown) {
    services.onQuickMenuShown(menuInfoShown);
  }

  public void onQuickMenuClosed(QuickMenuID menuId) {
    services.onQuickMenuClosed(menuId);
  }

  public void onQuickMenuItemPressed(QuickMenuItemID menuItemID) {
    services.onQuickMenuItemPressed(menuItemID);
  }

  public void onSelftestOutput(SelftestOutput output) {
    services.onSelftestOutput(output);
  }

  public void onSelftestDone() {
    services.onSelftestDone();
  }

}
