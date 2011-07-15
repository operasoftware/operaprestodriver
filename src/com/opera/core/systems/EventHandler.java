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
package com.opera.core.systems;

import com.opera.core.systems.scope.handlers.AbstractEventHandler;
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
 * Provides an interface to the Scope protocol.
 *
 * @author Deniz Turkoglu
 *
 */
public class EventHandler extends AbstractEventHandler {

  protected ScopeServices services;

  public EventHandler(ScopeServices services) {
    super(services);
    this.services = services;
  }

  /**
   * Changes the window-manager's active window on active window event
   */
  @Override
  public void onActiveWindow(Integer id) {
    services.getWindowManager().setActiveWindowId(id);
  }

  /**
   * Remove any runtime that has been stopped. This call can be quite late and
   * hence is resolved by cleanup method in onRuntimeStarted
   */
  @Override
  public void onRuntimeStopped(Integer id) {
    services.getDebugger().removeRuntime(id);
    // FIXME this event is quite buggy, ignore
    /*
    if(stopped.getRuntimeId() == services.getDebugger().getRuntimeId())
      services.getDebugger().setRuntimeId(0);
    */
  }

  @Override
  public void onRequest(int windowId) {
    services.onRequest(windowId);
  }

  /**
   * Handles windows that have been closed. Removes it from the list and removes
   * the runtimes that are associated with it
   */
  @Override
  public void onWindowClosed(Integer id) {
    services.onWindowClosed(id);
    services.getWindowManager().removeWindow(id);
    services.getDebugger().cleanUpRuntimes(id);
  }

  /**
   * Fired when a window is loaded
   *
   * @param windowId ID of the window that fired the event
   */
  @Override
  public void onWindowLoaded(int windowId) {
    services.getDebugger().cleanUpRuntimes(windowId);
    services.onWindowLoaded(windowId);
  }

  /**
   * Fired when opera is idle
   */
  @Override
  public void onOperaIdle() {
    services.onOperaIdle();
  }

  @Override
  public void onRuntimeStarted(RuntimeInfo started) {
    throw new UnsupportedOperationException("Not supported in STP/0");
  }

  @Override
  public void onUpdatedWindow(WindowInfo window) {
    throw new UnsupportedOperationException("Not supported in STP/0");
  }

  @Override
  public void onMessage(ConsoleMessage message) {
    throw new UnsupportedOperationException("Not supported in STP/0");
  }

  @Override
  public void onHttpResponse(int responseCode) {
    services.getWindowManager().getLastHttpResponseCode().compareAndSet(0,
        responseCode);
  }

  @Override
  public void onReadyStateChange(ReadyStateChange change) {
    throw new UnsupportedOperationException("Not supported in STP/0");
  }

  @Override
  public void onDesktopWindowShown(DesktopWindowInfo info) {
    services.onDesktopWindowShown(info);
  }

  @Override
  public void onDesktopWindowUpdated(DesktopWindowInfo info) {
    services.onDesktopWindowUpdated(info);
  }

  @Override
  public void onDesktopWindowActivated(DesktopWindowInfo info) {
    services.onDesktopWindowActivated(info);
  }

  @Override
  public void onDesktopWindowClosed(DesktopWindowInfo info) {
    services.onDesktopWindowClosed(info);
  }

  @Override
  public void onDesktopWindowLoaded(DesktopWindowInfo info) {
    services.onDesktopWindowLoaded(info);
  }

  @Override
  public void onQuickMenuShown(QuickMenuInfo menuInfoShown) {
	  services.onQuickMenuShown(menuInfoShown);
  }

  @Override
  public void onQuickMenuClosed(QuickMenuID menuId) {
	  services.onQuickMenuClosed(menuId);
	
  }
  
  @Override
  public void onQuickMenuItemPressed(QuickMenuItemID menuItemID) {
	  services.onQuickMenuItemPressed(menuItemID);
	
  }
 
  public void onSelftestOutput(SelftestOutput output) {
    services.onSelftestOutput(output);
  }

  @Override
  public void onSelftestDone() {
    services.onSelftestDone();
  }
}
