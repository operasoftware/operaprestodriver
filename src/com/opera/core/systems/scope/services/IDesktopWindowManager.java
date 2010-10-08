package com.opera.core.systems.scope.services;

import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;

import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;

public interface IDesktopWindowManager {

	/*
	 * @return id of active window 
	 */
	int getActiveWindowId();

	/*
	 * @return list of ids of open windows
	 */
	//List<Integer> getWindowHandles();
	
	/*
	 * @return number of open windows
	 */
	int getOpenWindowCount();
	
	void init();

	/*
	 * @return list of QuickWidgets in the window given by id id
	 */
	List<QuickWidget> getQuickWidgetList(int id);
	
	/*
	 * Get the window with title title
	 * @param title
	 * @return QuickWindow
	 */
	QuickWindow getWindow(String title);
	
	/*
	 * 
	 * @param title
	 * @return id of window with the given title
	 */
	int getWindowID(String title);
	
	/*
	 * 
	 * @return list of all open windows
	 */
	List<QuickWindow> getQuickWindowList();

	/*
	 * 
	 * @param windowId
	 * @return name of the window
	 */
	String getWindowName(int windowId);

	/*
	 * Get the quick widget with value value for property property in the window
	 * specified by the id
	 *   
	 * @param id - window id
	 * @param property
	 * @param value - value of property given
	 * @return
	 */
	QuickWidget getQuickWidget(int id, QuickWidgetSearchType property, String value);
	QuickWidget getQuickWidget(int id, QuickWidgetSearchType property, String value, String parentName);
	
	/*
	 * Get the quick window with value value for property property 
	 *   
	 * @param property
	 * @param value - value of property given
	 * @return
	 */
	QuickWindow getQuickWindow(QuickWidgetSearchType property, String value);

	/*
	 * Get the string specified by the given string id 
	 * @param enum_text
	 * @return
	 */
	String getString(String enum_text);
	
}
