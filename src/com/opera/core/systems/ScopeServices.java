/* Copyright (C) 2009-2010 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriverException;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.InvalidProtocolBufferException;

import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.scope.ScopeCommand;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.ScopeProtos.ClientInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.HostInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceResult;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceSelection;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.scope.services.ums.UmsServices;
import com.opera.core.systems.scope.stp.StpThread;
import com.opera.core.systems.scope.stp.StpConnection;

import com.opera.core.systems.scope.handlers.PbActionHandler;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import java.io.IOException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ScopeServices implements IConnectionHandler {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private IEcmaScriptDebugger debugger;
	private IOperaExec exec;
	private IWindowManager windowManager;
	private ScopeActions actionHandler;
	private Map<String, String> versions;
	private List<IConsoleListener> listeners;

	private WaitState waitState = new WaitState();
	private StpConnection connection = null;
	private StpThread stpThread;
	boolean shuttingDown = false;

	private List<String> listedServices;

	private AtomicInteger tagCounter;

	public ScopeActions getActionHandler() {
            return actionHandler;
	}
	
	public List<IConsoleListener> getConsoleListeners() {
		return listeners;
	}
	
	public void addConsoleListener(IConsoleListener listener) {
		listeners.add(listener);
	}

	public void setActionHandler(ScopeActions actionHandler) {
		this.actionHandler = actionHandler;
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
            stpThread = new StpThread((int)OperaIntervals.SERVER_PORT.getValue(), this, new UmsEventHandler(this));
        }
        
        public void init() throws WebDriverException
        {
            waitForHandshake();
            
            boolean enableDebugger = (OperaIntervals.ENABLE_DEBUGGER.getValue() != 0);
            HostInfo info = getHostInfo();
            createUmsServices(enableDebugger, info);

            connect();

            List<String> wantedServices = new ArrayList<String>();

            if(enableDebugger)
                wantedServices.add("ecmascript-debugger");

            wantedServices.add("exec");
            wantedServices.add("window-manager");
            wantedServices.add("console-logger");
            // wantedServices.add("http-logger");

            enableServices(wantedServices);

            initializeServices();
        }
        
	private void initializeServices() {
            exec.init();
            windowManager.init();
            debugger.init();
	}


        public void shutdown()
        {
            if (connection != null) {
                connection.close();
            }
            stpThread.shutdown();
            try {
                stpThread.join();
            } catch (InterruptedException ex) {
                // ignored.
            }
        }

        private void waitForHandshake() throws WebDriverException
        {
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
            if(!enableDebugger)
                    debugger = new PseudoEcmaScriptDebugger();
	}
        
	private void connect() {
            ClientInfo.Builder info = ClientInfo.newBuilder().setFormat("protobuf");
            executeCommand(ScopeCommand.CONNECT, info);
	}

        public void enableServices(List<String> requiredServices) {
            for (String requiredService : requiredServices) {
                try {
                    if(getListedServices().contains(requiredService))
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
	
        /**
         * Open an URL.
         *
         * @param url URL to load
         * @param timeout in milliseconds
         * @return the HTTP response code, or throws a WebDriverException.
         */
        int openUrl(String url, long timeout)
        {
            if (connection == null)
            	throw new CommunicationException("Unable to open URL because Opera is not connected.");

            int windowId = windowManager.getActiveWindowId();

            actionHandler.get(url);
            waitState.waitForWindowLoaded(windowId, timeout);
            int ret = windowManager.getLastHttpResponseCode().getAndSet(0);
            return ret;
        }

        public void quit() {
            shuttingDown = true;
            try {
                if(exec.getActionList().contains("Quit"))
                    exec.action("Quit");
                else
                    exec.action("Exit");
            } catch (Exception e) {
                logger.info("Caught exception when trying to shut down (cannot send quit).");
            }

            shutdown();
        }

        public boolean onConnected(StpConnection con)
        {
            if (connection == null)
            {
                logger.info("Got StpConnection");
                connection = con;
                return true;
            }
            logger.warning("StpConnection already attached - closing incoming connection.");
            return false;
        }
        
        public void onServiceList(java.util.List<String> services) {
            setActionHandler(new PbActionHandler(this));
            setListedServices(services);
        }
        
        public void onWindowLoaded(int id)
        {
            logger.info("Window loaded: windowId=" + id);
            waitState.onWindowLoaded(id);
        }

        public void onWindowClosed(int id)
        {
            logger.info("Window closed: windowId=" + id);
            waitState.onWindowClosed(id);
        }

        public void onHandshake(boolean stp1)
        {
            logger.info("Got Stp handshake!");
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
            if (connection != null)
            {
                if (!shuttingDown)
                {
                    waitState.onBinaryExit(exitValue);
                }
            }
        }

        public boolean isConnected()
        {
            return connection != null;
        }

        
	private Response waitForResponse(int tag, long timeout)
        {
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

        public List<String> getListedServices()
        {
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
            cb.setService(command.getService());
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

       	//FIXME (ask) move the tag control logic from here to another layer?
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
}
