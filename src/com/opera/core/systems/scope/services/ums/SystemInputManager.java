package com.opera.core.systems.scope.services.ums;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.services.SystemInput;

public class SystemInputManager extends AbstractService implements SystemInput {

	 SystemInputManager(ScopeServices services, String version) {
			super(services, version);
			
			String serviceName = "system-input";
			
			if(!isVersionInRange(version, "5.0", serviceName))
				throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
			
			services.setSystemInputManager(this);
		}
	
	public void click() {
		// TODO Auto-generated method stub

	}

	public void keyPress() {
		// TODO Auto-generated method stub

	}

}
