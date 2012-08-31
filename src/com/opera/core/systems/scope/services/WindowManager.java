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

package com.opera.core.systems.scope.services;

import com.opera.core.systems.scope.Service;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface WindowManager extends Service {

  /**
   * Sets the active window if the window is of type 'normal' discards any other window such as
   * 'dialog' or 'unknown'.
   */
  void setActiveWindowId(Integer windowId);

  int getActiveWindowId();

  void addWindow(WindowInfo window);

  void removeWindow(Integer windowId);

  void filterActiveWindow();

  /**
   * Find a window suitable for driving, find the active window, if it is not a normal window, find
   * the first normal window via JXPath.
   */
  void findDriverWindow();

  void setActiveWindow(String title);

  List<Integer> getWindowHandles();

  int getOpenWindowCount();

  AtomicInteger getLastHttpResponseCode();

  void resetWindowsList();

  void clearFilter();

  void filterWindow(Integer windowId);

  void openUrl(int windowId, String url);

  void createWindow();

  /**
   * Requests the closing of a specific window.  Window closing is asynchronous and your request may
   * be silently ignored or denied.  If the window actually was closed, then an OnWindowClosed event
   * will fire.  Attempting to close a non-existent window will trigger an error.
   *
   * Closes the specified window, including dialogues, by using Scope's CloseWindow command if
   * version 2.1 or greater of the Window Manager service is available.
   *
   * This method is blocking and will not return until the window has been closed unless it hits the
   * {@link com.opera.core.systems.scope.internal.OperaIntervals#WINDOW_CLOSE_TIMEOUT} timeout.
   *
   * @throws UnsupportedOperationException if window manager version is lower than 2.1
   */
  void closeWindow(int windowId);


  /**
   * Closes all open windows, including dialogues, by iterating through them and calling {@link
   * #closeWindow(int)} on each of them if version 2.1 or greater of the Window Manager service is
   * available.  This method deviates from {@link #closeWindow(int)} and will if not attempt to use
   * Opera's action system to close the windows and dialogues.
   *
   * This method is normally blocking, and will not return until the window has been closed or until
   * it hits the {@link com.opera.core.systems.scope.internal.OperaIntervals#WINDOW_CLOSE_TIMEOUT}
   * timeout.  However, note that using the action system is slower than Scope, and that it is also
   * notoriously unreliable.  We cannot guarantee that this method will be blocking if the action
   * mechanism is used.
   *
   * @throws UnsupportedOperationException if no suitable mechanisms for closing windows exists in
   *                                       the current product
   */
  void closeAllWindows();

}