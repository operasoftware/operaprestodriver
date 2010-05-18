/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import org.openqa.selenium.WebDriverException;

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

import com.opera.core.systems.scope.handlers.PbActionHandler;
import com.opera.core.systems.scope.handlers.UmsActionHandler;
import com.opera.core.systems.scope.handlers.XmlActionHandler;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

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
	
	private ScopeConnection connection = null;
        private StpThread stpThread;
	boolean running = true;
        boolean handShaken = false;
        AtomicBoolean isWairing = new AtomicBoolean(false);

	public ScopeConnection getConnection() {
		return connection;
	}
	
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
		stpThread = new StpThread((int)OperaIntervals.SERVER_PORT.getValue(), this);
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

        public void waitForHandshake()
        {
            synchronized (this)
            {
                try {
                    wait(OperaIntervals.HANDSHAKE_TIMEOUT.getValue());
                    if (!handShaken)
                        throw new FatalException("Handshake timed out.");
                } catch (InterruptedException ex) {
                    throw new FatalException("Handshake interrupted.");
                }
            }
        }

        public void secondaryInit()
        {
            boolean isStp1 = connection.isStp1();
            boolean enableDebugger = (OperaIntervals.ENABLE_DEBUGGER.getValue() != 0);
            if(isStp1) {
                connection.switchToStp1();
                HostInfo info = getHostInfo();
                createUmsServices(enableDebugger, info);
                connection.setEventHandler(new UmsEventHandler(this));

                connect();
                if(enableDebugger)
                    enableServices("ecmascript-debugger");
                enableServices("exec", "window-manager", "console-logger");//, "http-logger");
            }
            else {
                List<String> services = new ArrayList<String>();
                parser = new Parser();
                connection.switchToStp0();
                createXmlServices(enableDebugger);
                connection.setEventHandler(new EventHandler(this));

                if(enableDebugger)
                    services.add("ecmascript-debugger");

                services.add("exec");
                // services.add("console-logger");
                //services.add("http-logger");
                services.add("window-manager");
                connection.enableServices(services);
            }
            initializeServices();
	}
	
	private HostInfo getHostInfo() {
		Response response = connection.executeCommand(ScopeCommand.HOST_INFO, "scope", null);
		try {
			return HostInfo.parseFrom(response.getPayload());
		} catch (InvalidProtocolBufferException ex) {
			throw new WebDriverException("Error while parsing host info");
		}
	}
	
	private void createUmsServices(boolean enableDebugger, HostInfo info) {
		new UmsServices(this, info);
		if(!enableDebugger)
			debugger = createPseudoDebugger();
	}
	
	private IEcmaScriptDebugger createPseudoDebugger() {
		return new IEcmaScriptDebugger() {
			
			public void setRuntime(RuntimeInfo runtime) {
				// TODO Auto-generated method stub
				
			}
			
			public void setRuntime(Runtime runtime) {
				// TODO Auto-generated method stub
				
			}
			
			public Object scriptExecutor(String script, Object... params) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void removeRuntime(int runtimeId) {
				// TODO Auto-generated method stub
				
			}
			
			public List<String> listFramePaths() {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void init() {
				// TODO Auto-generated method stub
				
			}
			
			public int getRuntimeId() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public Integer getObject(String using) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public Integer executeScriptOnObject(String using, int objectId) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public Object executeScript(String using, boolean responseExpected) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public String executeJavascript(String using, boolean responseExpected) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public String executeJavascript(String using) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public List<Integer> examineObjects(Integer id) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void cleanUpRuntimes() {
				// TODO Auto-generated method stub
				
			}
			
			public void cleanUpRuntimes(int windowId) {
				// TODO Auto-generated method stub
				
			}
			
			public void changeRuntime(int index) {
				// TODO Auto-generated method stub
				
			}
			
			public void changeRuntime(String framePath) {
				// TODO Auto-generated method stub
				
			}
			
			public Object callFunctionOnObject(String using, int objectId,
					boolean responseExpected) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public String callFunctionOnObject(String using, int objectId) {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void addRuntime(RuntimeInfo info) {
				// TODO Auto-generated method stub
				
			}
			
			public void addRuntime(Runtime started) {
				// TODO Auto-generated method stub
				
			}

			public void releaseObjects() {
				// TODO Auto-generated method stub
				
			}

			public boolean updateRuntime() {
				// TODO Auto-generated method stub
				return false;
			}

			public void resetRuntimesList() {
				// TODO Auto-generated method stub
				
			}
		};
	}

	private void connect() {
		ClientInfo.Builder info = ClientInfo.newBuilder().setFormat("protobuf");
		connection.executeCommand(ScopeCommand.CONNECT, "scope", info);
	}
	
	public void enableServices(String... requiredServices){
		for (String requiredService : requiredServices) {
			try {
				if(connection.getListedServices().contains(requiredService))
					enable(requiredService);
			} catch (InvalidProtocolBufferException e) {
				throw new WebDriverException("Could not parse the message");
			}
		}
	}
	
	private ServiceResult enable(String serviceName) throws InvalidProtocolBufferException {
		ServiceSelection.Builder selection = ServiceSelection.newBuilder();
		selection.setName(serviceName);
		
		Response response = connection.executeCommand(ScopeCommand.ENABLE, "scope", selection);
		
		return ServiceResult.parseFrom(response.getPayload());
	}
	
	private void createXmlServices(boolean enableDebugger) {
		new XmlServices(this);
		if(!enableDebugger)
			debugger = createPseudoDebugger();
	}

	private void initializeServices() {
		exec.init();
		windowManager.init();
		debugger.init();
                
                ScopeActions scopeActionHandler;
		List<String> listedServices = getConnection().getListedServices();
		if(listedServices.contains("stp-1")) {
			scopeActionHandler = new PbActionHandler(this);
		} else {
			if (listedServices.contains("core-2-4")) {
				scopeActionHandler = new UmsActionHandler(this);
			} else {
				scopeActionHandler = new XmlActionHandler(this);
			}
		}

		setActionHandler(scopeActionHandler);
	}

        public boolean onConnected(StpConnection con)
        {
            if (connection == null)
            {
                handShaken = false;
                logger.info("Got StpConnection");
                connection = new ScopeConnection(con, this);
                connection.init();
                
                return true;
            }
            logger.warning("StpConnection already attached - closing incoming connection.");
            return false;
        }
        
        public void onHandshake(String message)
        {
            logger.info("Got Stp handshake!");
            handShaken = true;
            connection.initializeServices(message);

            synchronized (this) {
                notifyAll();
            }
        }
        
	public void onDisconnect()
        {
            logger.fine("Disconnected, closing StpConnection.");
            if (connection != null)
            {
                if (connection.getWaitState().isWaiting())
                {
                    logger.severe("Unexpected.");
                    connection.getWaitState().onException(new WebDriverException("Connection closed unexpectedly."));
                }
                logger.fine("Cleaning up...");
                connection = null;
            }
	}

	public void onResponseReceived(boolean success, int tag) {
            logger.fine("Got response.");
            if (success) {
                connection.getWaitState().onResponse(tag);
            } else {
                connection.getWaitState().onError(tag);
            }
	}

	public void onException(Exception ex) {
            logger.fine("Got exception");
            if (connection != null)
            {
                connection.getWaitState().onException(ex);
            }
	}

	public String getMinVersionFor(String service) {
            return versions.get(service);
	}

        public void onBinaryStopped() {
            if (connection != null)
            {
                connection.getWaitState().onBinaryExit();
            }
        }

}
