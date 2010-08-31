package com.opera.core.systems;

//import java.util.HashMap;
import java.util.Map;

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
	
}


