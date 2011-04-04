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

import java.util.LinkedList;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;

/**
 * This class handles a queue of events to be handled from multiple threads. One
 * thread can wait for events to happen while other threads can post these
 * events by calling the on{Event} handlers.
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
public class WaitState {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private boolean connected;
  private boolean wait_events;
	/// Whether we should keep a note of any idle events recieved.
	///
	/// This is used when we perform an action where we want to wait for an Idle
	/// event, such as clicking. First one has to wait for the "click" response,
	/// but in the mean time there may be an Idle event which would be missed.
	/// The number of missed Idle events is stored in captured_idle_events.
	private boolean capture_idle_events = false;
	/// Stores the number of Idle events seen while capture_idle_event is enabled
	///
	/// See the explanation for capture_idle_event.
	private int captured_idle_events = 0;

  private Object lock = new Object();

  enum WaitResult {
    RESPONSE, /* Got a response */
    ERROR, /* Got an error response */
    EXCEPTION, /* An exception occured (STP connection is not alive) */
    DISCONNECTED, /* STP connection is disconnected */
    HANDSHAKE, /* STP Handshake */
    EVENT_WINDOW_LOADED, /* finished loaded */
    EVENT_WINDOW_CLOSED, /* window closed */
    EVENT_OPERA_IDLE, /* opera is now idle */
    EVENT_DESKTOP_WINDOW_SHOWN, /* desktop window has shown and should be visible */
    EVENT_DESKTOP_WINDOW_CLOSED, /* desktop window closed */
    EVENT_DESKTOP_WINDOW_ACTIVATED, /* desktop window activated */
    EVENT_DESKTOP_WINDOW_UPDATED, /* desktop window updated*/
    EVENT_DESKTOP_WINDOW_LOADED, EVENT_REQUEST_FIRED
    /* sent when a http request is fired */
  }

  private class ResultItem {
    int data;
    WaitResult waitResult;
    WebDriverException exception;
    Response response;
    boolean seen;
    DesktopWindowInfo desktopWindowInfo; // No idea if this is right but it will

    // store the data for now, but it seems
    // wasteful

    public ResultItem(WebDriverException ex) {
      waitResult = WaitResult.EXCEPTION;
      exception = ex;
      logger.fine("EVENT: " + waitResult.toString() + ", exception: "
          + ex.toString());
    }

    public ResultItem(WaitResult result) {
      this.waitResult = result;
      logger.fine("EVENT: " + result.toString());
    }

    // BINARY_EXIT - with exit code.
    // WINDOW_LOADED with windowId
    // ERROR with tag id
    public ResultItem(WaitResult result, int data) {
      this.waitResult = result;
      this.data = data;
      logger.fine("EVENT: " + result.toString() + ", data=" + data);
    }

    public ResultItem(WaitResult result, DesktopWindowInfo info) {
      this.waitResult = result;
      this.desktopWindowInfo = info;
      logger.fine("EVENT: " + result.toString() + ", desktop window="
          + info.getWindowID());
    }

    public ResultItem(Response response, int tag) {
      this.response = response;
      this.data = tag;
      waitResult = WaitResult.RESPONSE;
      logger.fine("EVENT: " + waitResult.toString() + ", data=" + data);
    }

    public boolean isEventToWaitFor() {
      switch (waitResult) {
      case EVENT_DESKTOP_WINDOW_ACTIVATED:
      case EVENT_DESKTOP_WINDOW_CLOSED:
      case EVENT_DESKTOP_WINDOW_UPDATED:
      case EVENT_DESKTOP_WINDOW_SHOWN:
      case EVENT_DESKTOP_WINDOW_LOADED:
        return true;
      }
      return false;
    }

    public boolean wasSeen() {
      return seen;
    }
  }

  // Replace with BlockingQueue
  LinkedList<ResultItem> events = new LinkedList<ResultItem>();

  enum ResponseType {
    HANDSHAKE, RESPONSE, WINDOW_LOADED, REQUEST_FIRED, OPERA_IDLE, DESKTOP_WINDOW_SHOWN, DESKTOP_WINDOW_UPDATED, DESKTOP_WINDOW_ACTIVATED, DESKTOP_WINDOW_CLOSED, DESKTOP_WINDOW_LOADED;
  }

  public WaitState() {
    connected = true;
    wait_events = false;
  }

  public void setWaitEvents(boolean wait_events) {
    this.wait_events = wait_events;
  }

  private void internalWait(long timeout) throws WebDriverException {
    try {
      if (!connected) throw new CommunicationException(
          "Waiting aborted - not connected!");
      lock.wait(timeout);
    } catch (InterruptedException e) {
      throw new WebDriverException(e);
    }
  }

  void onHandshake() {
    synchronized (lock) {
      logger.fine("Got handshake");
      events.add(new ResultItem(WaitResult.HANDSHAKE));
      lock.notify();
    }
  }

  void onResponse(int tag, Response response) {
    synchronized (lock) {
      logger.fine("Got response for " + tag);
      events.add(new ResultItem(response, tag));
      lock.notify();
    }
  }

  void onError(int tag) {
    synchronized (lock) {
      logger.fine("Got ERROR for " + tag);
      events.add(new ResultItem(WaitResult.ERROR, tag));
      lock.notify();
    }
  }

  void onException(Exception e) {
    synchronized (lock) {
      logger.info("Got exception");
      events.add(new ResultItem(new WebDriverException(e)));
      connected = false;
      lock.notify();
    }
  }

  void onDisconnected() {
    synchronized (lock) {
      logger.info("Got disconnected.");
      events.add(new ResultItem(WaitResult.DISCONNECTED));
      connected = false;
      lock.notify();
    }
  }

  void onWindowLoaded(int windowId) {
    synchronized (lock) {
      logger.fine("Event: onWindowLoaded");
      events.add(new ResultItem(WaitResult.EVENT_WINDOW_LOADED, windowId));
      lock.notify();
    }
  }

  public void onRequest(int windowId) {
    synchronized (lock) {
      logger.info("Event: onRequest");
      events.add(new ResultItem(WaitResult.EVENT_REQUEST_FIRED, windowId));
      lock.notify();
    }
  }

  void onWindowClosed(int windowId) {
    synchronized (lock) {
      logger.fine("Event: onWindowClosed");
      events.add(new ResultItem(WaitResult.EVENT_WINDOW_CLOSED, windowId));
      lock.notify();
    }
  }

  void onOperaIdle() {
    synchronized (lock) {
      logger.fine("OperaIdle: Got message");

      logger.fine("Event: onOperaIdle");
      events.add(new ResultItem(WaitResult.EVENT_OPERA_IDLE, 0));// 0 is
      // important to
      // match later
      lock.notify();
    }
  }

  void onDesktopWindowClosed(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.fine("Event: onDesktopWindowClosed");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_CLOSED, info));
      lock.notify();
    }
  }

  void onDesktopWindowActivated(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.fine("Event: onDesktopWindowActivated");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_ACTIVATED, info));
      lock.notify();
    }
  }

  void onDesktopWindowShown(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.fine("Event: onDesktopWindowShown");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_SHOWN, info));
      lock.notify();
    }
  }

  void onDesktopWindowUpdated(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.fine("Event: onDesktopWindowUpdated");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_UPDATED, info));
      lock.notify();
    }
  }

  void onDesktopWindowLoaded(DesktopWindowInfo info) {
    synchronized (lock) {
      logger.fine("Event: onDesktopWindowLoaded");
      events.add(new ResultItem(WaitResult.EVENT_DESKTOP_WINDOW_LOADED, info));
      lock.notify();
    }
  }

  private ResultItem getResult() {
    if (events.isEmpty()) return null;

    // Test if we are listening to wait events
    // We don't want to remove them from the list while we are
    if (wait_events) {
      ResultItem result_item = events.getFirst();
      while (true) {
        if (!result_item.isEventToWaitFor()) {
          events.remove(result_item);
          return result_item;
        }

        if (!result_item.wasSeen()) {
          result_item.seen = true;
          return result_item;
        }

        int index = events.indexOf(result_item);
        if (index + 1 >= events.size()) {
          return null;
        }

        result_item = events.get(index + 1);
      }
    }

    return events.removeFirst();
  }

    /**
     * Checks for a result item.
     *
     * If no result item is available this method will wait for timeout
     * milliseconds and try again. If still no result if available then
     * a ResponseNotReceivedException is thrown.
     * @param timeout Time in milliseconds to wait before retrying.
     * @param idle Whether you are waiting for an Idle event. Changes error message.
     * @return The result
     */
    private final ResultItem pollResultItem(long timeout, boolean idle) {
    ResultItem result = getResult();

    if (result == null) {
      internalWait(timeout);
      result = getResult();
    }

    if (result == null)
      if (idle)
        throw new ResponseNotReceivedException("No OperaIdle response in a timely fashion.");
      else
        throw new ResponseNotReceivedException("No response in a timely fashion.");

    return result;
  }

  private final ResultItem waitAndParseResult(long timeout, int match,
      String stringMatch, final ResponseType type) {
    synchronized (lock) {
      while (true) {
        ResultItem result = pollResultItem(timeout, type == ResponseType.OPERA_IDLE);
        WaitResult waitResult = result.waitResult;

        switch (waitResult) {
        case HANDSHAKE:
          if (type == ResponseType.HANDSHAKE) return null;
          break;

        case RESPONSE:
          if (result.data == match && type == ResponseType.RESPONSE) return result;
          else if (type == ResponseType.HANDSHAKE) throw new CommunicationException(
              "Expecting handshake");
          break;

        case ERROR:
          if (result.data == match && type == ResponseType.RESPONSE) return null;
          else if (type == ResponseType.HANDSHAKE) throw new CommunicationException(
              "Expecting handshake");
          break;

        case EXCEPTION:
          throw result.exception;

        case DISCONNECTED:
          throw new CommunicationException("Problem encountered : "
              + waitResult.toString());

        case EVENT_WINDOW_LOADED:
          if (result.data == match && type == ResponseType.WINDOW_LOADED) return null;
          break;

        case EVENT_WINDOW_CLOSED:
          if (result.data == match && type == ResponseType.WINDOW_LOADED) throw new CommunicationException(
              "Window closed unexpectedly");
          break;

        case EVENT_DESKTOP_WINDOW_SHOWN:
          if (type == ResponseType.DESKTOP_WINDOW_SHOWN) {
            if (stringMatch.length() == 0) return result;
            else {
              logger.fine("EVENT_DESKTOP_WINDOW_SHOWN: Name: "
                  + result.desktopWindowInfo.getName() + " ID: "
                  + result.desktopWindowInfo.getWindowID() + " OnScreen: "
                  + result.desktopWindowInfo.getOnScreen());

              if (result.desktopWindowInfo.getName().equals(stringMatch)) return result;
            }
          }
          break;

        case EVENT_DESKTOP_WINDOW_UPDATED:
          if (type == ResponseType.DESKTOP_WINDOW_UPDATED) {
            if (stringMatch.length() == 0) return result;
            else {
              logger.fine("EVENT_DESKTOP_WINDOW_UPDATED: Name: "
                  + result.desktopWindowInfo.getName() + " ID: "
                  + result.desktopWindowInfo.getWindowID());

              if (result.desktopWindowInfo.getName().equals(stringMatch)) return result;
            }
          }
          break;

        case EVENT_DESKTOP_WINDOW_ACTIVATED:
          if (type == ResponseType.DESKTOP_WINDOW_ACTIVATED) {
            if (stringMatch.length() == 0) return result;
            else {
              logger.fine("DESKTOP_WINDOW_ACTIVATED: Name: "
                  + result.desktopWindowInfo.getName() + " ID: "
                  + result.desktopWindowInfo.getWindowID());

              if (result.desktopWindowInfo.getName().equals(stringMatch)) return result;
            }
          }
          break;

        case EVENT_DESKTOP_WINDOW_CLOSED:
          if (type == ResponseType.DESKTOP_WINDOW_CLOSED) {
            if (stringMatch.length() == 0) return result;
            else {
              logger.fine("EVENT_DESKTOP_WINDOW_CLOSED: Name: "
                  + result.desktopWindowInfo.getName() + " ID: "
                  + result.desktopWindowInfo.getWindowID());

              if (result.desktopWindowInfo.getName().equals(stringMatch)) return result;
            }
          }
          break;

        case EVENT_DESKTOP_WINDOW_LOADED:
          if (type == ResponseType.DESKTOP_WINDOW_LOADED) {
            if (stringMatch.length() == 0) return result;
            else {
              logger.fine("EVENT_DESKTOP_WINDOW_LOADED: Name: "
                  + result.desktopWindowInfo.getName() + " ID: "
                  + result.desktopWindowInfo.getWindowID());

              if (result.desktopWindowInfo.getName().equals(stringMatch)) return result;
            }
          }
          break;

        case EVENT_REQUEST_FIRED:
          if (result.data == match && type == ResponseType.REQUEST_FIRED) return null;

        case EVENT_OPERA_IDLE:
          logger.finest("RECV EVENT_OPERA_IDLE!");
          if (result.data == match && type == ResponseType.OPERA_IDLE) return null;
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
     * Enables the capturing on OperaIdle events.
     *
     * Sometimes when executing a command OperaIdle events will fire before
     * the response is received for the sent command. This results in missing
     * the Idle events, and later probably hitting a timeout.
     *
     * To prevent this you can call this method which will enable the
     * tracking of any Idle events received between now and when you call
     * waitForOperaIdle(). If Idle events have been received then
     * waitForOperaIdle() will return immediately.
     */
    public void captureOperaIdle() {
      capture_idle_events = true;
    }

    /**
     * Waits for an OperaIdle event before continuing.
     *
     * If captureOperaIdle() has been called since the last call of
     * waitForOperaIdle(), and one or more OperaIdle events have occurred then
     * this method will return immediately.
     *
     * After calling this method the capturing of OperaIdle events is
     * disabled until the next call of captureOperaIdle()
     * @param timeout Time in milliseconds to wait before aborting
     */
  public void waitForOperaIdle(long timeout) {
      if (capture_idle_events == true && captured_idle_events > 0) {
        logger.fine("Captured "+captured_idle_events+" OperaIdle event(s)");
        // reset
        capture_idle_events = false;
        captured_idle_events = 0;
        return;
      }

      // If we're waiting for an Idle event, then we don't need to capture
      // them anymore. If we've reached this far then captured_idle_events is
      // already 0
      capture_idle_events = false;
    	waitAndParseResult(timeout, 0/*0 = no window id!*/, null, ResponseType.OPERA_IDLE);
  }

  public Response waitFor(int tag, long timeout) {
    ResultItem item = waitAndParseResult(timeout, tag, null,
        ResponseType.RESPONSE);
    if (item != null) {
      return item.response;
    }
    return null;
  }

  public void waitForRequest(int windowId, long timeout) {
    waitAndParseResult(timeout, windowId, null, ResponseType.REQUEST_FIRED);
  }

  public int waitForDesktopWindowShown(String win_name, long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, win_name,
        ResponseType.DESKTOP_WINDOW_SHOWN);
    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }
    return 0;
  }

  public int waitForDesktopWindowUpdated(String win_name, long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, win_name,
        ResponseType.DESKTOP_WINDOW_UPDATED);
    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }
    return 0;
  }

  public int waitForDesktopWindowActivated(String win_name, long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, win_name,
        ResponseType.DESKTOP_WINDOW_ACTIVATED);
    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }
    return 0;

  }

  public int waitForDesktopWindowClosed(String win_name, long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, win_name,
        ResponseType.DESKTOP_WINDOW_CLOSED);
    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }
    return 0;
  }

  public int waitForDesktopWindowLoaded(String win_name, long timeout) {
    ResultItem item = waitAndParseResult(timeout, 0, win_name,
        ResponseType.DESKTOP_WINDOW_LOADED);
    if (item != null) {
      return item.desktopWindowInfo.getWindowID();
    }
    return 0;
  }
}
