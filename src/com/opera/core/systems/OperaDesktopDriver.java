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
	
	public int getWindowID(String name) {
		return desktopWindowManager.getWindowID(name);
	}
	
	public QuickWindow getWindow(String name) {
		return desktopWindowManager.getWindow(name);
	}
	
	public QuickWidget findWidgetByID(int windowId, String id) {
		return desktopWindowManager.getQuickWidget(windowId, "id", id);
	}
	
	public QuickWidget findWidgetByName(int windowId, String name){
		return desktopWindowManager.getQuickWidget(windowId, "name", name);
	}
	
	public String getString(String enum_text){
		return desktopWindowManager.getString(enum_text);
	}
	
	public int getWindowCount() {
		//TODO FIXME
		//return desktopWindowManager.getOpenWindowCount();
		return 0;
	}
	
	public void operaDesktopAction(String using, String... params) 
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

	public int waitForWindowShown(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowShown(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	public int waitForWindowUpdated(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowUpdated(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	public int waitForWindowActivated(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowActivated(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
		
	}

	public int waitForWindowClose(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowClosed(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}
}


