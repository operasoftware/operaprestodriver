package com.opera.core.systems;

import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.EcmascriptProtos.ReadyStateChange;
import com.opera.core.systems.scope.protos.EsdbgProtos.RuntimeInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;

public class EventHandler extends AbstractEventHandler {
	
	protected ScopeServices services;
	
	public EventHandler(ScopeServices services) {
		super(services);
		this.services = services;
	}

	/**
	 * Changes the window-manager's active window on active window event
	 */
	public void onActiveWindow(Integer id) {
		services.getWindowManager().setActiveWindowId(id);
	}

	/**
	 * Remove any runtime that has been stopped. This call
	 * can be quite late and hence is resolved by cleanup method
	 * in onRuntimeStarted
	 */
	public void onRuntimeStopped(Integer id) {
		services.getDebugger().removeRuntime(id);
		//FIXME this event is quite buggy, ignore
		/*
		if(stopped.getRuntimeId() == services.getDebugger().getRuntimeId())
			services.getDebugger().setRuntimeId(0);
		*/
	}


	public void onRequest(int windowId) {
		services.onRequest(windowId);
	}
	
	/**
	 * Handles windows that have been closed. Removes it from the
	 * list and removes the runtimes that are associated with it
	 */
	public void onWindowClosed(Integer id) {
		services.onWindowClosed(id);
		services.getWindowManager().removeWindow(id);
		services.getDebugger().cleanUpRuntimes(id);
	}

	/**
	 * Fired when a window is loaded
	 * 
	 * @param windowId ID of the window that fired the event
	 */
	public void onWindowLoaded(int windowId) {
		services.getDebugger().cleanUpRuntimes(windowId);
		services.onWindowLoaded(windowId);
	}

	@Override
	public void onRuntimeStarted(RuntimeInfo started) {
		throw new UnsupportedOperationException("Not supported in STP/0");
	}

	@Override
	public void onUpdatedWindow(WindowInfo window) {
		throw new UnsupportedOperationException("Not supported in STP/0");
	}

	@Override
	public void onMessage(ConsoleMessage message) {
		throw new UnsupportedOperationException("Not supported in STP/0");
	}

	@Override
	public void onHttpResponse(int responseCode) {
		services.getWindowManager().getLastHttpResponseCode().compareAndSet(0, responseCode);
	}

	@Override
	public void onReadyStateChange(ReadyStateChange change) {
		throw new UnsupportedOperationException("Not supported in STP/0");
	}

	@Override
	public void onDesktopWindowUpdated(DesktopWindowInfo info) {
		services.onDesktopWindowUpdated(info);
	}

	@Override
	public void onDesktopWindowActivated(DesktopWindowInfo info) {
		services.onDesktopWindowActivated(info);
	}

	@Override
	public void onDesktopWindowClosed(DesktopWindowInfo info) {
		services.onDesktopWindowClosed(info);
	}

}
