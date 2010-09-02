package com.opera.core.systems.scope.services;

import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;

public interface IDesktopWindowManager {

	/**
	 * Sets the active window if the window is of type 'normal'
	 * discards any other window such as 'dialog' or 'unknown'
	 * @param activeWindowId
	 */
	void setActiveWindowId(Integer windowId);

	int getActiveWindowId();

	void addWindow(DesktopWindowInfo window);

	void removeWindow(Integer windowId);
	
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
	//void findDriverWindow();

	void setActiveWindow(String title);
	
	//List<Integer> getWidgetList();
	
	int getOpenWindowCount();
	
	void resetWindowsList();
	
	// OBS: Temporary, for testing
	public void getWidgetList(int id);
	public int getWindowID(String title);
	public List<DesktopWindowInfo> getWindowList();

}
