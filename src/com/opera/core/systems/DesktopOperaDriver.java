package com.opera.core.systems;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriverException;

public class DesktopOperaDriver extends OperaDriver {
	
	private void createScopeServices() {
		try {
			Map<String, String> versions = new HashMap<String, String>();
			versions.put("ecmascript-debugger", "5.0");
			versions.put("window-manager", "2.0");
			versions.put("exec", "2.0");
			//versions.put("desktop-window-manager", "0.1");
			services = new DesktopScopeServices(versions);
			services.startStpThread();
		} catch (IOException e) {
			throw new WebDriverException(e);
		}
	}

}
