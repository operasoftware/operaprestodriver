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

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.scope.ScopeCommand;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickMenuItemID;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.ScopeProtos;
import com.opera.core.systems.scope.protos.ScopeProtos.ClientInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.HostInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceResult;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceSelection;
import com.opera.core.systems.scope.protos.SelftestProtos.SelftestOutput;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.ICookieManager;
import com.opera.core.systems.scope.services.ICoreUtils;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IPrefs;
import com.opera.core.systems.scope.services.ISelftest;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.scope.services.ums.SystemInputManager;
import com.opera.core.systems.scope.services.ums.UmsServices;
import com.opera.core.systems.scope.stp.StpConnection;
import com.opera.core.systems.scope.stp.StpThread;
import com.opera.core.systems.util.VersionUtil;

import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Implements the interface to the Scope protocol.
 *
 * @author Deniz Turkoglu <dturkoglu@opera.com>, Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public class ScopeServices implements IConnectionHandler {

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  private ICoreUtils coreUtils;
  private IEcmaScriptDebugger debugger;
  private IOperaExec exec;
  private IWindowManager windowManager;
  private IDesktopWindowManager desktopWindowManager;
  private IDesktopUtils desktopUtils;
  private IPrefs prefs;
  private SystemInputManager systemInputManager;
  private HostInfo hostInfo;
  private ICookieManager cookieManager;
  private ISelftest selftest;

  private Map<String, String> versions;

  private WaitState waitState = new WaitState();
  private StpConnection connection = null;

  private StpThread stpThread;
  boolean shuttingDown = false;

  private List<String> listedServices;

  private AtomicInteger tagCounter;

  private StringBuilder selftestOutput;
  private String profile;

  public Map<String, String> getVersions() {
    return versions;
  }

  public StpConnection getConnection() {
    return connection;
  }

  public IEcmaScriptDebugger getDebugger() {
    return debugger;
  }

  public void setDebugger(IEcmaScriptDebugger debugger) {
    this.debugger = debugger;
  }

  public IOperaExec getExec() {
    return exec;
  }

  public void setExec(IOperaExec exec) {
    this.exec = exec;
  }

  public IWindowManager getWindowManager() {
    return windowManager;
  }

  public void setWindowManager(IWindowManager windowManager) {
    this.windowManager = windowManager;
  }

  public ICoreUtils getCoreUtils() {
    return coreUtils;
  }

  public void setCoreUtils(ICoreUtils coreUtils) {
    this.coreUtils = coreUtils;
  }

  public IPrefs getPrefs() {
    return prefs;
  }

  public void setPrefs(IPrefs prefs) {
    this.prefs = prefs;
  }

  public IDesktopWindowManager getDesktopWindowManager() {
    return desktopWindowManager;
  }

  public void setDesktopWindowManager(IDesktopWindowManager desktopWindowManager) {
    this.desktopWindowManager = desktopWindowManager;
  }

  public IDesktopUtils getDesktopUtils() {
    return desktopUtils;
  }

  public void setDesktopUtils(IDesktopUtils desktopUtils) {
    this.desktopUtils = desktopUtils;
  }

  public SystemInputManager getSystemInputManager() {
    return systemInputManager;
  }

  public void setSystemInputManager(SystemInputManager manager) {
    this.systemInputManager = manager;
  }

  public ICookieManager getCookieManager() {
    return cookieManager;
  }

  public void setCookieManager(ICookieManager cookieManager) {
    this.cookieManager = cookieManager;
  }

  public ISelftest getSelftest() {
    return selftest;
  }

  public void setSelftest(ISelftest selftest) {
    this.selftest = selftest;
  }


  /**
   * Creates the scope server on specified address and port Enables the required services for
   * webdriver
   */
  public ScopeServices(Map<String, String> versions, int port, boolean manualConnect)
      throws IOException {
    this.versions = versions;
    tagCounter = new AtomicInteger();
    stpThread = new StpThread(port, this, new UmsEventHandler(this), manualConnect);
    selftestOutput = new StringBuilder();
  }

  public void init() {
    waitState.setProfile(profile);
    waitForHandshake();

    boolean enableDebugger = (OperaIntervals.ENABLE_DEBUGGER.getValue() != 0);
    hostInfo = getHostInfo();
    createUmsServices(enableDebugger, hostInfo);

    connect();

    List<String> wantedServices = new ArrayList<String>();

    if (enableDebugger) {
      wantedServices.add("ecmascript-debugger");
    }

    wantedServices.add("exec");
    wantedServices.add("window-manager");
    wantedServices.add("core");

    if (versions.containsKey("prefs")) {
      wantedServices.add("prefs");
    }

    if (versions.containsKey("desktop-window-manager")) {
      wantedServices.add("desktop-window-manager");
    }

    if (versions.containsKey("system-input")) {
      wantedServices.add("system-input");
    }

    if (versions.containsKey("desktop-utils")) {
      wantedServices.add("desktop-utils");
    }

    if (versions.containsKey("selftest")) {
      wantedServices.add("selftest");
    }

    //wantedServices.add("console-logger");
    //wantedServices.add("http-logger");
    wantedServices.add("cookie-manager");

    enableServices(wantedServices);

    initializeServices(enableDebugger);
  }

  private void initializeServices(boolean enableDebugger) {
    exec.init();
    windowManager.init();
    if (versions.containsKey("core") && coreUtils != null) {
      coreUtils.init();
    }

    if (versions.containsKey("prefs") && prefs != null) {
      prefs.init();
    }
    if (versions.containsKey("desktop-window-manager") && desktopWindowManager != null) {
      desktopWindowManager.init();
    }
    if (versions.containsKey("system-input") && systemInputManager != null) {
      systemInputManager.init();
    }
    if (versions.containsKey("desktop-utils") && desktopUtils != null) {
      desktopUtils.init();
    }

    if (enableDebugger) {
      debugger.init();
    }
  }

  public void shutdown() {
    /*
     * This can get called twice if we get a DISCONNECTED exception when
     * closing down Opera. Check if we're already shutting down and bail.
     */
    if (shuttingDown) {
      return;
    }

    shuttingDown = true;

    if (connection != null) {
      connection.close();
    }

    stpThread.shutdown();

    try {
      stpThread.join();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  private void waitForHandshake() throws WebDriverException {
    try {
      waitState.waitForHandshake(OperaIntervals.HANDSHAKE_TIMEOUT.getValue());
    } catch (WebDriverException e) {
      shutdown();
      throw e;
    }
  }

  private HostInfo getHostInfo() {
    Response response = executeCommand(ScopeCommand.HOST_INFO, null);
    try {
      return HostInfo.parseFrom(response.getPayload());
    } catch (InvalidProtocolBufferException ex) {
      throw new WebDriverException("Error while parsing host info");
    }
  }

  private void createUmsServices(boolean enableDebugger, HostInfo info) {
    new UmsServices(this, info);
    if (!enableDebugger) {
      debugger = new IEcmaScriptDebugger() {

        public void setRuntime(RuntimeInfo runtime) {
        }

        public Object scriptExecutor(String script, Object... params) {
          return null;
        }

        public void removeRuntime(int runtimeId) {
        }

        public List<String> listFramePaths() {
          return null;
        }

        public void init() {
        }

        public int getRuntimeId() {
          return 0;
        }

        public Integer getObject(String using) {
          return null;
        }

        public Integer executeScriptOnObject(String using, int objectId) {
          return null;
        }

        public Object executeScript(String using, boolean responseExpected) {
          return null;
        }

        public String executeJavascript(String using, boolean responseExpected) {
          return null;
        }

        public String executeJavascript(String using) {
          return null;
        }

        public List<Integer> examineObjects(Integer id) {
          return null;
        }

        public void cleanUpRuntimes() {
        }

        public void cleanUpRuntimes(int windowId) {
        }

        public void changeRuntime(String framePath) {
        }

        public Object callFunctionOnObject(String using, int objectId,
                                           boolean responseExpected) {
          return null;
        }

        public String callFunctionOnObject(String using, int objectId) {
          return null;
        }

        public void addRuntime(RuntimeInfo info) {
        }

        public void releaseObjects() {
        }

        public boolean updateRuntime() {
          return false;
        }

        public void resetRuntimesList() {
        }

        public void readyStateChanged(ReadyStateChange change) {
        }

        public void releaseObject(int objectId) {
        }

        public void resetFramePath() {
        }

        public void changeRuntime(int index) {
        }

        public String executeJavascript(String using, Integer windowId) {
          return null;
        }

        public Object examineScriptResult(Integer id) {
          return null;
        }

        public void setDriver(OperaDriver driver) {
        }
      };
    }
  }

  private void connect() {
    ClientInfo.Builder info = ClientInfo.newBuilder().setFormat("protobuf");
    executeCommand(ScopeCommand.CONNECT, info);
  }

  public void enableServices(List<String> requiredServices) {
    for (String requiredService : requiredServices) {
      try {
        if (getListedServices().contains(requiredService)) {
          enable(requiredService);
        }
      } catch (InvalidProtocolBufferException e) {
        throw new WebDriverException("Could not parse the message");
      }
    }
  }

  private ServiceResult enable(String serviceName)
      throws InvalidProtocolBufferException {
    ServiceSelection.Builder selection = ServiceSelection.newBuilder();
    selection.setName(serviceName);
    Response response = executeCommand(ScopeCommand.ENABLE, selection);
    return ServiceResult.parseFrom(response.getPayload());
  }

  public void quitOpera(OperaRunner runner, int pid) {
    try {
      if (exec.getActionList().contains("Quit")) {
        exec.action("Quit");
      } else {
        exec.action("Exit");
      }
    } catch (Exception e) {
      // We expect a CommunicationException here, because as Opera is shutting
      // down the connection will be closed.
      if (!(e instanceof CommunicationException)) {
        logger.info("Caught exception when trying to shut down: " + e.getMessage());
      }
    }

    if (runner != null && pid > 0) {
      long interval = OperaIntervals.QUIT_POLL_INTERVAL.getValue();
      long timeout = OperaIntervals.QUIT_RESPONSE_TIMEOUT.getValue();
      while (runner.isOperaRunning(pid) && timeout > 0) {
        try {
          Thread.sleep(interval);
        } catch (InterruptedException e) {
          // ignore
        }
        timeout -= interval;
      }
    }
  }

  public void quit() {
    quit(null, 0);
  }

  public void quit(OperaRunner runner, int pid) {
    quitOpera(runner, pid);
    shutdown();
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
    setListedServices(services);
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
    if (connection != null) {
      if (!shuttingDown) {
        waitState.onDisconnected();
        logger.finest("Cleaning up...");
        connection = null;
      }
    }
  }

  public void onOperaIdle() {
    logger.finest("Got idle event");
    waitState.onOperaIdle();
  }

  public void onSelftestOutput(SelftestOutput output) {
    selftestOutput = selftestOutput.append(output.getOutput());
  }

  public void onSelftestDone() {
    String results = selftestOutput.toString();
    selftestOutput = new StringBuilder();
    waitState.onSelftestDone(results);
  }

  public String selftest(List<String> modules, long timeout) {
    if (selftest == null) {
      throw new UnsupportedOperationException("selftest service is not supported");
    }

    selftest.runSelftests(modules);
    return waitState.waitForSelftestDone(timeout);
  }

  public void waitForWindowLoaded(int activeWindowId, long timeout) {
    logger.finest("waitForWindowLoaded with params activeWindowId=" +
                  activeWindowId + " timeout=" + timeout);
    waitState.waitForWindowLoaded(activeWindowId, timeout);
  }

  public boolean isOperaIdleAvailable() {
    for (ScopeProtos.Service service : hostInfo.getServiceListList()) {
      if (service.getName().equals("core")) {
        String version = service.getVersion();

        // Version 1.1 introduced some important fixes, and we don't want to use idle detection
        // without this.
        boolean ok = VersionUtil.compare(version, "1.1") >= 0;
        logger.finer("core service version check: " + ok + " (" + version + ")");
        return ok;
      }
    }

    logger.severe("core service not found");
    return false;
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
    logger.fine("Capturing idle event");
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
    logger.finer("idle: Waiting for (timeout = " + timeout + ")");
    waitState.waitForOperaIdle(timeout);
    logger.finer("idle: Finished waiting");
  }

  public void waitStart() {
    waitState.setWaitEvents(true);
  }

  public int waitForDesktopWindowLoaded(String win_name, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowLoaded(win_name, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowShown(String win_name, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowShown(win_name, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowUpdated(String win_name, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowUpdated(win_name, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowActivated(String win_name, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowActivated(win_name, timeout);
    } catch (Exception e) {
      return 0;
    }
  }

  public int waitForDesktopWindowClosed(String win_name, long timeout) {
    waitState.setWaitEvents(false);
    try {
      return waitState.waitForDesktopWindowClosed(win_name, timeout);
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
    if (connection != null) {
      logger.finest("Got response");
      if (response != null) {
        waitState.onResponse(tag, response);
      } else {
        waitState.onError(tag);
      }
    }
  }

  public void onException(Exception ex) {
    logger.finest("Got exception");
    if (connection != null) {
      waitState.onException(ex);
      connection = null;
    }
  }

  public String getMinVersionFor(String service) {
    return versions.get(service);
  }

  private Response waitForResponse(int tag, long timeout) {
    try {
      return waitState.waitFor(tag, timeout);
    } catch (WebDriverException e) {
      shutdown();
      throw e;
    }
  }

  public void setListedServices(java.util.List<String> services) {
    listedServices = services;
  }

  public List<String> getListedServices() {
    return listedServices;
  }

  /**
   * Close the connection and cleanup the channel
   */
  public void close() {
    connection.close();
  }

  private Command.Builder buildCommand(ICommand command, ByteString payload) {
    Command.Builder cb = Command.newBuilder();
    cb.setCommandID(command.getCommandID());
    cb.setFormat(0); // protobuf
    cb.setService(command.getServiceName());
    cb.setTag(tagCounter.incrementAndGet());
    cb.setPayload(payload);
    return cb;
  }

  /**
   * Sends a command and wait for the response.
   */
  public Response executeCommand(ICommand command, Builder<?> builder) {
    return executeCommand(command, builder,
                          OperaIntervals.RESPONSE_TIMEOUT.getValue());
  }

  public Response executeCommand(ICommand command, Builder<?> builder,
                                 long timeout) {
    ByteString payload = (builder != null) ? builder.build().toByteString()
                                           : ByteString.EMPTY;
    Command.Builder commandBuilder = buildCommand(command, payload);
    int tag = commandBuilder.getTag();
    connection.send(commandBuilder.build());
    return waitForResponse(tag, timeout);
  }

  public void startStpThread() {
    stpThread.start();
  }

  public void onRequest(int windowId) {
    logger.info("Window closed: windowId=" + windowId);
    waitState.onRequest(windowId);
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }
}
