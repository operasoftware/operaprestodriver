package com.opera.core.systems;

//import java.util.HashMap;
import java.util.Map;

import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.IDesktopWindowManager;

public class OperaDesktopDriver extends OperaDriver {
	
	private IDesktopWindowManager desktopWindowManager;
	
	/**
	 * For testing override this method.
	 */
	protected void init() {
		super.init();
		desktopWindowManager = services.getDesktopWindowManager();
	}

	protected Map<String, String> getServicesList()
	{
		Map<String, String> versions = super.getServicesList();
		versions.put("desktop-window-manager", "1.0");
		return versions;
	}
	
	public int getActiveWindowID()
	{
		return desktopWindowManager.getActiveWindowId();
	}

	// OBS: Temporary for testing
	public void getWidgetList(int id)
	{
		desktopWindowManager.getWidgetList(id);
	}
	
	public void getWindowList() {
		desktopWindowManager.getWindowList();
	}
	
	public int getWindowID(String title) {
		return desktopWindowManager.getWindowID(title);
	}
	
	public QuickWindow getWindow(String title) {
		return desktopWindowManager.getWindow(title);
	}
	
	public int getQuickWidgetID(int windowId, String property, String value){
		return desktopWindowManager.getQuickWidgetID(windowId, property, value);
	}
	
	public QuickWidget findWidgetByName(int windowId, String name){
		return desktopWindowManager.getQuickWidget(windowId, "name", name);
	}
	
	public QuickWidget findWidgetByText(int windowId, String text){
		return desktopWindowManager.getQuickWidget(windowId, "text", text);
	}
	
	public QuickWidget findWidgetByID(int windowId, String id) {
		return desktopWindowManager.getQuickWidget(windowId, "id", id);
	}
	
	public int getWindowCount() {
		//TODO FIXME
		//return desktopWindowManager.getOpenWindowCount();
		return 0;
	}
	
	public void operaAction(String using, String... params) 
	{
        // Desktop never wants the window
		exec.action(using, 0, params);
	}
	
	public void waitStart() {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitStart();
	}

	public void waitForWindowUpdated() {
		waitForWindowUpdated("");
	}

	public void waitForWindowActivated() {
		waitForWindowActivated("");
	}

	public void waitForWindowClose() {
		waitForWindowClose("");
	}

	public void waitForWindowUpdated(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitForDesktopWindowUpdated(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}


	public int waitForWindowActivated(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitForDesktopWindowActivated(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
		
		return 1;
	}

	public void waitForWindowClose(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitForDesktopWindowClosed(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}
}


