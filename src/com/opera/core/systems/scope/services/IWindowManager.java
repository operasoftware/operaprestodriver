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

package com.opera.core.systems.scope.services;

import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;

import org.openqa.selenium.WebDriverException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface IWindowManager {

  /**
   * Sets the active window if the window is of type 'normal' discards any other window such as
   * 'dialog' or 'unknown'.
   */
  void setActiveWindowId(Integer windowId);

  int getActiveWindowId();

  void addWindow(WindowInfo window);

  void removeWindow(Integer windowId);

  void filterActiveWindow();

  void closeAllWindows();

  /**
   * Get the list of all windows, then find a driver window (which needs to be 'normal' window)
   * after that set the active window to that window.
   */
  void init();

  /**
   * Find a window suitable for driving, find the active window, if it is not a normal window, find
   * the first normal window via JXPath.
   *
   * @throws WebDriverException if no window is available for driving
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

  void closeWindow(int windowId);

}