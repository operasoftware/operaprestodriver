package com.opera.core.systems.scope.services;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public interface IWindowManager {

	/**
	 * Sets the active window if the window is of type 'normal'
	 * discards any other window such as 'dialog' or 'unknown'
	 * @param activeWindowId
	 */
	void setActiveWindowId(int windowId);

	int getActiveWindowId();

	void addWindow(Object window);

	void removeWindow(int windowId);
	
	void filterActiveWindow();

	/**
	 * Get the list of all windows, then find a driver window (which needs to be 'normal' window)
	 * after that set the active window to that window
	 */
	void init();

	/**
	 * Find a window suitable for driving, find the active window,
	 * if it is not a normal window, find the first normal window
	 * via JXPath
	 * @throws WebDriverException if no window is available for driving
	 */
	void findDriverWindow();

	/**
	 * Finds and updates the active window
	 * In STP/0 this command triggers and event and
	 * is handled by event handler. This has been fixed
	 * in STP/1
	 */
	void updateActiveWindow();

	void setActiveWindow(String windowName);
	
        @Deprecated
	CountDownLatch getWindowClosedLatch();

        @Deprecated
	void setWindowClosedLatch(CountDownLatch windowClosedLatch);

        @Deprecated
	void waitForWindowClosed();

	Set<String> getWindowHandles();
	
	int getOpenWindowCount();
	
	AtomicInteger getLastHttpResponseCode();

	void resetWindowsList();
	
	//FIXME ugly hack for ping
	
	boolean canPingHost();
}