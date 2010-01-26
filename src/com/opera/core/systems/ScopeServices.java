/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceResult;
import com.opera.core.systems.scope.protos.ScopeProtos.ServiceSelection;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.services.model.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.model.IOperaExec;
import com.opera.core.systems.scope.services.model.IWindowManager;
import com.opera.core.systems.scope.services.ums.UmsServices;
import com.opera.core.systems.scope.services.xml.XmlServices;


public class ScopeServices implements IConnectionHandler {
	
	private IEcmaScriptDebugger debugger;
	private IOperaExec exec;
	private IWindowManager windowManager;
	private ScopeActions actionHandler;
	private Parser parser;
	
	private ScopeConnection connection;	
	
	public ScopeConnection getConnection() {
		return connection;
	}
	
	public ScopeActions getActionHandler() {
		return actionHandler;
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
		 public int getCommandID() { return code; }

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
	public ScopeServices() {
		setupConnection();
	}
	
	public void init() {
		connection.waitForHandShake();
		boolean isStp1 = connection.isStp1();
		boolean enableDebugger = (OperaIntervals.ENABLE_DEBUGGER.getValue() == 1);
		if(isStp1) {
			connection.switchToStp1();
			createUmsServices(enableDebugger);
			connection.setEventHandler(new UmsEventHandler(this));
			connect();
			if(enableDebugger)
				enableServices("ecmascript-debugger");
			enableServices("exec", "window-manager", "http-logger");
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
			services.add("http-logger");
			services.add("window-manager");
			connection.enableServices(services);
		}
		initializeServices();
	}
	
	private void createUmsServices(boolean enableDebugger) {
		new UmsServices(this);
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
		};
	}

	private void connect() {
		ClientInfo.Builder info = ClientInfo.newBuilder().setFormat("protobuf");
		connection.executeCommand(ScopeCommand.CONNECT, "scope", info);
	}
	
	public void enableServices(String... requiredServices){
		for (String requiredService : requiredServices) {
			try {
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
	
	private void setupConnection() {
		connection = new ScopeConnection(this);
		connection.init();
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
	}

	public void onDisconnect() {
		onSocketException();
	}

	public void onResponseReceived() {
		connection.getResponseReceivedLatch().countDown();
		
	}

	public void onSocketException() {
		windowManager.getLoadCompleteLatch().countDown();
		onResponseReceived();
	}
	
	
}
