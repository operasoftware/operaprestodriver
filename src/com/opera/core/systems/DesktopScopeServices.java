package com.opera.core.systems;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.scope.stp.StpThread;

public class DesktopScopeServices extends ScopeServices {
	private IDesktopWindowManager desktopWindowManager;

	/**
	 * Creates the scope server on specified address and port
	 * Enables the required services for webdriver
	 * @param ipAddress
	 * @param portNumber
	 * @param intervals 
	 */
	public DesktopScopeServices(Map<String, String> versions) throws IOException {
		super(versions);
	}

	public void setDesktopWindowManager(IDesktopWindowManager desktopWindowManager) {
		this.desktopWindowManager = desktopWindowManager;
	}


}
