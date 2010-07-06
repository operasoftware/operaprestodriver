/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.handlers;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;


/**
 * Event handler for scope events and network exceptions
 * @author Deniz Turkoglu
 *
 */
public abstract class AbstractEventHandler {
	

    /*
    protected IWindowManager windowManager;
	protected IOperaExec exec;
	protected IEcmaScriptDebugger debugger;
	protected ScopeConnection connection;
	*/
	public AbstractEventHandler(ScopeServices services) {
     /*
		this.windowManager = services.getWindowManager();
		this.debugger = services.getDebugger();
		this.exec = services.getExec();
		this.connection = services.getConnection();
     */
	}
	
	/**
	 * Fired when a new runtime started is received
	 * Runtime-started is needed for tracking ecmascript injections
	 * @param started
	 */
	
	public abstract void onRuntimeStarted(RuntimeInfo started);

	/**
	 * Fired on new console messages
	 * @param message
	 */
	
	public abstract void onMessage(ConsoleMessage message);
	
	/**
	 * Fired when a runtime is stopped and no longer injectable
	 * @param stopped
	 */
	public abstract void onRuntimeStopped(Integer id);

	/**
	 * Fired when a new window is created or window
	 * has incoming changes (such as title change)
	 * @param window
	 */
	
	public abstract void onUpdatedWindow(WindowInfo window);

	/**
	 * Fired when a window becomes active (steals focus)
	 * @param window
	 */
	public abstract void onActiveWindow(Integer id);

	/**
	 * Fired when a window instance is closed
	 * @param closed
	 */
	public abstract void onWindowClosed(Integer id);

	/**
	 * Fired when a window load is complete
	 * @param windowId Id of the window that is loaded 
	 */
	public abstract void onWindowLoaded(int windowId);
	
	public abstract void onHttpResponse(int responseCode);
	
	public abstract void onReadyStateChange(ReadyStateChange change);
	
	public int parseHeader(String header) {
		int responseCode = 0;
		try {
			responseCode = Integer.parseInt(header.split(" ")[1]);
		} catch (NumberFormatException e) {
			//ignore for now
		}
		return responseCode;
	}
}
