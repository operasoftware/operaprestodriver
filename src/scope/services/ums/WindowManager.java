/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems.scope.services.ums;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.ICommand;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.internal.OperaFlags;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.WmProtos.WindowFilter;
import com.opera.core.systems.scope.protos.WmProtos.WindowID;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowList;
import com.opera.core.systems.scope.services.IWindowManager;
import com.sun.xml.internal.ws.util.VersionUtil;

/**
 * Handles the windowmanager service
 * @author Deniz Turkoglu
 *
 */
public class WindowManager extends AbstractService implements IWindowManager {

	//TODO add support for dialogs
	//TODO allow switching windows
	private final AtomicInteger activeWindowId = new AtomicInteger();

	private final CompiledExpression windowFinder;
	
	private Map<Integer, WindowInfo> windows = new ConcurrentHashMap<Integer, WindowInfo>();
	
	protected CountDownLatch loadCompleteLatch = new CountDownLatch(1);
	protected CountDownLatch windowClosedLatch = new CountDownLatch(1);
	
	private final AtomicInteger lastHttpResponseCode = new AtomicInteger();
	
	public AtomicInteger getLastHttpResponseCode() {
		return lastHttpResponseCode;
	}
	
	public CountDownLatch getWindowClosedLatch() {
		return windowClosedLatch;
	}

	public void setWindowClosedLatch(CountDownLatch windowClosedLatch) {
		this.windowClosedLatch = windowClosedLatch;
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#getLoadCompleteLatch()
	 */
	public CountDownLatch getLoadCompleteLatch() {
		return loadCompleteLatch;
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#setLoadCompleteLatch(java.util.concurrent.CountDownLatch)
	 */
	public void setLoadCompleteLatch(CountDownLatch loadCompleteLatch) {
		this.loadCompleteLatch = loadCompleteLatch;
	}
	
	/*
	command GetActiveWindow(Default) returns (WindowID) = 1;
    command ListWindows(Default) returns (WindowList) = 2;
    command ModifyFilter(WindowFilter) returns (Default) = 3;
    event OnWindowUpdated returns (WindowInfo) = 4;
    event OnWindowClosed returns (WindowID) = 5;
    event OnWindowActivated returns (WindowID) = 6;
    event OnWindowLoaded returns (WindowID) = 7;
	 */
	
	public enum WmCommand implements ICommand {
		GET_ACTIVE_WINDOW(1), 
		LIST_WINDOWS(2), 
		MODIFY_FILTER(3),
		WINDOW_UPDATED(4),
		WINDOW_CLOSED(5),
		WINDOW_ACTIVATED(6),
		WINDOW_LOADED(7),
		DEFAULT(-1);

		private int code;
		private static final Map<Integer, WmCommand> lookup = new HashMap<Integer, WmCommand>();

		static {
			for (WmCommand command : EnumSet.allOf(WmCommand.class))
				lookup.put(command.getCommandID(), command);
		}

		private WmCommand(int code) {
			this.code = code;
		}

		public int getCommandID() {
			return code;
		}

		public static WmCommand get(int code) {
			WmCommand command = lookup.get(code);
			if(command == null)
				return DEFAULT;
			return command;
		}

	}
	
    public WindowManager(ScopeServices services, String serviceName, String version) {
		super(services, serviceName, version);
		//check if our fixes are in
		if(VersionUtil.compare(version, services.getMinVersionFor("window-manager")) == -1)
			throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
		services.setWindowManager(this);
		windowFinder = JXPathContext.compile("/.[windowType='normal']/windowID");
	}
    
    public int getOpenWindowCount() {
		return windows.keySet().size();
	}
    
	public void setActiveWindowId(int windowId) {
		//FIXME we wont be switching windows automatically, user must
		//explicitly call switchTo()
		WindowInfo window = windows.get(windowId);
		if(!OperaFlags.ENABLE_DIALOGS) {
			if(window != null && window.getWindowType().equals("normal"))
				activeWindowId.set(windowId);
		} else activeWindowId.set(windowId);
	}

	public int getActiveWindowId() {
		return activeWindowId.get();
	}
	
	public Map<Integer, WindowInfo> getWindows() {
		return windows;
	}

	public void addWindow(Object object) {
		WindowInfo info = (WindowInfo) object;
		windows.put(info.getWindowID(), info);
	}
	

	public void removeWindow(int windowId) {
		windows.remove(windowId);
	}
	
	public void init() {
		initializeWindows();
		findDriverWindow();
	}

	public void findDriverWindow() {
		
		updateActiveWindow();
		
		JXPathContext pathContext = JXPathContext.newContext(windows.values());
		
		WindowInfo window = windows.get(activeWindowId.get());
		
		if (window == null || !window.getWindowType().equals("normal")) {
			// we dont deal with anything else, at least for now
			// select a window that is normal and return first
			
			//Fix for Windows OS, we dont encounter this problem on linux at all
			if(windows.isEmpty())
				throw new WebDriverException("List of windows is empty");
			
			Integer windowId = (Integer) windowFinder.getValue(pathContext);

			if (windowId != null) {
				activeWindowId.set(windowId);
			} else {
				throw new WebDriverException("No window is available for driving");
			}
			filterActiveWindow();
		}

	}

	public void updateActiveWindow(){
		Response response = executeCommand(WmCommand.GET_ACTIVE_WINDOW, null);
		WindowID.Builder builder = WindowID.newBuilder();
		buildPayload(response, builder);
		WindowID id = builder.build();
		setActiveWindowId(id.getWindowID());
	}
	
	/**
	 * Filter only the active window so we don't get messages from
	 * other windows (like thread messages)
	 */
	public void filterActiveWindow() {
		WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
		filterBuilder.setClearFilter(true);
		filterBuilder.addIncludeIDList(getActiveWindowId());
		
		executeCommand(WmCommand.MODIFY_FILTER, filterBuilder);
	}


	/**
	 * Set the filter to include all windows so we can get a list
	 * and maintain a list of windows
	 */
	private void initializeWindows() {
		
		WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
		filterBuilder.setClearFilter(true);
		filterBuilder.addIncludePatternList("*");
		executeCommand(WmCommand.MODIFY_FILTER, filterBuilder);
		
		Response response = executeCommand(WmCommand.LIST_WINDOWS, null);
		WindowList.Builder builder = WindowList.newBuilder();
		buildPayload(response, builder);
		WindowList list = builder.build();
		
		List<WindowInfo> windowsList = list.getWindowListList();
		
		windows.clear();
		for (WindowInfo window : windowsList) {
			//FIXME workaround for CORE-25866
			if(window.getTitle().length() > 0)
				windows.put(window.getWindowID(), window);
		}
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#setActiveWindow(java.lang.String)
	 */
	public void setActiveWindow(String windowName) {
		Integer windowId = (Integer) xpathPointer(windows.values(), "/.[title='" + windowName + "']/windowID").getValue(); 
		if(windowId == null)
			throw new NoSuchWindowException("No such window : " + windowName);
		activeWindowId.set(windowId);
	}


	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#waitForWindowLoaded()
	 */
	public void waitForWindowLoaded(long timeout) {
		boolean pageLoaded;
		try {
			pageLoaded = loadCompleteLatch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new WebDriverException("Exception while waiting for page load : " + e.getMessage());
		}
		if(!pageLoaded)
			throw new WebDriverException("Page load timeout");
	}
	
	public void waitForWindowLoaded() {
		waitForWindowLoaded(OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}
	
	
	public void waitForWindowClosed() {
		try {
			windowClosedLatch.await(OperaIntervals.PAGE_LOAD_TIMEOUT.getValue(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			//ignore
		}
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#getWindowHandles()
	 */
	public Set<String> getWindowHandles() {
		Collection<WindowInfo> windows = getWindows().values();
		Set<String> handles = new HashSet<String>();
		
		for (WindowInfo window : windows) {
			handles.add(window.getTitle().toString());
		}
		return handles;
	}

	public void resetWindowsList() {
		initializeWindows();
	}

}
