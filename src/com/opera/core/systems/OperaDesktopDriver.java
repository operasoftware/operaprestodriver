package com.opera.core.systems;

//import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

public class OperaDesktopDriver extends OperaDriver {
	
	private IDesktopWindowManager desktopWindowManager;
	private SystemInputManager systemInputManager;
	
	/**
	 * For testing override this method.
	 */
	protected void init() {
		super.init();
		desktopWindowManager = services.getDesktopWindowManager();
		systemInputManager = services.getSystemInputManager();
		System.out.println("SystemInput Manager = " + systemInputManager);
	}

	// TODO: FIXME
	protected Map<String, String> getServicesList() {
		Map<String, String> versions = super.getServicesList();
		versions.put("desktop-window-manager", "1.0");
		versions.put("system-input", "1.0");
		return versions;
	}
	
	/**
	 * @return active window id
	 */
	public int getActiveWindowID() {
		return desktopWindowManager.getActiveWindowId();
	}

	/**
	 * @param win_name
	 * @return list of widgets in the window with name win_name
	 */
	public List<QuickWidget> getQuickWidgetList(String winName) {
		int id = getWindowID(winName);
		return desktopWindowManager.getQuickWidgetList(id);
	}
	
	public List<QuickWindow> getWindowList() {
		return desktopWindowManager.getQuickWindowList();
	}

	/**
	 * @param name
	 * @return window id of window with window_name 
	 */
	public int getWindowID(String windowName) {
		return desktopWindowManager.getWindowID(windowName);
	}
	
	/**
	 * @param name
	 * @return QuickWindow with the given name
	 */
	public QuickWindow getWindow(String windowName) {
		return desktopWindowManager.getWindow(windowName);
	}
	
	/**
	 * @param windowId
	 * @param widgetName
	 * @return QuickWidget with the given name in the window with id windowId
	 */
	public QuickWidget findWidgetByName(int windowId, String widgetName){
		return desktopWindowManager.getQuickWidget(windowId, "name", widgetName);
	}

	/**
	 * @param windowId
	 * @return String: name of the window
	 */
	public String getWindowName(int windowId) {
		return desktopWindowManager.getWindowName(windowId);
	}
	
	/**
	 * 
	 * @param enum_text
	 * @return the string specified by the id @param enum_text
	 */
	public String getString(String enum_text){
		return desktopWindowManager.getString(enum_text);
	}
	
	public int getWindowCount() {
		//TODO FIXME
		//return desktopWindowManager.getOpenWindowCount();
		return 0;
	}
	
	/**
	 * Execute opera action
	 * @param using - action_name
	 * @param params - parameters to the action
	 */
	public void operaDesktopAction(String using, String... params) 
	{
        // Desktop never wants the window
		exec.action(using, 0, params);
	}
	
	/*
	 * Starts a process of waiting for a window to show
	 * After this call, messages to the driver about window events are not thrown away,
	 * so that the notification about window shown is not lost because of other events or messages 
	 */
	public void waitStart() {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitStart();
	}

	/**
	 * Wait for any window update event
	 */
	public void waitForWindowUpdated() {
		waitForWindowUpdated("");
	}

	/**
	 * Wait for any window activated event
	 */
	public void waitForWindowActivated() {
		waitForWindowActivated("");
	}

	/**
	 * Wait for any window close event
	 */
	public void waitForWindowClose() {
		waitForWindowClose("");
	}

	/**
	 * Wait until the window given by the @param win_name is shown, and then returns the
	 * window id of this window
	 * 
	 * @param win_name - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowShown(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowShown(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	/**
	 * Wait until the window given by the @param win_name is updated, and then returns the
	 * window id of this window
	 * 
	 * @param win_name - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowUpdated(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowUpdated(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	/**
	 * Wait until the window given by the @param win_name is activated, and then returns the
	 * window id of this window
	 * 
	 * @param win_name - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowActivated(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowActivated(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
		
	}

	/**
	 * Wait until the window given by the @param win_name is closed, and then returns the
	 * window id of this window
	 * 
	 * @param win_name - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowClose(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowClosed(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}
	
}


