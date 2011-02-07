package com.opera.core.systems;

import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;

/**
 * Provided support for an alternate protocol. To be removed.
 *
 * @deprecated
 * @author Deniz Turkoglu
 *
 */
@Deprecated
public class UmsEventHandler extends EventHandler {

	public UmsEventHandler(ScopeServices services) {
		super(services);
	}
	
	@Override
	public void onRuntimeStarted(RuntimeInfo info) {
		/*
		if(info.getHtmlFramePath().equals("_top") && info.getWindowID() == services.getWindowManager().getActiveWindowId()) {
			//check if we already have such a runtime and clean them up if needed
			services.getDebugger().cleanUpRuntimes();
			services.getDebugger().setRuntime(info);
		}
		*/
		services.getDebugger().addRuntime(info);
	}
	
	@Override
	public void onUpdatedWindow(WindowInfo info) {
		//logger.log(Level.INFO, window.toString());
		//if(info.getOpenerID() == 0)
			services.getWindowManager().addWindow(info);
	}
	
	@Override
	public void onMessage(ConsoleMessage message) {
		throw new UnsupportedOperationException();
	}

}
