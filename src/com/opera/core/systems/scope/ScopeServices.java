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

package com.opera.core.systems.scope;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import com.opera.core.systems.internal.ImplicitWait;
import com.opera.core.systems.internal.OperaDefaults;
import com.opera.core.systems.internal.VersionUtil;
import com.opera.core.systems.runner.interfaces.OperaRunner;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.exceptions.ScopeException;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.scope.handlers.ScopeEventHandler;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemID;
import com.opera.core.systems.scope.protos.ScopeProtos;
import com.opera.core.systems.scope.protos.ScopeProtos.ClientInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.HostInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceResult;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceSelection;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.ConsoleLogger;
import com.opera.core.systems.scope.services.CookieManager;
import com.opera.core.systems.scope.services.Core;
import com.opera.core.systems.scope.services.Debugger;
import com.opera.core.systems.scope.services.EcmascriptDebugger;
import com.opera.core.systems.scope.services.Exec;
import com.opera.core.systems.scope.services.Prefs;
import com.opera.core.systems.scope.services.Selftest;
import com.opera.core.systems.scope.services.WindowManager;
import com.opera.core.systems.scope.services.desktop.DesktopUtils;
import com.opera.core.systems.scope.services.desktop.DesktopWindowManager;
import com.opera.core.systems.scope.stp.StpConnection;
import com.opera.core.systems.scope.stp.StpThread;
import com.opera.core.systems.scope.stp.services.MockEcmascriptDebugger;
import com.opera.core.systems.scope.stp.services.desktop.ScopeSystemInputManager;
import com.opera.core.systems.scope.stp.services.messages.ScopeMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Implements the interface to the Scope protocol.
 */
public class ScopeServices implements IConnectionHandler {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final SortedSet<ScopeService> requiredServices;
  private final StpThread stpThread;
  private final AtomicInteger tagCounter;
  private final WaitState waitState = new WaitState();

  private Core core;
  private Debugger debugger;
  private Exec exec;
  private WindowManager windowManager;
  private ConsoleLogger consoleLogger;
  private DesktopWindowManager desktopWindowManager;
  private DesktopUtils desktopUtils;
  private Prefs prefs;
  private ScopeSystemInputManager systemInputManager;
  private CookieManager cookieManager;
  private Selftest selftest;
  private StpConnection connection = null;
  private List<String> listedServices;
  private boolean shutdown = false;
  private Map<ScopeService, Service> services = ImmutableMap.of();
  private Map<ScopeService, String> availableServices = ImmutableMap.of();

  /**
   * Creates the Scope server on specified address and port, as well as enabling the required Scope
   * services.
   *
   * @param requiredServices set of required services
   * @param port             the port on which to start the Scope server
   * @param manualConnect    whether to output ready message with port number when starting
   * @throws IOException if an I/O error occurs
   */
  public ScopeServices(final SortedSet<ScopeService> requiredServices, int port,
                       boolean manualConnect) throws IOException {
    this.requiredServices = requiredServices;
    tagCounter = new AtomicInteger();
    stpThread = new StpThread(port, this, new ScopeEventHandler(this), manualConnect);
  }

  /**
   * Gets the supported services from Opera and calls methods to enable the ones we requested.
   */
  public void init() {
    waitForHandshake();

    availableServices = buildAvailableServices(getHostInfo());

    connect();

    // We always need a debugger, and it should never be included in the set of required services
    if (OperaDefaults.ENABLE_DEBUGGER &&
        !(requiredServices.contains(ScopeService.ECMASCRIPT) ||
          requiredServices.contains(ScopeService.ECMASCRIPT_DEBUGGER))) {
      if (availableServices.containsKey(ScopeService.ECMASCRIPT)) {
        requiredServices.add(ScopeService.ECMASCRIPT);
      } else {
        requiredServices.add(ScopeService.ECMASCRIPT_DEBUGGER);
      }
    } else {
      debugger = new MockEcmascriptDebugger();
    }

    services = createServices(requiredServices);
    enableServices(services.values());
    initializeServices(services.values());
  }

  private Map<ScopeService, String> buildAvailableServices(HostInfo info) {
    ImmutableMap.Builder<ScopeService, String> services = ImmutableMap.builder();

    for (ScopeProtos.Service service : info.getServiceListList()) {
      ScopeService scopeService = ScopeService.get(service.getName());
      if (scopeService != null) {
        services.put(scopeService, service.getVersion());
      }
    }

    return services.build();
  }

  /**
   * Build and construct the given services.
   *
   * @param services a map of required services and their minimum version
   * @return list of services actually created
   */
  private Map<ScopeService, Service> createServices(Set<ScopeService> services) {
    Map<ScopeService, Service> actualServices = Maps.newTreeMap();

    for (ScopeService requiredService : services) {
      if (availableServices.containsKey(requiredService)) {
        for (ScopeService availableService : availableServices.keySet()) {
          if (availableService == requiredService) {
            actualServices.put(requiredService, requiredService.newInstance(this));
          }
        }
      }
    }

    return actualServices;
  }

  private void initializeServices(Collection<Service> services) {
    for (Service service : services) {
      logger.finer(String.format("Initializing service %s (version %s)",
                                 service.getServiceName(), service.getServiceVersion()));
      service.init();
    }
  }

  public boolean isConnected() {
    return connection != null && connection.isConnected();
  }

  public void shutdown() {
    shutdown = true;  // don't unlock this

    if (isConnected()) {
      connection.close();
    }

    stpThread.shutdown();

    try {
      stpThread.join();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  private void waitForHandshake() throws ScopeException {
    try {
      waitState.waitForHandshake(OperaIntervals.HANDSHAKE_TIMEOUT.getMs());
    } catch (ScopeException e) {
      shutdown();
      throw e;
    }
  }

  /**
   * Gets information on available services and their versions from Opera.  This includes the STP
   * version, core version, platform, operating system, user agent string and a list of available
   * services.
   *
   * @return information about the connected browser's debug capabilities
   */
  private HostInfo getHostInfo() {
    Response response = executeMessage(ScopeMessage.HOST_INFO, null);

    try {
      return HostInfo.parseFrom(response.getPayload());
    } catch (InvalidProtocolBufferException e) {
      throw new CommunicationException("Error while parsing host info", e);
    }
  }

  /**
   * Connects and resets any settings and services that the client used earlier.
   */
  private void connect() {
    ClientInfo.Builder info = ClientInfo.newBuilder().setFormat("protobuf");
    executeMessage(ScopeMessage.CONNECT, info);
  }

  public void enableServices(Collection<Service> services) {
    for (com.opera.core.systems.scope.Service service : services) {
      try {
        enable(service);
      } catch (InvalidProtocolBufferException e) {
        throw new ScopeException("Could not parse the message", e);
      }
    }
  }

  private ServiceResult enable(com.opera.core.systems.scope.Service service)
      throws InvalidProtocolBufferException {
    ServiceSelection.Builder selection = ServiceSelection.newBuilder();
    selection.setName(service.getServiceName());
    Response response = executeMessage(ScopeMessage.ENABLE, selection);
    return ServiceResult.parseFrom(response.getPayload());
  }

  public void quitOpera(final OperaRunner runner) throws IOException {
    if (!isConnected()) {
      return;
    }

    if (exec != null) {
      try {
        if (exec.getActionList().contains("Quit")) {
          exec.action("Quit");
        } else if (exec.getActionList().contains("Exit")) {
          exec.action("Exit");
        }
      } catch (CommunicationException e) {
        throw new IOException("Exception on shutdown: " + e.getMessage(), e);
      }
    }

    // Wait for Opera to quit
    if (runner != null) {
      new ImplicitWait(OperaIntervals.QUIT_RESPONSE_TIMEOUT.getValue())
          .until(new Callable<Boolean>() {
            public Boolean call() {
              return runner.isOperaRunning();
            }
          });

      if (runner.isOperaRunning()) {
        throw new IOException("Opera is still running!");
      }
    }
  }

  public Map<ScopeService, String> getAvailableServices() {
    return availableServices;
  }

  public void quit() throws IOException {
    quit(null);
  }

  public void quit(OperaRunner runner) throws IOException {
    try {
      quitOpera(runner);
    } finally {
      shutdown();
    }
  }

  public boolean onConnected(StpConnection con) {
    logger.finest("onConnect fired");
    if (connection == null) {
      logger.finest("Got StpConnection");
      connection = con;
      return true;
    }
    logger.warning("StpConnection already attached - closing incoming connection.");
    return false;
  }

  public void onServiceList(List<String> services) {
    // Not needed, we also receive them through host info
  }

  public void onWindowLoaded(int id) {
    logger.finest("Window loaded: windowId=" + id);
    waitState.onWindowLoaded(id);
  }

  public void onWindowClosed(int id) {
    logger.finest("Window closed: windowId=" + id);
    waitState.onWindowClosed(id);
  }

  public void onDesktopWindowShown(DesktopWindowInfo info) {
    logger.finest("DesktopWindow shown: windowId=" + info.getWindowID());
    waitState.onDesktopWindowShown(info);
  }

  public void onDesktopWindowUpdated(DesktopWindowInfo info) {
    logger.finest("DesktopWindow updated: windowId=" + info.getWindowID());
    waitState.onDesktopWindowUpdated(info);
  }

  public void onDesktopWindowClosed(DesktopWindowInfo info) {
    logger.finest("DesktopWindow closed: windowId=" + info.getWindowID());
    waitState.onDesktopWindowClosed(info);
  }

  public void onDesktopWindowActivated(DesktopWindowInfo info) {
    logger.finest("DesktopWindow active: windowId=" + info.getWindowID());
    waitState.onDesktopWindowActivated(info);
  }

  public void onDesktopWindowLoaded(DesktopWindowInfo info) {
    logger.finest("DesktopWindow loaded: windowId=" + info.getWindowID());
    waitState.onDesktopWindowLoaded(info);
  }

  public void onDesktopWindowPageChanged(DesktopWindowInfo info) {
    logger.fine("DesktopWindow page changed: windowId=" + info.getWindowID());
    waitState.onDesktopWindowPageChanged(info);
  }

  public void onQuickMenuShown(QuickMenuInfo info) {
    logger.finest("QuickMenu shown: menuName=" + info.getMenuId().getMenuName());
    waitState.onQuickMenuShown(info);
  }

  public void onQuickMenuItemPressed(QuickMenuItemID menuItemID) {
    logger.finest("QuickMenu shown: menuItem=" + menuItemID.getMenuText());
    waitState.onQuickMenuItemPressed(menuItemID);
  }

  // TODO ADD PARAM AGAIN, or just name?
  public void onQuickMenuClosed(QuickMenuID id) {
    logger.finest("QuickMenu closed");//: menuName=" + info.getMenuId().getMenuName());
    waitState.onQuickMenuClosed(id);
  }

  public void onHandshake(boolean stp1) {
    logger.finest("Got Stp handshake!");
    waitState.onHandshake();
  }

  public void onDisconnect() {
    logger.fine("Disconnected, closing STP connection");
    if (isConnected() && !shutdown) {
      waitState.onDisconnected();
      connection = null;
    }
  }

  public void onOperaIdle() {
    logger.finest("idle: Got idle event");
    waitState.onOperaIdle();
  }

  public void onSelftestDone() {
    waitState.onSelftestDone();
  }

  public void waitForWindowLoaded(int activeWindowId, long timeout) {
    waitState.waitForWindowLoaded(activeWindowId, timeout);
  }

  public WaitState waitFor() {
    return waitState;
  }

  public boolean isOperaIdleAvailable() {
    // core version 1.1 introduced some important fixes, and we don't want to use the idle loading
    // scheme without this
    return services.containsKey(ScopeService.CORE) &&
           VersionUtil.compare(services.get(ScopeService.CORE).getServiceVersion(), "1.1") >= 0;
  }

  /**
   * Enables the capturing on OperaIdle events.
   *
   * Sometimes when executing a command OperaIdle events will fire before the response is received
   * for the sent command. This results in missing the Idle events, and later probably hitting a
   * timeout.
   *
   * To prevent this you can call this function which will enable the tracking of any Idle events
   * received between now and when you call waitForOperaIdle(). If Idle events have been received
   * then waitForOperaIdle() will return immediately.
   */
  public void captureOperaIdle() {
    logger.finer("idle: Capturing idle event");
    waitState.captureOperaIdle();
  }

  /**
   * Waits for an OperaIdle event before continuing.
   *
   * If captureOperaIdle() has been called since the last call of waitForOperaIdle(), and one or
   * more OperaIdle events have occurred then this function will return immediately.
   *
   * After calling this function the capturing of OperaIdle events is disabled until the next call
   * of captureOperaIdle()
   *
   * @param timeout Time in milliseconds to wait before aborting
   */
  public void waitForOperaIdle(long timeout) {
    logger.finest("idle: Waiting for (timeout = " + timeout + ")");
    waitState.waitForOperaIdle(timeout);
    logger.finest("idle: Finished waiting");
  }

  public void waitStart() {
    waitState.setWaitEvents(true);
  }

  public int waitForDesktopWindowLoaded(String windowName, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowLoaded(windowName, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowShown(String windowName, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowShown(windowName, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowUpdated(String windowName, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowUpdated(windowName, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowActivated(String windowName, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowActivated(windowName, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowClosed(String windowName, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowClosed(windowName, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowPageChanged(String windowName, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForWindowPageChanged(windowName, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public String waitForMenuShown(String menuName, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForQuickMenuShown(menuName, timeout);
    } catch (Exception e) {
      return "";
    }
  }

  public String waitForMenuClosed(String menuName, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForQuickMenuClosed(menuName, timeout);

    } catch (Exception e) {
      return "";
    }
  }

  public String waitForMenuItemPressed(String menuItemText, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForQuickMenuItemPressed(menuItemText, timeout);

    } catch (Exception e) {
      return "";
    }
  }

  public void onResponseReceived(int tag, Response response) {
    if (isConnected()) {
      logger.finest("Got response");
      if (response != null) {
        waitState.onResponse(tag, response);
      } else {
        waitState.onError(tag);
      }
    }
  }

  public void onException(Exception exception) {
    if (isConnected()) {
      waitState.onException(exception);
      connection = null;
    }
  }

  private Response waitForResponse(int tag, long timeout) {
    try {
      return waitState.waitFor(tag, timeout);
    } catch (CommunicationException e) {
      shutdown();
      throw e;
    }
  }

  /**
   * Close the connection and cleanup the channel
   */
  public void close() {
    connection.close();
  }

  private Command.Builder buildMessage(Message message, ByteString payload) {
    Command.Builder cb = Command.newBuilder();
    cb.setCommandID(message.getID());
    cb.setFormat(0); // protobuf
    cb.setService(message.getServiceName());
    cb.setTag(tagCounter.incrementAndGet());
    cb.setPayload(payload);
    return cb;
  }

  /**
   * Sends a message and wait for the response.
   */
  public Response executeMessage(Message message, Builder<?> builder) {
    return executeMessage(message, builder,
                          OperaIntervals.RESPONSE_TIMEOUT.getMs());
  }

  public Response executeMessage(Message message, Builder<?> builder, long timeout) {
    ByteString payload = (builder != null) ? builder.build().toByteString() : ByteString.EMPTY;
    Command.Builder messageBuilder = buildMessage(message, payload);
    int tag = messageBuilder.getTag();
    connection.send(messageBuilder.build());
    return waitForResponse(tag, timeout);
  }

  public void startStpThread() {
    stpThread.start();
  }

  public void onRequest(int windowId) {
    logger.fine("Window closed: windowId=" + windowId);
    waitState.onRequest(windowId);
  }

  public StpConnection getConnection() {
    return connection;
  }

  public Debugger getDebugger() {
    return debugger;
  }

  public void setDebugger(EcmascriptDebugger debugger) {
    this.debugger = debugger;
  }

  // Getters and setters for the different Scope services:

  public Exec getExec() {
    return exec;
  }

  public void setExec(Exec exec) {
    this.exec = exec;
  }

  public WindowManager getWindowManager() {
    return windowManager;
  }

  public void setWindowManager(WindowManager windowManager) {
    this.windowManager = windowManager;
  }

  public ConsoleLogger getConsoleLogger() {
    return consoleLogger;
  }

  public void setConsoleLogger(ConsoleLogger consoleLogger) {
    this.consoleLogger = consoleLogger;
  }

  public Core getCore() {
    return core;
  }

  public void setCore(Core core) {
    this.core = core;
  }

  public Prefs getPrefs() {
    return prefs;
  }

  public void setPrefs(Prefs prefs) {
    this.prefs = prefs;
  }

  public DesktopWindowManager getDesktopWindowManager() {
    return desktopWindowManager;
  }

  public void setDesktopWindowManager(DesktopWindowManager desktopWindowManager) {
    this.desktopWindowManager = desktopWindowManager;
  }

  public DesktopUtils getDesktopUtils() {
    return desktopUtils;
  }

  public void setDesktopUtils(DesktopUtils desktopUtils) {
    this.desktopUtils = desktopUtils;
  }

  public ScopeSystemInputManager getSystemInputManager() {
    return systemInputManager;
  }

  public void setSystemInputManager(ScopeSystemInputManager manager) {
    this.systemInputManager = manager;
  }

  public CookieManager getCookieManager() {
    return cookieManager;
  }

  public void setCookieManager(CookieManager cookieManager) {
    this.cookieManager = cookieManager;
  }

  @SuppressWarnings("unused")
  public Selftest getSelftest() {
    return selftest;
  }

  public void setSelftest(Selftest selftest) {
    this.selftest = selftest;
  }

}