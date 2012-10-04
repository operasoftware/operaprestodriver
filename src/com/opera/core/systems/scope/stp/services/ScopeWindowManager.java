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

package com.opera.core.systems.scope.stp.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.opera.core.systems.internal.ImplicitWait;
import com.opera.core.systems.internal.StackHashMap;
import com.opera.core.systems.internal.VersionUtil;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.ScopeServices;
import com.opera.core.systems.scope.exceptions.ScopeException;
import com.opera.core.systems.scope.exceptions.WindowNotFoundException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.WmProtos.CloseWindowArg;
import com.opera.core.systems.scope.protos.WmProtos.OpenURLArg;
import com.opera.core.systems.scope.protos.WmProtos.WindowFilter;
import com.opera.core.systems.scope.protos.WmProtos.WindowID;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowList;
import com.opera.core.systems.scope.services.WindowManager;
import com.opera.core.systems.scope.stp.services.messages.WindowManagerMessage;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.openqa.selenium.NoSuchWindowException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static com.opera.core.systems.scope.internal.OperaIntervals.INTERNAL_FREQUENCY;
import static com.opera.core.systems.scope.internal.OperaIntervals.WINDOW_CLOSE_TIMEOUT;

/**
 * The Window Manager service handles events such as opening a URL, filtering and closing windows,
 * and tracks windows being loaded.
 *
 * We want to be able to work with windows on a higher level than before.  One important goal is to
 * opt-in on the messages you get instead of being flooded.
 *
 * This protocol will prevent a lot of flooding from the other services, but it will on the other
 * hand flood a bit itself.  There is no way to stop it from sending OnWindowActivated messages for
 * example.
 */
public class ScopeWindowManager extends AbstractService implements WindowManager {

  public static final String CLOSE_ALL_PAGES_ACTION = "Close all pages";
  public static final CompiledExpression WINDOW_FINDER_EXPRESSION =
      JXPathContext.compile("/.[windowType='normal']/windowID");

  private final StackHashMap<Integer, WindowInfo> windows = new StackHashMap<Integer, WindowInfo>();
  private final AtomicInteger lastHttpResponseCode = new AtomicInteger();

  public ScopeWindowManager(ScopeServices services) {
    super(services, SERVICE_NAME);  // 2.1?
    services.setWindowManager(this);
  }

  public AtomicInteger getLastHttpResponseCode() {
    return lastHttpResponseCode;
  }

  public int getOpenWindowCount() {
    return windows.size();
  }

  public void setActiveWindowId(Integer windowId) {
    windows.putIfAbsent(windowId, null);
  }

  public int getActiveWindowId() {
    Integer windowID = windows.peekKey();
    if (windowID == null) {
      // If we closed all windows, it is possible that event has not fired yet, in such cases resort
      // to what Opera believes to be current active window.
      return findActiveWindow().getWindowID();
    }
    return windowID;
  }

  public void addWindow(WindowInfo info) {
    windows.put(info.getWindowID(), info);
  }

  public void removeWindow(Integer windowId) {
    windows.remove(windowId);
  }

  /**
   * Get the list of all windows, then find a driver window (which needs to be 'normal' window)
   * after that set the active window to that window.
   */
  public void init() {
    initializeWindows();
    findDriverWindow();
  }

  public void findDriverWindow() {
    JXPathContext pathContext = JXPathContext.newContext(windows.values());
    WindowInfo window = windows.peek();

    if (window == null || !window.getWindowType().equals("normal")) {
      Integer windowId = (Integer) WINDOW_FINDER_EXPRESSION.getValue(pathContext);

      if (windowId != null) {
        setActiveWindowId(windowId);
      } else {
        throw new WindowNotFoundException("No window is available for driving");
      }

      filterActiveWindow();
    }
  }

  private WindowID findActiveWindow() {
    // TODO(andreastt): CORE-47781 (Mobile takes a little while to initialize UI thread)
    if (services.getCore().getProduct().equalsIgnoreCase("mobile")) {
      sleep(100);
    }

    Response response = executeMessage(WindowManagerMessage.GET_ACTIVE_WINDOW, null);
    WindowID.Builder builder = WindowID.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

  /**
   * Filter only the active window so we don't get messages from other windows (like thread
   * messages).
   */
  public void filterActiveWindow() {
    WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
    filterBuilder.setClearFilter(true);
    filterBuilder.addIncludeIDList(getActiveWindowId());

    executeMessage(WindowManagerMessage.MODIFY_FILTER, filterBuilder);
  }

  /**
   * Set the filter to include all windows so we can get a list and maintain a list of windows.
   */
  private void initializeWindows() {
    clearFilter();

    Response response = executeMessage(WindowManagerMessage.LIST_WINDOWS, null);
    WindowList.Builder builder = WindowList.newBuilder();
    buildPayload(response, builder);
    WindowList list = builder.build();

    List<WindowInfo> windowsList = list.getWindowListList();

    windows.clear();
    for (WindowInfo window : windowsList) {
      windows.put(window.getWindowID(), window);
    }
    // initialize windowStack
    windows.putIfAbsent(findActiveWindow().getWindowID(), null);
  }

  public void setActiveWindow(String title) {
    Integer windowId = (Integer) xpathPointer(windows.values(),
                                              "/.[title='" + title + "']/windowID").getValue();
    if (windowId == null) {
      throw new NoSuchWindowException("No such window: " + title);
    }
    setActiveWindowId(windowId);
  }

  public void createWindow() {
    Response response = executeMessage(WindowManagerMessage.CREATE_WINDOW, null);
    WindowID.Builder builder = WindowID.newBuilder();
    buildPayload(response, builder);
    openUrl(builder.build().getWindowID(), "opera:debug");
  }

  public List<Integer> getWindowHandles() {
    return ImmutableList.copyOf(windows.asStack());  // handles
  }

  public void resetWindowsList() {
    initializeWindows();
  }

  public void clearFilter() {
    WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
    filterBuilder.setClearFilter(true);
    filterBuilder.addIncludePatternList("*");
    executeMessage(WindowManagerMessage.MODIFY_FILTER, filterBuilder);
  }

  public void filterWindow(Integer windowId) {
    WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
    filterBuilder.setClearFilter(true);
    filterBuilder.addIncludeIDList(windowId);
    executeMessage(WindowManagerMessage.MODIFY_FILTER, filterBuilder);
  }

  public void openUrl(int windowId, String url) {
    // Opera Mobile still uses WM 2.0
    if (VersionUtil.compare(getServiceVersion(), "2.1") < 0) {
      services.getExec().action("Go", url);
      return;
    }

    OpenURLArg.Builder openUrlBuilder = OpenURLArg.newBuilder();
    openUrlBuilder.setWindowID(windowId);
    openUrlBuilder.setUrl(url);

    Response response = executeMessage(WindowManagerMessage.OPEN_URL, openUrlBuilder);

    if (response == null) {
      throw new ScopeException("Internal error while opening " + url);
    }
  }

  public void closeWindow(final int windowId) {
    assertSupportsClosingWindows();

    CloseWindowArg.Builder closeWindowBuilder = CloseWindowArg.newBuilder();
    closeWindowBuilder.setWindowID(windowId);

    Response response = executeMessage(WindowManagerMessage.CLOSE_WINDOW, closeWindowBuilder);

    new ImplicitWait(WINDOW_CLOSE_TIMEOUT.getValue(), INTERNAL_FREQUENCY.getValue())
        .until(new Callable<Boolean>() {
          public Boolean call() {
            return !windows.containsKey(windowId);
          }
        });

    if (response == null) {
      throw new ScopeException("Internal error while closing window");
    }
  }

  public void closeAllWindows() {
    int toBeClosed = windows.size();

    if (supportsClosingWindows()) {
      LinkedList<Integer> list = Lists.newLinkedList(windows.asStack());
      while (!list.isEmpty()) {
        closeWindow(list.removeFirst());
      }
    } else if (services.getExec().getActionList().contains(CLOSE_ALL_PAGES_ACTION)) {
      services.getExec().action(CLOSE_ALL_PAGES_ACTION);
      sleep(OperaIntervals.WINDOW_CLOSE_USING_ACTION_SLEEP.getMs() * toBeClosed);
    } else {
      // This is a different type of exception than the one in closeWindow(i)
      throw new UnsupportedOperationException("Product does not support closing windows");
    }
  }

  // SDK's WM doesn't work as expected
  private boolean supportsClosingWindows() {
    return !services.getCore().getProduct().equals("devices") &&
           VersionUtil.compare(getServiceVersion(), "2.1") >= 0;

  }

  private void assertSupportsClosingWindows() {
    if (!supportsClosingWindows()) {
      throw new UnsupportedOperationException(String.format(
          "Window Manager version %s does not support closing windows", getServiceVersion()));
    }
  }

}