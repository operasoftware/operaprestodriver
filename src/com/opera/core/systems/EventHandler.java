package com.opera.core.systems;

import com.opera.core.systems.scope.beans.Message;
import com.opera.core.systems.scope.beans.Runtime;
import com.opera.core.systems.scope.beans.RuntimeStarted;
import com.opera.core.systems.scope.beans.UpdatedWindow;
import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.protos.ConsoleLoggerProtos.ConsoleMessage;
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

	public void onMessage(Message message) {
		//FIXME implement
		System.out.println(message.getDescription().getContent().toString());
	}
	
	/**
	 * Handles the runtime started event, if the runtime has frame _top and
	 * it has the id of active window, that means we need to clean up 'old'
	 * runtimes from our list as they are no longer accessible
	 * After processing the runtime, we add it to our list for further access
	 */
	public void onRuntimeStarted(RuntimeStarted started) {
		Runtime runtime = started.getRuntime();
		if(runtime.getHtmlFramePath().equals("_top") && runtime.getWindowId() == services.getWindowManager().getActiveWindowId()) {
			//check if we already have such a runtime and clean them up if needed
			services.getDebugger().cleanUpRuntimes();
			services.getDebugger().setRuntime(runtime);
		}
		services.getDebugger().addRuntime(runtime);
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

	/**
	 * Handles the new windows, this event triggers even
	 * on title change so we need to use opener id to
	 * distinguish a new window even (opener-id=0 is a new window)
	 */
	public void onUpdatedWindow(UpdatedWindow window) {
		//logger.log(Level.INFO, window.toString());
		if(window.getWindow().getOpenerId() == 0)
			services.getWindowManager().addWindow(window.getWindow());
	}

	/**
	 * Handles windows that have been closed. Removes it from the
	 * list and removes the runtimes that are associated with it
	 */
	public void onWindowClosed(Integer id) {
		services.getWindowManager().removeWindow(id);
		services.getDebugger().cleanUpRuntimes(id);
		services.getWindowManager().getWindowClosedLatch().countDown();
	}

	public void onWindowLoaded() {
		services.getWindowManager().getLoadCompleteLatch().countDown();
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

}
