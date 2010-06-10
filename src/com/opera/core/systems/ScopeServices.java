/* Copyright (C) 2009-2010 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriverException;
import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.InvalidProtocolBufferException;

import com.opera.core.systems.model.ICommand;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.scope.beans.Runtime;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.Parser;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.ClientInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.HostInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceResult;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceSelection;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.scope.services.ums.UmsServices;
import com.opera.core.systems.scope.services.xml.XmlServices;
import com.opera.core.systems.scope.stp.StpThread;
import com.opera.core.systems.scope.stp.StpConnection;
import com.opera.core.systems.scope.internal.OperaBinary;

import com.opera.core.systems.scope.handlers.PbActionHandler;
import com.opera.core.systems.scope.handlers.UmsActionHandler;
import com.opera.core.systems.scope.handlers.XmlActionHandler;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import java.io.IOException;

import java.util.logging.Logger;

public class ScopeServices implements IConnectionHandler {
	
        private final Logger logger = Logger.getLogger(this.getClass().getName());
    
	private IEcmaScriptDebugger debugger;
	private IOperaExec exec;
	private IWindowManager windowManager;
	private ScopeActions actionHandler;
	private Parser parser;
	private Map<String, String> versions;
	private List<IConsoleListener> listeners;
        private OperaBinary opera;
	
        private WaitState waitState = new WaitState();
        private StpConnection connection = null;
        private StpThread stpThread = null;
	boolean running = true;
        boolean shuttingDown = false;

        private List<String> listedServices;

	private int tagCounter;

	
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

	public Parser getParser() {
		return parser;
	}
	
	public enum ScopeCommand implements ICommand {
            CONNECT(3),
            DISCONNECT(4),
            ENABLE(5),
            DISABLE(6),
            HOST_INFO(10),
            MESSAGE_INFO(11);

            private int code;
            private static final Map<Integer,ScopeCommand> lookup = new HashMap<Integer,ScopeCommand>();

            static {
            for(ScopeCommand command : EnumSet.allOf(ScopeCommand.class))
                lookup.put(command.getCommandID(), command);
            }

            private ScopeCommand(int code) {
                this.code = code;
            }
            
            public int getCommandID() {
                return code;
            }

            public static ScopeCommand get(int code) {
                return lookup.get(code);
            }
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
            listeners = new LinkedList<IConsoleListener>();
            stpThread = new StpThread((int)OperaIntervals.SERVER_PORT.getValue(), this, new UmsEventHandler(this));
        }
        
        public void init(OperaBinary binary) throws WebDriverException
        {
            this.opera = binary;
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

            if (opera != null)
            {
                opera.shutdown();
                opera = null;
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
            Response response = executeCommand(ScopeCommand.HOST_INFO, "scope", null);
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
        
        private void createXmlServices(boolean enableDebugger) {
            new XmlServices(this);
            if(!enableDebugger)
                    debugger = new PseudoEcmaScriptDebugger();
	}

	
        public List<String> getListedServices()
        {
            return listedServices;
        }
        
	private void connect() {
            ClientInfo.Builder info = ClientInfo.newBuilder().setFormat("protobuf");
            executeCommand(ScopeCommand.CONNECT, "scope", info);
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
            Response response = executeCommand(ScopeCommand.ENABLE, "scope", selection);
            return ServiceResult.parseFrom(response.getPayload());
	}
	

	private void initializeServices() {
            exec.init();
            windowManager.init();
            debugger.init();
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
                throw new WebDriverException("Unable to open URL because Opera is not connected.");

            int windowId = windowManager.getActiveWindowId();

            actionHandler.get(url);
            waitState.waitForWindowLoaded(windowId, timeout);
            int ret = windowManager.getLastHttpResponseCode().getAndSet(0);
            System.out.println("openURL => " + ret);
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
            
            if (opera != null)
            {
                opera.shutdown();
            }
            
            /*
            connection.sendQuit();
            */
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
            opera = null;
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

        /**
	 * Parse the services message
	 * @param serviceMessage
	 */
	public void setListedServices(java.util.List<String> services) {
            listedServices = services;
	}
	
	/**
	 * Close the connection and cleanup the channel
	 */
	public void close() {
            connection.close();
	}

	
	private Command.Builder buildCommand(int commandId, String service, ByteString payload){
		Command.Builder command  = Command.newBuilder();
		command.setCommandID(commandId); //connect
		command.setFormat(0); //protobuf
		command.setService(service);
		command.setTag(++tagCounter);
		
		command.setPayload(payload);
		return command;
	}
	
	/**
	 * Sends a command
	 * @param serviceName
	 * @param message
	 */
	public Response executeCommand(ICommand command, String service, Builder<?> builder) {
		return executeCommand(command, service, builder, OperaIntervals.RESPONSE_TIMEOUT.getValue());
	}

       	//FIXME (ask) move the tag control logic from here to another layer?
	public Response executeCommand(ICommand command, String service, Builder<?> builder, long timeout) {
            ByteString payload = (builder != null) ? builder.build().toByteString() : ByteString.EMPTY; 
            Command.Builder commandBuilder = buildCommand(command.getCommandID(), service, payload);
            int tag = commandBuilder.getTag();
            connection.send(commandBuilder.build());
            return waitForResponse(tag, timeout);
	}

        /**
	 * Send a message directly
	 * @param message
	 */
        @Deprecated
	public void send(String message) {
            connection.send(message);
	}

	/**
	 * Send a message prepending xml tag
	 * @param serviceName
	 * @param message
	 */
        @Deprecated
        public void sendXmlMessage(String serviceName, String message) {
            String xml = (serviceName + " <?xml version=\"1.0\"?>" + message);
            connection.send(xml);
	}
}
