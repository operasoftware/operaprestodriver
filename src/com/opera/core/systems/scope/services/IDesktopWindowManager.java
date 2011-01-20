package com.opera.core.systems.scope.services;

import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;

import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;

public interface IDesktopWindowManager {

	/**
	 * init - do initalization neccessary to set up the service
	 */
	void init();

	/*
	 * @return id of active window
	 */
	int getActiveQuickWindowId();

	/*
	 * @return number of open windows
	 */
	int getOpenQuickWindowCount();

	/*
	 * @return list of QuickWidgets in the window given by id id
	 */
	List<QuickWidget> getQuickWidgetList(int windowId);

	/*
	 * Get the window with title title
	 * @param title
	 * @return QuickWindow
	 */
	QuickWindow getQuickWindow(String title);

	/*
	 *
	 * @param title
	 * @return id of window with the given title
	 */
	int getQuickWindowID(String title);

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
	String getQuickWindowName(int windowId);

	/**
	 * Get the quick widget with value value for property property in
	 * the window specified by the id.
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param property - property specified for the search, NAME, POS or TEXT
	 * @param value - value of property given
	 * @return QuickWidget matching the property, or null if no such widget
	 */
	QuickWidget getQuickWidget(int windowId, QuickWidgetSearchType property, String value);
	
	/**
	 * Get the quick widget with value value for property property in 
	 * the window specified by the id.
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param property - property specified for the search, NAME, POS or TEXT
	 * @param value - value of property given
	 * @param parentName - Name of the widgets parent widget
	 * @return QuickWidget matching the property, or null if no such widget
	 */
	QuickWidget getQuickWidget(int windowId, QuickWidgetSearchType property, String value, String parentName);

	/**
	 * Get the quick widget with value value by position.
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param row - row of the widget
	 * @param column - column of the widget
	 * @return QuickWidget matching the parameters, or null if no such widget
	 */
	QuickWidget getQuickWidgetByPos(int windowId, int row, int column);

	/**
	 * Get the quick widget with value value by position.
	 *
	 * @param windowId - window id of the window containing the widget
	 * @param row - row of the widget
	 * @param column - column of the widget
	 * @param parentName - Name of the widgets parent widget
	 * @return QuickWidget matching the parameters, or null if no such widget
	 */
	QuickWidget getQuickWidgetByPos(int windowId, int row, int column, String parentName);

	/**
	 * Get the quick window with value value for property property.
	 *
	 * @param property - property to search by, NAME, POS or TEXT
	 * @param value - value of property given
	 * @return QuickWindow matching the parameters, or null if no such widget
	 */
	QuickWindow getQuickWindow(QuickWidgetSearchType property, String value);

	/**
	 * Get the quick window with value value for property property.
	 *
	 * @param windowId - id of the window
	 * @return QuickWindow with id windowId, if any, else null. 
	 */
	QuickWindow getQuickWindowById(int windowId);
}
