/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */

package com.opera.core.systems.scope.services.ums;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.WindowManagerCommand;
import com.opera.core.systems.scope.exceptions.WindowNotFoundException;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.WmProtos.WindowFilter;
import com.opera.core.systems.scope.protos.WmProtos.WindowID;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowList;
import com.opera.core.systems.scope.services.IWindowManager;


/**
 * Handles the windowmanager service
 * @author Deniz Turkoglu
 *
 */
public class WindowManager extends AbstractService implements IWindowManager {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private Map<Integer, WindowInfo> windows = new ConcurrentHashMap<Integer, WindowInfo>();
	private Stack<Integer> windowStack = new Stack<Integer>();
	private Object lock = new Object(); // lock for the windowStack. Note windowStack and windows need to be in sync.

	private final CompiledExpression windowFinder;
	
	private final AtomicInteger lastHttpResponseCode = new AtomicInteger();
	
	public AtomicInteger getLastHttpResponseCode() {
		return lastHttpResponseCode;
	}

    public WindowManager(ScopeServices services, String version) {
		super(services, version);
		
		String serviceName = "window-manager";
		
		if(!isVersionInRange(version, "3.0", serviceName))
			throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
		
		services.setWindowManager(this);
		windowFinder = JXPathContext.compile("/.[windowType='normal']/windowID");
	}
    
    public int getOpenWindowCount() {
		return windows.keySet().size();
	}
    
	public void setActiveWindowId(Integer windowId) {
            synchronized (lock)
            {
            	//active-window can fire before updated-window
            	/*
                WindowInfo window = windows.get(windowId);
                if (window == null)
                    throw new WindowNotFoundException("Window not found: " + windowId);
                 */
                windowStack.remove(windowId);
                windowStack.push(windowId);
            }
	}

	public int getActiveWindowId() {
            synchronized (lock)
            {
                if (windowStack.isEmpty())
                    throw new WindowNotFoundException("No windows open!");
                return windowStack.peek();
            }
	}

	public Map<Integer, WindowInfo> getWindows() {
		return windows;
	}

	public void addWindow(Object object) {
            synchronized (lock)
            {
                WindowInfo info = (WindowInfo) object;

                if (!windows.containsKey(info.getWindowID()))
                {
                    windows.put(info.getWindowID(), info);
                    windowStack.push(info.getWindowID());
                }
                else
                {
                    updateWindow(info);
                }
            }
	}

        private void updateWindow(WindowInfo info)
        {
            windows.put(info.getWindowID(), info);
        }
	

	public void removeWindow(Integer windowId) {
            synchronized (lock) {
                if  (windowStack.contains(windowId))
                    windowStack.remove(windowId);

                if (windows.containsKey(windowId))
                    windows.remove(windowId);
            }
	}
	
	public void init() {
		initializeWindows();
		windowStack.push(findActiveWindow().getWindowID());
		findDriverWindow();
	}

	public void findDriverWindow() {
		
		//updateActiveWindow();
		
		JXPathContext pathContext = JXPathContext.newContext(windows.values());
		WindowInfo window = null;

		synchronized (lock) {
			window = windows.get(windowStack.peek());
		}
		
		if (window == null || !window.getWindowType().equals("normal")) {
			// we dont deal with anything else, at least for now
			// select a window that is normal and return first
			
			//Fix for Windows OS, we dont encounter this problem on linux at all
			if(windows.isEmpty())
				throw new WebDriverException("List of windows is empty");
			
			Integer windowId = (Integer) windowFinder.getValue(pathContext);

			if (windowId != null) {
                            setActiveWindowId(windowId);
			} else {
				throw new WindowNotFoundException("No window is available for driving");
			}
			filterActiveWindow();
		}

	}

	// NOTE: This is proven to be not working on Opera side...
	private WindowID findActiveWindow() {
            Response response = executeCommand(WindowManagerCommand.GET_ACTIVE_WINDOW, null);
            WindowID.Builder builder = WindowID.newBuilder();
            buildPayload(response, builder);
            return builder.build();
	}
	
	public void updateActiveWindow() {
		//int activeWindow = findActiveWindow().getWindowID();
		setActiveWindowId(windowStack.peek());
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
		//initialize windowStack
		
	}

	/* (non-Javadoc)
	 * @see com.opera.core.systems.scope.services.xml.IWindowManager#setActiveWindow(java.lang.String)
	 */
	public void setActiveWindow(String windowName) {
		Integer windowId = (Integer) xpathPointer(windows.values(), "/.[title='" + windowName + "']/windowID").getValue(); 
		if(windowId == null)
			throw new NoSuchWindowException("No such window : " + windowName);
                setActiveWindowId(windowId);
	}

        public void closeActiveWindow()
        {
            synchronized (lock) {
                if (!windowStack.isEmpty()) {
                    Integer windowId = windowStack.pop();
                    windowStack.remove(windowId);
                    windows.remove(windowId);
                }
            }

            logger.fine("closeActiveWindow");
            services.getExec().action("Close page");
        }

        public void closeWindow(Integer windowId) {
            // TODO: WindowManager 2.1 has a close window command
            services.getExec().action("Close page", Integer.toString(windowId));
            removeWindow(windowId);
        }

        public void closeAllWindows() {
            logger.fine("closeAllWindows");
            LinkedList<Integer> list = new LinkedList<Integer>();
            boolean canCloseAll = services.getExec().getActionList().contains("Close all pages");

            synchronized (lock) {
                if (!canCloseAll) {
                    while (!windowStack.isEmpty()) {
                        Integer windowId = windowStack.peek();
                        list.add(windowId);
                        windowStack.remove(windowId);
                        windows.remove(windowId);
                    }
                }
            }

            if (canCloseAll) {
                services.getExec().action("Close all pages");
            } else {
                while (!list.isEmpty())
                {
                    Integer windowId = list.removeFirst();
                    logger.fine("closeWindow " + windowId);
                    // TODO: WindowManager 2.1 has a close window command
                    services.getExec().action("Close page");
                }
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

}