/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems.scope.services.xml;

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
import com.opera.core.systems.scope.beans.Filter;
import com.opera.core.systems.scope.beans.ListWindowsReply;
import com.opera.core.systems.scope.beans.ObjectFactory;
import com.opera.core.systems.scope.beans.Pattern;
import com.opera.core.systems.scope.beans.Window;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.Parser;
import com.opera.core.systems.scope.services.IWindowManager;

/**
 * Handles the windowmanager service
 * @author Deniz Turkoglu
 *
 */
public class WindowManager extends AbstractService implements IWindowManager {

	//TODO add support for dialogs
	//TODO allow switching windows
	private final AtomicInteger activeWindowId = new AtomicInteger();
	private int tag;
	private Parser parser;
	
	private final CompiledExpression windowFinder;
	
	private Map<Integer, Window> windows = new ConcurrentHashMap<Integer, Window>();
	private final AtomicInteger lastHttpResponseCode = new AtomicInteger();
	
	public AtomicInteger getLastHttpResponseCode() {
		return lastHttpResponseCode;
	}
	
	protected CountDownLatch loadCompleteLatch = new CountDownLatch(1);
	protected CountDownLatch windowClosedLatch = new CountDownLatch(1);
	
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
	
	
    public WindowManager(ScopeServices services, String serviceName) {
		super(services, serviceName, "1.0");
		services.setWindowManager(this);
		this.parser = services.getParser();
		windowFinder = JXPathContext.compile("//.[windowType='normal']/windowId");
	}
    
    public int getOpenWindowCount() {
		return windows.keySet().size();
	}
    
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#setActiveWindowId(int)
	 */
	public void setActiveWindowId(int windowId) {
		Window window = windows.get(windowId);
		if(window != null && window.getWindowType().equals("normal"))
			activeWindowId.set(windowId);
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#getActiveWindowId()
	 */
	public int getActiveWindowId() {
		return activeWindowId.get();
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#getActiveWindow()
	 */
	public Window getActiveWindow() {
		return windows.get(activeWindowId.get());
	}

	private Map<Integer, Window> getWindows() {
		return windows;
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#addWindow(com.opera.core.systems.scope.beans.Window)
	 */
	public void addWindow(Object object) {
		Window window = (Window) object;
		windows.put(window.getWindowId(), window);
	}
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#removeWindow(int)
	 */
	public void removeWindow(int windowId) {
		windows.remove(windowId);
	}
	
	
	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#init()
	 */
	public void init() {
		initializeWindows();
		findDriverWindow();
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#findDriverWindow()
	 */
	public void findDriverWindow() {
		
		updateActiveWindow();
		
		JXPathContext pathContext = JXPathContext.newContext(windows.values());
		
		Window window = windows.get(activeWindowId.get());
		
		if (window == null || !window.getWindowType().equals("normal")) {
			// we dont deal with anything else, at least for now
			// select a window that is normal and return first

			Integer windowId = (Integer) windowFinder.getValue(pathContext);

			if (windowId != null) {
				activeWindowId.set(windowId);
			} else {
				throw new WebDriverException("No window is available for driving");
			}
			filterActiveWindow();
		}

	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#updateActiveWindow()
	 */
	public void updateActiveWindow(){
		this.post("<get-active-window />");		
	}
	
	/**
	 * Filter only the active window so we don't get messages from
	 * other windows (like thread messages)
	 */
	public void filterActiveWindow(){
		Filter filter = new Filter();
		filter.setTag(++tag);
		
		ObjectFactory factory = new ObjectFactory();
		Pattern pattern = new Pattern();
		pattern.setWindowId(activeWindowId.get());
		filter.getIncludeOrExclude().add(factory.createInclude(pattern));
		this.postRequest(parser.createRequest(filter, Filter.class));
		String filterReply = waitForResponse("<filter-reply>");
		if(!filterReply.contains("ok"))
			throw new WebDriverException("Could not set window filter");
	}

	/**
	 * Set the filter to include all windows so we can get a list
	 * and maintain a list of windows
	 */
	private void initializeWindows() {
		
		Filter filter = new Filter();
		filter.setTag(tag++);
		ObjectFactory factory = new ObjectFactory();
		Pattern pattern = new Pattern();
		pattern.setString("*");
		filter.getIncludeOrExclude().add(factory.createInclude(pattern));
		this.postRequest(parser.createRequest(filter, Filter.class));
		String filterReply = waitForResponse("<filter-reply>");
		if(!filterReply.contains("ok"))
			throw new WebDriverException("Could not set window filter");
		
		this.post("<list-windows />");
		String response = waitForResponse("<list-windows-reply>");

		ListWindowsReply windowsReply = parser.parseResponse(ListWindowsReply.class, response);

		List<Window> windowsList = windowsReply.getWindow();
		
		windows.clear();
		
		for (Window window : windowsList) {
			//FIXME workaround for CORE-25866
			if(window.getTitle().getContent().size() > 0)
				windows.put(window.getWindowId(), window);
		}
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#setActiveWindow(java.lang.String)
	 */
	public void setActiveWindow(String windowName) {
		Integer windowId = (Integer) xpathPointer(windows.values(), "//.[title='" + windowName + "']/windowId").getValue(); 
		if(windowId == null)
			throw new NoSuchWindowException("No such window : " + windowName);
		activeWindowId.set(windowId);
	}


	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#waitForWindowLoaded()
	 */
	public void waitForWindowLoaded(long timeout) {
		//waitForResponse("<window-loaded>", 10000);
		try {
			loadCompleteLatch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new WebDriverException("Page load timeout");
		}
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
		Collection<Window> windows = getWindows().values();
		Set<String> handles = new HashSet<String>();
		
		for (Window window : windows) {
			handles.add(window.getTitle().getContent().toString());
		}
		return handles;
	}

	public void resetWindowsList() {
		initializeWindows();
	}
}
