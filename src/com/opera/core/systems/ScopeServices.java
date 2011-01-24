/* Copyright (C) 2009-2010 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriverException;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.scope.ScopeCommand;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.ClientInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.HostInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceResult;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceSelection;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.ICookieManager;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.scope.services.IPrefs;
import com.opera.core.systems.scope.services.ums.SystemInputManager;
import com.opera.core.systems.scope.services.ums.UmsServices;
import com.opera.core.systems.scope.stp.StpConnection;
import com.opera.core.systems.scope.stp.StpThread;

public class ScopeServices implements IConnectionHandler {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private IEcmaScriptDebugger debugger;
	private IOperaExec exec;
	private IWindowManager windowManager;
	private IDesktopWindowManager desktopWindowManager; 
	private IDesktopUtils desktopUtils; 
	private IPrefs prefs; 
	private SystemInputManager systemInputManager;
	private ICookieManager cookieManager;
	
	private Map<String, String> versions;

	private List<IConsoleListener> listeners;

	private WaitState waitState = new WaitState();
	private StpConnection connection = null;

	private StpThread stpThread;
	boolean shuttingDown = false;

	private List<String> listedServices;

	private AtomicInteger tagCounter;
	
	public ICookieManager getCookieManager() {
		return cookieManager;
	}

	public void setCookieManager(ICookieManager cookieManager) {
		this.cookieManager = cookieManager;
	}

	
	public Map<String, String> getVersions() {
		return versions;
	}

	public StpConnection getConnection() {
		return connection;
	}
	
	public List<IConsoleListener> getConsoleListeners() {
		return listeners;
	}
	
	public void addConsoleListener(IConsoleListener listener) {
		listeners.add(listener);
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

	IPrefs getPrefs() {
		return prefs;
	}

	public void setPrefs(IPrefs prefs) {
		this.prefs = prefs;
	}

	IDesktopWindowManager getDesktopWindowManager() {
		return desktopWindowManager;
	}

	public void setDesktopWindowManager(IDesktopWindowManager desktopWindowManager) {
		this.desktopWindowManager = desktopWindowManager;
	}

	IDesktopUtils getDesktopUtils() {
		return desktopUtils;
	}

	public void setDesktopUtils(IDesktopUtils desktopUtils) {
		this.desktopUtils = desktopUtils;
	}

	SystemInputManager getSystemInputManager() {
		return systemInputManager;
	}
	
	public void setSystemInputManager(SystemInputManager manager) {
		this.systemInputManager = manager; 
	}
	
	/**
	 * Creates the scope server on specified address and port
	 * Enables the required services for webdriver
	 * @param ipAddress
	 * @param portNumber
	 * @param intervals 
	 */
	public ScopeServices(Map<String, String> versions) throws IOException {
		this.versions = versions;
		tagCounter = new AtomicInteger();
		listeners = new LinkedList<IConsoleListener>();
		stpThread = new StpThread((int) OperaIntervals.SERVER_PORT.getValue(), this, new UmsEventHandler(this));
	}

	public void init() {
		waitForHandshake();

		boolean enableDebugger = (OperaIntervals.ENABLE_DEBUGGER.getValue() != 0);
		HostInfo info = getHostInfo();
		createUmsServices(enableDebugger, info);

		connect();

		List<String> wantedServices = new ArrayList<String>();

		if (enableDebugger)
			wantedServices.add("ecmascript-debugger");

		wantedServices.add("exec");
		wantedServices.add("window-manager");

		if (versions.containsKey("prefs"))
			wantedServices.add("prefs");

		if (versions.containsKey("desktop-window-manager"))
			wantedServices.add("desktop-window-manager");
		
		if (versions.containsKey("system-input"))
			wantedServices.add("system-input");

		if (versions.containsKey("desktop-utils"))
			wantedServices.add("desktop-utils");
		
		wantedServices.add("console-logger");
//		wantedServices.add("http-logger");
		wantedServices.add("core");
		wantedServices.add("cookie-manager");
		
		enableServices(wantedServices);

		initializeServices(enableDebugger);
	}
        
	private void initializeServices(boolean enableDebugger) {
		exec.init();
		windowManager.init();

		if (versions.containsKey("prefs") && prefs != null)
			prefs.init();

		if (versions.containsKey("desktop-window-manager") && desktopWindowManager != null)
			desktopWindowManager.init();

		if (versions.containsKey("system-input") && systemInputManager != null)
			systemInputManager.init();

		if (versions.containsKey("desktop-utils") && desktopUtils != null)
			desktopUtils.init();

		if(enableDebugger)
			debugger.init();
	}

	public void shutdown() {
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
		if (!enableDebugger)
			debugger = new PseudoEcmaScriptDebugger();
	}

	private void connect() {
		ClientInfo.Builder info = ClientInfo.newBuilder().setFormat("protobuf");
		executeCommand(ScopeCommand.CONNECT, info);
	}

	public void enableServices(List<String> requiredServices) {
		for (String requiredService : requiredServices) {
			try {
				if (getListedServices().contains(requiredService))
					enable(requiredService);
			} catch (InvalidProtocolBufferException e) {
				throw new WebDriverException("Could not parse the message");
			}
		}
	}
	
	private ServiceResult enable(String serviceName) throws InvalidProtocolBufferException {
		ServiceSelection.Builder selection = ServiceSelection.newBuilder();
		selection.setName(serviceName);
		Response response = executeCommand(ScopeCommand.ENABLE, selection);
		return ServiceResult.parseFrom(response.getPayload());
	}

	public void quit() {
		try {
			if (exec.getActionList().contains("Quit"))
				exec.action("Quit");
			else
				exec.action("Exit");
		} catch (Exception e) {
			logger.info("Caught exception when trying to shut down (cannot send quit). : " + e.getMessage());
		}

		shutdown();
	}

	public boolean onConnected(StpConnection con) {
		if (connection == null) {
			logger.fine("Got StpConnection");
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
		logger.fine("Window loaded: windowId=" + id);
		waitState.onWindowLoaded(id);
	}

	public void onWindowClosed(int id) {
		logger.fine("Window closed: windowId=" + id);
		waitState.onWindowClosed(id);
	}
	
	public void onDesktopWindowShown(DesktopWindowInfo info) {
		logger.fine("DesktopWindow shown: windowId=" + info.getWindowID());
		waitState.onDesktopWindowShown(info);
	}

	public void onDesktopWindowUpdated(DesktopWindowInfo info) {
		logger.fine("DesktopWindow updated: windowId=" + info.getWindowID());
		waitState.onDesktopWindowUpdated(info);
	}

	public void onDesktopWindowClosed(DesktopWindowInfo info) {
		logger.fine("DesktopWindow closed: windowId=" + info.getWindowID());
		waitState.onDesktopWindowClosed(info);
	}

	public void onDesktopWindowActivated(DesktopWindowInfo info) {
		logger.fine("DesktopWindow active: windowId=" + info.getWindowID());
		waitState.onDesktopWindowActivated(info);
	}
	
	public void onDesktopWindowLoaded(DesktopWindowInfo info) {
		logger.fine("DesktopWindow loaded: windowId=" + info.getWindowID());
		waitState.onDesktopWindowLoaded(info);
	}

	public void onHandshake(boolean stp1) {
		logger.fine("Got Stp handshake!");
		waitState.onHandshake();
	}
        
	public void onDisconnect()
        {
            logger.fine("Disconnected, closing StpConnection.");
            if (connection != null)
            {
                if (!shuttingDown)
                {
                    waitState.onDisconnected();
                    logger.fine("Cleaning up...");
                    connection = null;
                }
            }
	}
	
	public void onOperaIdle() {
		logger.fine("Got Opera Idle event!");
		waitState.onOperaIdle();
	}
	
	public void waitForWindowLoaded(int activeWindowId, long timeout) {
		waitState.waitForWindowLoaded(activeWindowId, timeout);
	}
	
	public boolean isOperaIdleAvailable(){
		for(String service : listedServices){if(service.equals("core")) return true;}
		return false;
	}

	public void waitForOperaIdle(long timeout) {
		logger.info("====================================");
		logger.info("|       WAITING FOR OPERA_IDLE     |");
		logger.info("====================================");
		
		waitState.waitForOperaIdle(timeout);
  }

	public void waitStart() {
		waitState.setWaitEvents(true);
	}
	
	public int waitForDesktopWindowLoaded(String win_name, long timeout) {
		waitState.setWaitEvents(false);
		try {
			return waitState.waitForDesktopWindowLoaded(win_name, timeout);
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	public int waitForDesktopWindowShown(String win_name, long timeout) {
		waitState.setWaitEvents(false);
		try {
			return waitState.waitForDesktopWindowShown(win_name, timeout);
		}
		catch (Exception e) {
			return 0;
		}
	}

	public int waitForDesktopWindowUpdated(String win_name, long timeout) {
		waitState.setWaitEvents(false);
		try {
			return waitState.waitForDesktopWindowUpdated(win_name, timeout);
		}
		catch (Exception e) {
			return 0;
		}
	}

	public int waitForDesktopWindowActivated(String win_name, long timeout) {
		waitState.setWaitEvents(false);
		try {
			return waitState.waitForDesktopWindowActivated(win_name, timeout);
		}
		catch (Exception e) {
			return 0;
		}
	}

	public int waitForDesktopWindowClosed(String win_name, long timeout) {
		waitState.setWaitEvents(false);
		try	{
			return waitState.waitForDesktopWindowClosed(win_name, timeout);
		}
		catch (Exception e) {
			return 0;
		}
	}

	public void onResponseReceived(int tag, Response response) {
            if (connection != null)
            {
                logger.fine("Got response.");
                if (response != null) {
                    waitState.onResponse(tag, response);
                } else {
                    waitState.onError(tag);
                }
            }
	}

	public void onException(Exception ex) {
            logger.fine("Got exception");
            if (connection != null)
            {
                waitState.onException(ex);
                connection = null;
            }
	}

	public String getMinVersionFor(String service) {
		return versions.get(service);
	}

	public void onBinaryStopped(int exitValue) {
		if (connection != null) {
			if (!shuttingDown) {
				waitState.onBinaryExit(exitValue);
			}
		}
	}

	public boolean isConnected() {
		return connection != null;
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
     *
	 * @param serviceName
	 * @param message
	 */
	public Response executeCommand(ICommand command, Builder<?> builder) {
		return executeCommand(command, builder, OperaIntervals.RESPONSE_TIMEOUT.getValue());
	}

	public Response executeCommand(ICommand command, Builder<?> builder, long timeout) {
		ByteString payload = (builder != null) ? builder.build().toByteString() : ByteString.EMPTY;
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
}

