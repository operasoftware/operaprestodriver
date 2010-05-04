package com.opera.core.systems.scope.services.ums;

import java.util.List;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.protos.ScopeProtos.HostInfo;
import com.opera.core.systems.scope.protos.ScopeProtos.Service;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IWindowManager;
import com.sun.xml.ws.util.VersionUtil;

public class UmsServices {

	protected final IEcmaScriptDebugger debugger;
	protected final IWindowManager windowManager;
	protected final IOperaExec exec;

	public IEcmaScriptDebugger getDebugger() {
		return debugger;
	}

	public IWindowManager getWindowManager() {
		return windowManager;
	}

	public IOperaExec getExec() {
		return exec;
	}
	
	public UmsServices(ScopeServices services, HostInfo info) {
		List<Service> serviceList = info.getServiceListList();
		windowManager = new WindowManager(services, "window-manager", getVersionForService(serviceList, "window-manager"));
		String esdbgVersion = getVersionForService(serviceList, "ecmascript-debugger");
		
		if(VersionUtil.compare(esdbgVersion, "6.0") >= 0)
			debugger = new EcmaScriptDebugger6(services, "ecmascript-debugger", esdbgVersion);
		else
			debugger = new EcmaScriptDebugger(services, "ecmascript-debugger", esdbgVersion);
		
		exec = new OperaExec(services, "exec", getVersionForService(serviceList, "exec"));
	}

	private String getVersionForService(List<Service> serviceList, String name) {
		return findServiceNamed(serviceList, name).getVersion();
	}

	private Service findServiceNamed(List<Service> services, String name) {
		Service found = null;
		for(Service service : services) {
			if(service.getName().equals(name)) {
				found = service;
				break;
			}
		}
		return found;
	}
}
