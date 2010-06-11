/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems.scope.services.ums;

import java.util.Collection;
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

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.WindowManagerCommand;
import com.opera.core.systems.scope.exceptions.WindowNotFoundException;
import com.opera.core.systems.scope.internal.OperaFlags;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.WmProtos.WindowFilter;
import com.opera.core.systems.scope.protos.WmProtos.WindowID;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowList;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.util.VersionUtil;


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
	
        @Deprecated
	public CountDownLatch getWindowClosedLatch() {
		return windowClosedLatch;
	}

        @Deprecated
	public void setWindowClosedLatch(CountDownLatch windowClosedLatch) {
		this.windowClosedLatch = windowClosedLatch;
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
		} else activeWindowId.set(window.getWindowID());
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
				throw new WindowNotFoundException("No window is available for driving");
			}
			filterActiveWindow();
		}

	}

	
	private WindowID findActiveWindow() {
		Response response = executeCommand(WindowManagerCommand.GET_ACTIVE_WINDOW, null);
		WindowID.Builder builder = WindowID.newBuilder();
		buildPayload(response, builder);
		return builder.build();
	}
	
	public void updateActiveWindow(){
		setActiveWindowId(findActiveWindow().getWindowID());
	}
	
	/**
	 * Filter only the active window so we don't get messages from
	 * other windows (like thread messages)
	 */
	public void filterActiveWindow() {
		WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
		filterBuilder.setClearFilter(true);
		filterBuilder.addIncludeIDList(getActiveWindowId());
		
		executeCommand(WindowManagerCommand.MODIFY_FILTER, filterBuilder);
	}


	/**
	 * Set the filter to include all windows so we can get a list
	 * and maintain a list of windows
	 */
	private void initializeWindows() {
		
		WindowFilter.Builder filterBuilder = WindowFilter.newBuilder();
		filterBuilder.setClearFilter(true);
		filterBuilder.addIncludePatternList("*");
		executeCommand(WindowManagerCommand.MODIFY_FILTER, filterBuilder);

		Response response = executeCommand(WindowManagerCommand.LIST_WINDOWS, null);
		WindowList.Builder builder = WindowList.newBuilder();
		buildPayload(response, builder);
		WindowList list = builder.build();

		List<WindowInfo> windowsList = list.getWindowListList();

		windows.clear();
		for (WindowInfo window : windowsList) {
			//FIXME workaround for CORE-25866
			//if(window.getTitle().length() > 0)
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
	
        @Deprecated
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
		Collection<WindowInfo> windowCollection = getWindows().values();
		Set<String> handles = new HashSet<String>();
		
		for (WindowInfo window : windowCollection) {
			handles.add(window.getTitle().toString());
		}
		return handles;
	}

	public void resetWindowsList() {
		initializeWindows();
	}

	public boolean canPingHost() {
		try {
			WindowID id = findActiveWindow();
			return (id != null);
		} catch (WebDriverException e) {
			return false;
		}
	}

}
