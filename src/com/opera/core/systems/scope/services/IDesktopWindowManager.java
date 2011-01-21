package com.opera.core.systems.scope.services;

import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;

import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;

public interface IDesktopWindowManager {

	/**
	 * Does initalization neccessary to set up the service
	 */
	void init();

	/**
	 * Gets id of active window
	 * 
	 * @return id of active window
	 */
	int getActiveQuickWindowId();

	/**
	 * Gets number of open windows
	 * 
	 * @return number of open windows
	 */
	int getOpenQuickWindowCount();

	/*
	 * @return list of QuickWidgets in the window given by id id
	 */
	List<QuickWidget> getQuickWidgetList(int windowId);

	/**
	 * Gets the window with name name
	 * 
	 * @param title
	 * @return QuickWindow
	 */
	QuickWindow getQuickWindow(String name);

	/**
	 * Gets window id of this window
	 *
	 * @param title
	 * @return id of window with the given title
	 */
	int getQuickWindowID(String title);

	/**
	 * Gets a list of all open windows
	 *
	 * @return list of all open windows
	 */
	List<QuickWindow> getQuickWindowList();

	/**
	 * Gets the name of the window
	 * 
	 * @param windowId
	 * @return name of the window
	 */
	String getQuickWindowName(int windowId);

	/**
	 * Gets a QuickWidget based on a specific property
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param property - property specified for the search 
	 * @param value - value of property given
	 * @return QuickWidget matching the property, or null if no such widget
	 */
	QuickWidget getQuickWidget(int windowId, QuickWidgetSearchType property, String value);
	
	/**
	 * Gets the quick widget in a specified window based on a specific property
	 * and its parent widgets name
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param property - property specified for the search, NAME, POS or TEXT
	 * @param value - value of property given
	 * @param parentName - Name of the widgets parent widget
	 * @return QuickWidget matching the property, or null if no such widget
	 */
	QuickWidget getQuickWidget(int windowId, QuickWidgetSearchType property, String value, String parentName);

	/**
	 * Gets the quick widget based on its position
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param row - row of the widget
	 * @param column - column of the widget
	 * @return QuickWidget matching the parameters, or null if no such widget
	 */
	QuickWidget getQuickWidgetByPos(int windowId, int row, int column);

	/**
	 * Gets the quick widget based on its position and its parent widgets
	 * name
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param row - row of the widget
	 * @param column - column of the widget
	 * @param parentName - Name of the widgets parent widget
	 * @return QuickWidget matching the parameters, or null if no such widget
	 */
	QuickWidget getQuickWidgetByPos(int windowId, int row, int column, String parentName);

	/**
	 * Gets the quick window based on a specific property
	 *
	 * @param property - property to search by
	 * @param value - value of property given
	 * @return QuickWindow matching the parameters, or null if no such widget
	 */
	QuickWindow getQuickWindow(QuickWidgetSearchType property, String value);

	/**
	 * Gets the quick window with the given window id 
	 *
	 * @param windowId - id of the window
	 * @return QuickWindow with id windowId, if any, else null. 
	 */
	QuickWindow getQuickWindowById(int windowId);
}
