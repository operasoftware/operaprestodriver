package com.opera.core.systems.scope.services.ums;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.services.model.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.model.IOperaExec;
import com.opera.core.systems.scope.services.model.IWindowManager;

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
	
	public UmsServices(ScopeServices services) {
		windowManager = new WindowManager(services, "window-manager");
		debugger = new EcmaScriptDebugger(services, "ecmascript-debugger");
		exec = new OperaExec(services, "exec");
	}
}
