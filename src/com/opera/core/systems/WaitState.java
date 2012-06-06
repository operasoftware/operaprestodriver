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

package com.opera.core.systems;

import com.google.common.base.Throwables;

import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemID;
import com.opera.core.systems.scope.protos.UmsProtos.Response;

import org.openqa.selenium.WebDriverException;

import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * This class handles a queue of events to be handled from multiple threads. One thread can wait for
 * events to happen while other threads can post these events by calling the on{Event} handlers.
 *
 * @author Jan Vidar Krey <janv@opera.com>
 */
public class WaitState {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private boolean connected;
  private boolean waitEvents;

  /**
   * Whether we should keep a note of any idle events received.
   *
   * This is used when we perform an action where we want to wait for an idle event, such as
   * clicking. First one has to wait for the "click" response, but in the mean time there may be an
   * idle event which would be missed.  The number of missed idle events is stored in
   * capturedIdleEvents.
   */
  private boolean captureIdleEvents = false;

  /**
   * Stores the number of Idle events seen while captureIdleEvent is enabled.
   *
   * See the explanation for captureIdleEvent.
   */
  private int capturedIdleEvents = 0;

  private final Object lock = new Object();

  enum WaitResult {
    RESPONSE,                        // Got a response
    ERROR,                           // Got an error response
    EXCEPTION,                       // An exception occurred (STP connection is not alive)
    DISCONNECTED,                    // STP connection is disconnected
    HANDSHAKE,                       // STP Handshake
    EVENT_WINDOW_ACTIVATED,          // active window changed
    EVENT_WINDOW_LOADED,             // finished loaded
    EVENT_WINDOW_CLOSED,             // window closed
    EVENT_OPERA_IDLE,                // opera is now idle
    EVENT_DESKTOP_WINDOW_SHOWN,      // desktop window has shown and should be visible
    EVENT_DESKTOP_WINDOW_CLOSED,     // desktop window closed
    EVENT_DESKTOP_WINDOW_ACTIVATED,  // desktop window activated
    EVENT_DESKTOP_WINDOW_UPDATED,    // desktop window updated
    EVENT_DESKTOP_WINDOW_LOADED,
    EVENT_REQUEST_FIRED,             // sent when a http request is fired
    EVENT_QUICK_MENU_SHOWN,          // Quick menu shown
    EVENT_QUICK_MENU_CLOSED,         // Quick menu was closed
    EVENT_QUICK_MENU_ITEM_PRESSED,   // Quick menu item was pressed
    EVENT_SELFTEST_DONE,             // Selftest service finishes testing
    EVENT_DESKTOP_WINDOW_PAGE_CHANGED /* new Page shown in a dialog/window */
  }

  private class ResultItem {

    private int data;
    private final WaitResult waitResult;
    private Exception exception;
    private Response response;
    private boolean seen;
    private long remainingIdleTimeout;
    private DesktopWindowInfo desktopWindowInfo;
    private QuickMenuInfo quickMenuInfo;
    private QuickMenuID quickMenuId;
    private QuickMenuItemID quickMenuItemID;

    // Store the data for now, but it seems wasteful
    private String selftestResults;

    public ResultItem(Exception ex) {
      waitResult = WaitResult.EXCEPTION;
      exception = ex;
      logger.finest(String.format("EVENT: %s, exception: %s", waitResult, exception));
    }

    public ResultItem(WaitResult result) {
      this.waitResult = result;
      logger.finest("EVENT: " + result.toString());
    }

    /**
     * TODO: Explain
     *
     * Response events:
     *
     * BINARY_EXIT    with exit code WINDOW_LOADED  with windowID ERROR          with tag ID
     */
    public ResultItem(WaitResult result, int data) {
      this.waitResult = result;
      this.data = data;
      logger.finest("EVENT: " + result.toString() + ", data=" + data);
    }

    public ResultItem(WaitResult result, DesktopWindowInfo info) {
      this.waitResult = result;
      this.desktopWindowInfo = info;
      logger.finest("EVENT: " + result.toString() + ", desktop window=" + info.getWindowID());
    }

    public ResultItem(WaitResult result, QuickMenuInfo info) {
      this.waitResult = result;
      this.quickMenuInfo = info; // TODO FIXME
      logger
          .finest("EVENT: " + result.toString() + ", quick_menu=" + info.getMenuId().getMenuName());
    }

    public ResultItem(WaitResult result, QuickMenuID id) {
      this.waitResult = result;
      this.quickMenuId = id;

      logger.finest("EVENT: " + result.toString() + ", quick_menu=" + id.getMenuName());
    }

    public ResultItem(WaitResult result, QuickMenuItemID info) {
      this.waitResult = result;
      this.quickMenuItemID = info; // TODO FIXME
      logger.finest("EVENT: " + result.toString() + ", quick_menu_item=" + info.getMenuText());
    }


    public ResultItem(Response response, int tag) {
      this.response = response;
      this.data = tag;
      waitResult = WaitResult.RESPONSE;
      logger.finest("EVENT: " + waitResult.toString() + ", data=" + data);
    }

    public ResultItem(String results) {
      waitResult = WaitResult.EVENT_SELFTEST_DONE;
      selftestResults = results;
    }

    public boolean isEventToWaitFor() {
      switch (waitResult) {
        case EVENT_DESKTOP_WINDOW_ACTIVATED:
        case EVENT_DESKTOP_WINDOW_CLOSED:
        case EVENT_DESKTOP_WINDOW_UPDATED:
        case EVENT_DESKTOP_WINDOW_SHOWN:
        case EVENT_DESKTOP_WINDOW_LOADED:
        case EVENT_QUICK_MENU_SHOWN:
        case EVENT_QUICK_MENU_CLOSED:
        case EVENT_QUICK_MENU_ITEM_PRESSED:
        case EVENT_SELFTEST_DONE:
        case EVENT_DESKTOP_WINDOW_PAGE_CHANGED:
          return true;
      }
      return false;
    }

    public boolean wasSeen() {
      return seen;
    }

    public Exception getException() {
      return exception;
    }
  }

  // TODO: Replace with BlockingQueue
  private final LinkedList<ResultItem> events = new LinkedList<ResultItem>();

  enum ResponseType {
    HANDSHAKE,
    RESPONSE,
    WINDOW_LOADED,
    REQUEST_FIRED,
    OPERA_IDLE,
    DESKTOP_WINDOW_SHOWN,
    DESKTOP_WINDOW_UPDATED,
    DESKTOP_WINDOW_ACTIVATED,
    DESKTOP_WINDOW_CLOSED,
    DESKTOP_WINDOW_LOADED,
    QUICK_MENU_SHOWN,
    QUICK_MENU_CLOSED,
    QUICK_MENU_ITEM_PRESSED,
    SELFTEST_DONE,
    DESKTOP_WINDOW_PAGE_CHANGED;
  }

  public WaitState() {
    connected = true;
    waitEvents = false;
  }

  public void setWaitEvents(boolean wait_events) {
    this.waitEvents = wait_events;
  }

  private void internalWait(long timeout) {
    try {
      if (!connected) {
        throw new CommunicationException("Waiting aborted - not connected!");
      }
      lock.wait(timeout);
    } catch (InterruptedException e) {
      throw new WebDriverException(e);
    }
  }

  void onHandshake() {
    synchronized (lock) {
      logger.finest("Event: onHandshake");
      events.add(new ResultItem(WaitResult.HANDSHAKE));
      lock.notifyAll();
    }
  }

  void onResponse(int tag, Response response) {
    synchronized (lock) {
      logger.finest("Event: onResponse for " + tag);
      events.add(new ResultItem(response, tag));
      lock.notifyAll();
    }
  }

  void onError(int tag) {
    synchronized (lock) {
      logger.fine("Got ERROR for " + tag);
      events.add(new ResultItem(WaitResult.ERROR, tag));
      lock.notifyAll();
    }
  }

  void onException(Exception e) {
    synchronized (lock) {
      events.add(new ResultItem(e));
      connected = false;
      lock.notifyAll();
    }
  }

  void onDisconnected() {
    synchronized (lock) {
      logger.finest("Event: onDisconnected");
      events.add(new ResultItem(WaitResult.DISCONNECTED));
      connected = false;
      lock.notifyAll();
    }
  }

  void onWindowLoaded(int windowId) {
    synchronized (lock) {
      logger.finest("Event: onWindowLoaded");
      events.add(new ResultItem(WaitResult.EVENT_WINDOW_LOADED, windowId));
      lock.notifyAll();
    }
  }

  public void onRequest(int windowId) {
    synchronized (lock) {
      logger.finest("Event: onRequest");
      events.add(new ResultItem(WaitResult.EVENT_REQUEST_FIRED, windowId));
      lock.notifyAll();
    }
  }

  void onWindowClosed(int windowId) {
    synchronized (lock) {
      logger.finest("Event: onWindowClosed");
      events.add(new ResultItem(WaitResult.EVENT_WINDOW_CLOSED, windowId));
      lock.notifyAll();
    }
  }

  void onOperaIdle() {
    synchronized (lock) {
      logger.finest("Event: onOperaIdle");
      events.add(new ResultItem(WaitResult.EVENT_OPERA_IDLE, 0));// 0 is
      // important to match later
      lock.notifyAll();
    }
  }

  void onDesktopWindowClosed(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.finest("Event: onDesktopWindowClosed");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_CLOSED, info));
      lock.notifyAll();
    }
  }

  void onDesktopWindowActivated(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.finest("Event: onDesktopWindowActivated");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_ACTIVATED, info));
      lock.notifyAll();
    }
  }

  void onDesktopWindowShown(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.finest("Event: onDesktopWindowShown");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_SHOWN, info));
      lock.notifyAll();
    }
  }

  void onDesktopWindowUpdated(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.finest("Event: onDesktopWindowUpdated");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_UPDATED, info));
      lock.notifyAll();
    }
  }

  void onDesktopWindowLoaded(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.finest("Event: onDesktopWindowLoaded");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_LOADED, info));
      lock.notifyAll();
    }
  }

  void onQuickMenuShown(QuickMenuInfo info) {
    synchronized (lock) {
      logger.finest("Event: onQuickMenuShown");
      events.add(new ResultItem(WaitResult.EVENT_QUICK_MENU_SHOWN, info));
      lock.notifyAll();
    }
  }

  void onQuickMenuClosed(QuickMenuID id) {
    synchronized (lock) {
      logger.finest("Event: onQuickMenuClosed");
      events.add(new ResultItem(WaitResult.EVENT_QUICK_MENU_CLOSED, id));
      lock.notifyAll();
    }
  }

  void onQuickMenuItemPressed(QuickMenuItemID menuItemID) {
    synchronized (lock) {
      logger.finest("Event: onQuickMenuItemPressed");
      events.add(new ResultItem(WaitResult.EVENT_QUICK_MENU_ITEM_PRESSED, menuItemID));
      lock.notifyAll();
    }
  }

  void onSelftestDone(String results) {
    synchronized (lock) {
      logger.finest("Event: onSelftestDone");
      events.add(new ResultItem(results));
      lock.notifyAll();
    }
  }

  void onDesktopWindowPageChanged(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.fine("Event: onDesktopPageChanged");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_PAGE_CHANGED, info));
      lock.notify();
    }
  }

  private ResultItem getResult() {
    if (events.isEmpty()) {
      return null;
    }

    // Test if we are listening to wait events
    // We don't want to remove them from the list while we are
    if (waitEvents) {
      ResultItem resultItem = events.getFirst();
      while (true) {
        if (!resultItem.isEventToWaitFor()) {
          events.remove(resultItem);
          return resultItem;
        }

        if (!resultItem.wasSeen()) {
          resultItem.seen = true;
          return resultItem;
        }

        int index = events.indexOf(resultItem);
        if (index + 1 >= events.size()) {
          return null;
        }

        resultItem = events.get(index + 1);
      }
    }

    return events.removeFirst();
  }

  /**
   * Checks for a result item. <p/> If no result item is available this method will wait for timeout
   * milliseconds and try again. If still no result if available then a ResponseNotReceivedException
   * is thrown.
   *
   * @param timeout time in milliseconds to wait before retrying.
   * @param idle    whether you are waiting for an Idle event. Changes error message.
   * @return the result
   */
  private ResultItem pollResultItem(long timeout, boolean idle) {
    ResultItem result = getResult();
    if (result != null) {
      result.remainingIdleTimeout = timeout;
    }

    if (result == null && timeout > 0) {
      long start = System.currentTimeMillis();
      internalWait(timeout);
      long end = System.currentTimeMillis();
      result = getResult();
      if (result != null) {
        result.remainingIdleTimeout = timeout - (end - start);
        logger.finest("Remaining timeout: " + result.remainingIdleTimeout);
      }
    }

    if (result == null) {
      if (idle) {
        throw new ResponseNotReceivedException("No idle response in a timely fashion");
      } else {
        throw new ResponseNotReceivedException("No response in a timely fashion");
      }
    }

    return result;
  }

  private ResultItem waitAndParseResult(long timeout, int match, String stringMatch,
                                        final ResponseType type) {
    synchronized (lock) {
      while (true) {
        ResultItem result = pollResultItem(timeout, type == ResponseType.OPERA_IDLE);
        timeout = result.remainingIdleTimeout;
        WaitResult waitResult = result.waitResult;

        switch (waitResult) {
          case HANDSHAKE:
            if (type == ResponseType.HANDSHAKE) {
              return null;
            }
            break;

          case RESPONSE:
            if (result.data == match && type == ResponseType.RESPONSE) {
              return result;
            } else if (type == ResponseType.HANDSHAKE) {
              throw new CommunicationException("Expected handshake, got response");
            }
            break;

          case ERROR:
            if (result.data == match && type == ResponseType.RESPONSE) {
              return null;
            } else if (type == ResponseType.HANDSHAKE) {
              throw new CommunicationException("Expected handshake, got error");
            }
            break;

          case EXCEPTION:
            Throwables.propagate(result.getException());
            break;

          case DISCONNECTED:
            throw new CommunicationException(String.format("Problem encountered: %s", waitResult));

          case EVENT_WINDOW_LOADED:
            if (result.data == match && type == ResponseType.WINDOW_LOADED) {
              return null;
            }
            break;

          case EVENT_WINDOW_CLOSED:
            if (result.data == match && type == ResponseType.WINDOW_LOADED) {
              throw new CommunicationException("Window closed unexpectedly");
            }
            break;

          case EVENT_DESKTOP_WINDOW_SHOWN:
            if (type == ResponseType.DESKTOP_WINDOW_SHOWN) {
              if (stringMatch.length() == 0) {
                return result;
              } else {
                logger.finest("EVENT_DESKTOP_WINDOW_SHOWN: Name: "
                              + result.desktopWindowInfo.getName() + " ID: "
                              + result.desktopWindowInfo.getWindowID() + " OnScreen: "
                              + result.desktopWindowInfo.getOnScreen());

                if (result.desktopWindowInfo.getName().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_DESKTOP_WINDOW_UPDATED:
            if (type == ResponseType.DESKTOP_WINDOW_UPDATED) {
              if (stringMatch.length() == 0) {
                return result;
              } else {
                logger.fine("EVENT_DESKTOP_WINDOW_UPDATED: Name: "
                            + result.desktopWindowInfo.getName() + " ID: "
                            + result.desktopWindowInfo.getWindowID());

                if (result.desktopWindowInfo.getName().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_DESKTOP_WINDOW_ACTIVATED:
            if (type == ResponseType.DESKTOP_WINDOW_ACTIVATED) {
              if (stringMatch.length() == 0) {
                return result;
              } else {
                logger.fine("DESKTOP_WINDOW_ACTIVATED: Name: "
                            + result.desktopWindowInfo.getName() + " ID: "
                            + result.desktopWindowInfo.getWindowID());

                if (result.desktopWindowInfo.getName().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_DESKTOP_WINDOW_CLOSED:
            if (type == ResponseType.DESKTOP_WINDOW_CLOSED) {
              if (stringMatch.length() == 0) {
                return result;
              } else {
                logger.fine("EVENT_DESKTOP_WINDOW_CLOSED: Name: "
                            + result.desktopWindowInfo.getName() + " ID: "
                            + result.desktopWindowInfo.getWindowID());

                if (result.desktopWindowInfo.getName().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_DESKTOP_WINDOW_LOADED:
            if (type == ResponseType.DESKTOP_WINDOW_LOADED) {
              if (stringMatch.length() == 0) {
                return result;
              } else {
                logger.fine("EVENT_DESKTOP_WINDOW_LOADED: Name: "
                            + result.desktopWindowInfo.getName() + " ID: "
                            + result.desktopWindowInfo.getWindowID());

                if (result.desktopWindowInfo.getName().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_QUICK_MENU_SHOWN:
            if (type == ResponseType.QUICK_MENU_SHOWN) {
              if (stringMatch.length() == 0) {
                return result;
              } else {
                logger.fine("EVENT_QUICK_MENU_SHOWN: Name: "
                            + result.quickMenuInfo.getMenuId().getMenuName());

                if (result.quickMenuInfo.getMenuId().getMenuName().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_QUICK_MENU_CLOSED:
            if (type == ResponseType.QUICK_MENU_CLOSED) {
              // empty stringMatch
              if (stringMatch != null && stringMatch.length() == 0) {
                return result;
              } else {
                if (result.quickMenuId.getMenuName().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_QUICK_MENU_ITEM_PRESSED:
            if (type == ResponseType.QUICK_MENU_ITEM_PRESSED) {
              if (stringMatch.length() == 0) {
                return result;
              } else {
                logger.fine("QUICK_MENU_ITEM_PRESSED: Text: "
                            + result.quickMenuItemID.getMenuText());

                if (result.quickMenuItemID.getMenuText().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_DESKTOP_WINDOW_PAGE_CHANGED:
            if (type == ResponseType.DESKTOP_WINDOW_PAGE_CHANGED) {
              if (stringMatch.length() == 0) {
                return result;
              } else {
                logger.fine("EVENT_DESKTOP_WINDOW_PAGE_CHANGED: Name: "
                            + result.desktopWindowInfo.getName() + " ID: "
                            + result.desktopWindowInfo.getWindowID() + " OnScreen: "
                            + result.desktopWindowInfo.getOnScreen());

                if (result.desktopWindowInfo.getName().equals(stringMatch)) {
                  return result;
                }
              }
            }
            break;

          case EVENT_REQUEST_FIRED:
            if (result.data == match && type == ResponseType.REQUEST_FIRED) {
              return null;
            }
            break;

          case EVENT_OPERA_IDLE:
            logger.finest("RECV EVENT_OPERA_IDLE!");
            if (result.data == match && type == ResponseType.OPERA_IDLE) {
              return null;
            }
            break;

          case EVENT_SELFTEST_DONE:
            // TODO:
            logger.finest("RECV EVENT_SELFTEST_DONE");
            if (type == ResponseType.SELFTEST_DONE) {
              return result;
            }
            break;
        }
      }
    }
  }

  public void waitForHandshake(long value) {
    waitAndParseResult(value, 0, null, ResponseType.HANDSHAKE);
  }

  public void waitForWindowLoaded(int windowId, long timeout) {
    waitAndParseResult(timeout, windowId, null, ResponseType.WINDOW_LOADED);
  }

  /**
   * Enables the capturing on OperaIdle events. <p/> Sometimes when executing a command OperaIdle
   * events will fire before the response is received for the sent command. This results in missing
   * the Idle events, and later probably hitting a timeout. <p/> To prevent this you can call this
   * method which will enable the tracking of any Idle events received between now and when you call
   * waitForOperaIdle(). If Idle events have been received then waitForOperaIdle() will return
   * immediately.
   */
  public void captureOperaIdle() {
    logger.finer("captureIdleEvents is now true!");
    captureIdleEvents = true;
  }

  /**
   * Waits for an OperaIdle event before continuing. <p/> If captureOperaIdle() has been called
   * since the last call of waitForOperaIdle(), and one or more OperaIdle events have occurred then
   * this method will return immediately. <p/> After calling this method the capturing of OperaIdle
   * events is disabled until the next call of captureOperaIdle()
   *
   * @param timeout time in milliseconds to wait before aborting
   */
  public void waitForOperaIdle(long timeout) {
    if (captureIdleEvents && capturedIdleEvents > 0) {
      logger.finer("Captured " + capturedIdleEvents + " idle event(s)");
      // reset
      captureIdleEvents = false;
      capturedIdleEvents = 0;
      return;
    }

    // If we're waiting for an Idle event, then we don't need to capture them anymore.  If we've
    // reached this far then capturedIdleEvents is already 0.
    captureIdleEvents = false;
    waitAndParseResult(timeout, 0 /*0 = no window id!*/, null, ResponseType.OPERA_IDLE);
  }

  public Response waitFor(int tag, long timeout) {
    ResultItem item = waitAndParseResult(timeout, tag, null, ResponseType.RESPONSE);

    if (item != null) {
      return item.response;
    }

    return null;
  }

  public int waitForDesktopWindowShown(String windowName, long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, windowName, ResponseType.DESKTOP_WINDOW_SHOWN);

    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }

    return 0;
  }

  public int waitForDesktopWindowUpdated(String windowName, long timeout) {
    ResultItem item = waitAndParseResult(timeout,
                                         0,
                                         windowName,
                                         ResponseType.DESKTOP_WINDOW_UPDATED);

    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }

    return 0;
  }

  public int waitForDesktopWindowActivated(String windowName, long timeout) {
    ResultItem item = waitAndParseResult(timeout,
                                         0,
                                         windowName,
                                         ResponseType.DESKTOP_WINDOW_ACTIVATED);

    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }

    return 0;
  }

  public int waitForDesktopWindowClosed(String windowName, long timeout) {
    ResultItem item = waitAndParseResult(timeout,
                                         0,
                                         windowName,
                                         ResponseType.DESKTOP_WINDOW_CLOSED);

    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }

    return 0;
  }

  public int waitForDesktopWindowLoaded(String windowName, long timeout) {
    ResultItem item = waitAndParseResult(timeout,
                                         0,
                                         windowName,
                                         ResponseType.DESKTOP_WINDOW_LOADED);

    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }

    return 0;
  }

  public int waitForWindowPageChanged(String windowName, long timeout) {
    ResultItem item = waitAndParseResult(timeout,
                                         0,
                                         windowName,
                                         ResponseType.DESKTOP_WINDOW_PAGE_CHANGED);

    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }
    return 0;
  }

  public String waitForQuickMenuShown(String menuName, long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, menuName, ResponseType.QUICK_MENU_SHOWN);

    if (item != null) {
      return item.quickMenuInfo.getMenuId().getMenuName();
    }

    return "";
  }

  public String waitForQuickMenuClosed(String menuName, long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, menuName, ResponseType.QUICK_MENU_CLOSED);

    if (item != null) {
      return item.quickMenuId.getMenuName();
    }

    return "";
  }

  public String waitForQuickMenuItemPressed(String menuItemText, long timeout) {
    ResultItem
        item =
        waitAndParseResult(timeout, 0, menuItemText, ResponseType.QUICK_MENU_ITEM_PRESSED);

    if (item != null) {
      return item.quickMenuItemID.getMenuText();
    }

    return "";
  }

  public String waitForSelftestDone(long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, null, ResponseType.SELFTEST_DONE);

    if (item != null) {
      return item.selftestResults;
    }

    return null;
  }

}