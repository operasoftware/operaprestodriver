package com.opera.core.systems.scope.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;

public interface IWindowManager {

	/**
	 * Sets the active window if the window is of type 'normal'
	 * discards any other window such as 'dialog' or 'unknown'
	 * @param activeWindowId
	 */
	void setActiveWindowId(Integer windowId);

	int getActiveWindowId();

	void addWindow(WindowInfo window);

	void removeWindow(Integer windowId);
	
	void filterActiveWindow();

	void closeWindow(Integer window);

	void closeAllWindows();

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

	void setActiveWindow(String title);
	
	List<Integer> getWindowHandles();
	
	int getOpenWindowCount();
	
	AtomicInteger getLastHttpResponseCode();

	void resetWindowsList();

	void clearFilter();

	void filterWindow(Integer windowId);
}
