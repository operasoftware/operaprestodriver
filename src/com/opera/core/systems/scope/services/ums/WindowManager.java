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
package com.opera.core.systems.scope.services.ums;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.WindowManagerCommand;
import com.opera.core.systems.scope.exceptions.WindowNotFoundException;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.WmProtos.CloseWindowArg;
import com.opera.core.systems.scope.protos.WmProtos.OpenURLArg;
import com.opera.core.systems.scope.protos.WmProtos.WindowFilter;
import com.opera.core.systems.scope.protos.WmProtos.WindowID;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowList;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.util.StackHashMap;

/**
 * window-manager service implementation, handles events such as window-closed
 * and updated and tracks window being loaded
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 */
public class WindowManager extends AbstractService implements IWindowManager {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private final StackHashMap<Integer, WindowInfo> windows = new StackHashMap<Integer, WindowInfo>();
  private final CompiledExpression windowFinder;

  private final AtomicInteger lastHttpResponseCode = new AtomicInteger();

  public AtomicInteger getLastHttpResponseCode() {
    return lastHttpResponseCode;
  }

  public WindowManager(ScopeServices services, String version) {
    super(services, version);

    String serviceName = "window-manager";

    if (!isVersionInRange(version, "3.0", serviceName)) throw new UnsupportedOperationException(
        serviceName + " version " + version + " is not supported");

    services.setWindowManager(this);
    windowFinder = JXPathContext.compile("/.[windowType='normal']/windowID");
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
      // if we closed all windows, it is possible that event
      // has not fired yet, in such cases resort to what Opera
      // believes to be current active window
      return findActiveWindow().getWindowID();
    }
    return windowID.intValue();
  }

  public void addWindow(WindowInfo info) {
    windows.put(info.getWindowID(), info);
  }

  public void removeWindow(Integer windowId) {
    windows.remove(windowId);
  }

  public void init() {
    initializeWindows();
    findDriverWindow();
  }

  public void findDriverWindow() {
    JXPathContext pathContext = JXPathContext.newContext(windows.values());
    WindowInfo window = windows.peek();

    if (window == null || !window.getWindowType().equals("normal")) {
      // we dont deal with anything else, at least for now
      // select a window that is normal and return first

      // Fix for Windows OS, we dont encounter this problem on linux at all
      /*
      if(windowsStack.isEmpty())
        throw new WebDriverException("List of windows is empty");
      */

      Integer windowId = (Integer) windowFinder.getValue(pathContext);

      if (windowId != null) {
        setActiveWindowId(windowId);
      } else {
        throw new WindowNotFoundException("No window is available for driving");
      }
      filterActiveWindow();
    }

  }

  // NOTE: This is proven to be not working on Opera side...
  private WindowID findActiveWindow() {
    Response response = executeCommand(WindowManagerCommand.GET_ACTIVE_WINDOW,
        null);
    WindowID.Builder builder = WindowID.newBuilder();
    buildPayload(response, builder);
    return builder.build();
  }

  /**
   * Filter only the active window so we don't get messages from other windows
   * (like thread messages)
   */
  public void filterActiveWindow() {
    WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
    filterBuilder.setClearFilter(true);
    filterBuilder.addIncludeIDList(getActiveWindowId());

    executeCommand(WindowManagerCommand.MODIFY_FILTER, filterBuilder);
  }

  /**
   * Set the filter to include all windows so we can get a list and maintain a
   * list of windows
   */
  private void initializeWindows() {
    clearFilter();

    Response response = executeCommand(WindowManagerCommand.LIST_WINDOWS, null);
    WindowList.Builder builder = WindowList.newBuilder();
    buildPayload(response, builder);
    WindowList list = builder.build();

    List<WindowInfo> windowsList = list.getWindowListList();

    windows.clear();
    for (WindowInfo window : windowsList) {
      // FIXME workaround for CORE-25866
      // if(window.getTitle().length() > 0)
      windows.put(window.getWindowID(), window);
    }
    // initialize windowStack
    windows.putIfAbsent(findActiveWindow().getWindowID(), null);
  }

  /* (non-Javadoc)
   * @see com.opera.core.systems.scope.services.xml.IWindowManager#setActiveWindow(java.lang.String)
   */
  public void setActiveWindow(String title) {
    Integer windowId = (Integer) xpathPointer(windows.values(),
        "/.[title='" + title + "']/windowID").getValue();
    if (windowId == null) throw new NoSuchWindowException("No such window : "
        + title);
    setActiveWindowId(windowId);
  }

  public void closeAllWindows() {
    logger.fine("closeAllWindows");
    LinkedList<Integer> list = new LinkedList<Integer>(windows.asStack());
    boolean canCloseAll = services.getExec().getActionList().contains(
        "Close all pages");

    if (canCloseAll) {
      services.getExec().action("Close all pages");
    } else {
      while (!list.isEmpty()) {
        closeWindow(list.removeFirst());

        // BAD HACK! DELAYING CLOSE-WINDOW
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        logger.warning("Bad hack: sleeping 10ms between closing of windows, to prevent opera from crashing!");
        // BAD HACK DONE!
      }
    }
  }

  /* (non-Javadoc)
   * @see com.opera.core.systems.scope.services.xml.IWindowManager#getWindowHandles()
   */
  public List<Integer> getWindowHandles() {
    /*
    Collection<WindowInfo> windowCollection = getWindows().values();
    List<Integer> handles = new ArrayList<Integer>(windowCollection.size());

    for (WindowInfo window : windowCollection) {
      handles.add(window.getWindowID());
    }
    return handles;
    */
    return new LinkedList<Integer>(windows.asStack());// handles;
  }

  public void resetWindowsList() {
    initializeWindows();
  }

  public void clearFilter() {
    WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
    filterBuilder.setClearFilter(true);
    filterBuilder.addIncludePatternList("*");

    executeCommand(WindowManagerCommand.MODIFY_FILTER, filterBuilder);
  }

  public void filterWindow(Integer windowId) {
    WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
    filterBuilder.setClearFilter(true);
    filterBuilder.addIncludeIDList(windowId);

    executeCommand(WindowManagerCommand.MODIFY_FILTER, filterBuilder);

  }

  public String getActiveWindowTitle() {
    return windows.peek().getTitle();
  }

  public void openUrl(int windowId, String url) {
    OpenURLArg.Builder openUrlBuilder = OpenURLArg.newBuilder();
    openUrlBuilder.setWindowID(windowId);
    openUrlBuilder.setUrl(url);

    Response response = executeCommand(WindowManagerCommand.OPEN_URL, openUrlBuilder);

    if (response == null) {
      throw new WebDriverException("Internal error while opening " + url);
    }
  }

  public void closeWindow(int windowId) {
    CloseWindowArg.Builder closeWindowBuilder = CloseWindowArg.newBuilder();
    closeWindowBuilder.setWindowID(windowId);

    Response response = executeCommand(WindowManagerCommand.CLOSE_WINDOW, closeWindowBuilder);
    removeWindow(windowId);

    if (response == null) {
      throw new WebDriverException("Internal error while closing window");
    }
  }
}
