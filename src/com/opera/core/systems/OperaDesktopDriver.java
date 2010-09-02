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
	
	public void waitStart() {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitStart();
	}

	public void waitForWindowUpdated() {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitForDesktopWindowUpdated(OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}


	public int waitForWindowActivated() {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitForDesktopWindowActivated(OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
		
		return 1;
	}

	public void waitForWindowClose() {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitForDesktopWindowClosed(OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}
}


