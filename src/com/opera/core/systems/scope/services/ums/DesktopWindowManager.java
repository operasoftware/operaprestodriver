package com.opera.core.systems.scope.services.ums;

import java.util.LinkedList;
import java.util.List;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DesktopWindowManagerCommand;
import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.ScopeServices;

//import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfoList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
//import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowID;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.IDesktopUtils;

public class DesktopWindowManager extends AbstractService implements IDesktopWindowManager {
	
	private int activeWindowId = 0;
	private final SystemInputManager systemInputManager;
	private final IDesktopUtils desktopUtils;
	
	 public DesktopWindowManager(IDesktopUtils desktopUtils, SystemInputManager inputManager, ScopeServices services, String version) {
			super(services, version);
			
			this.systemInputManager = inputManager;
			this.desktopUtils = desktopUtils;
			String serviceName = "desktop-window-manager";
			
			if(!isVersionInRange(version, "3.0", serviceName))
				throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
			
			services.setDesktopWindowManager(this);
		}
	 
	 public void init() {}
	  
	 public int getActiveQuickWindowId() {
		Response response = executeCommand(DesktopWindowManagerCommand.GET_ACTIVE_WINDOW, null);
		DesktopWindowID.Builder builder = DesktopWindowID.newBuilder();
		buildPayload(response, builder);
		activeWindowId = builder.build().getWindowID();
		return activeWindowId;
	}
	
	public int getOpenQuickWindowCount() {
		List<DesktopWindowInfo> windows = getDesktopWindowInfoList();
		return windows.size();
	}

	/*private void printQuickWidget(QuickWidgetInfo info) {
		System.out.println("Widget name: " + info.getName());
		System.out.println("       type: " + info.getType());
		System.out.println("    visible: " + info.getVisible());
		System.out.println("       text: " + info.getText());
		System.out.println("      state: " + info.getValue());
		System.out.println("    enabled: " + info.getEnabled());
		System.out.println("    default: " + info.getDefaultLook());
		System.out.println("    focused: " + info.getFocusedLook());
		System.out.println("          x: " + info.getRect().getX());
		System.out.println("          y: " + info.getRect().getY());
		System.out.println("      width: " + info.getRect().getWidth());
		System.out.println("     height: " + info.getRect().getHeight());
		System.out.println(" ");
	}*/
	
	public QuickWidget getQuickWidget(int id, QuickWidgetSearchType property, String value)
	{
		
		return getQuickWidget(id, property, value, "");
		/*if (id < 0) {
			id = getActiveWindowId();
		}
		
		QuickWidgetSearch.Builder searchBuilder = QuickWidgetSearch.newBuilder();
		DesktopWindowID.Builder winBuilder = DesktopWindowID.newBuilder();
		winBuilder.clearWindowID();
		winBuilder.setWindowID(id);
		searchBuilder.setWindowID(winBuilder);
		searchBuilder.setSearchType(property);
		searchBuilder.setData(value);
		
		try {
			Response response = executeCommand(DesktopWindowManagerCommand.GET_QUICK_WIDGET, searchBuilder);
			QuickWidgetInfo.Builder builder = QuickWidgetInfo.newBuilder();
			buildPayload(response, builder);
			QuickWidgetInfo info = builder.build();
			return new QuickWidget(desktopUtils, systemInputManager, info, id);
		}
		catch (WebDriverException e) { 
			//System.out.println("Catching webdriver exception");
			return null;
		}*/
	}
	
	// TODO: FIXME: Do this without getting the list
	// parentName is set to name, pos or text depending on widget.getParentType
	public QuickWidget getQuickWidget(int id, QuickWidgetSearchType property, String value, String parentName)
	{
		if (id < 0) {
			id = getActiveQuickWindowId();
		}
		
		List<QuickWidget> widgets = getQuickWidgetList(id);
		for (QuickWidget widget : widgets) {
			if (property.equals(QuickWidgetSearchType.NAME)){
				if ((parentName.length() == 0 || widget.getParentName().equals(parentName)) 
							&& widget.getName().equals(value)) {
					return widget;
				}
			}
			else if (property.equals(QuickWidgetSearchType.TEXT)) {
				if ((parentName.length() == 0|| widget.getParentName().equals(parentName)) 
							&& widget.getText().trim().equals(value)) {
					return widget;
				}
			}
		}
		return null;
	}

	public QuickWidget getQuickWidgetByPos( int id, int row, int column)
	{
		return getQuickWidgetByPos(id, row, column, "");
	}
	
	// FIXME. TODO: ADD check type of widget too. Also to the other funcs to find widget!
	public QuickWidget getQuickWidgetByPos( int id, int row, int column, String parentName)
	{
		if (id < 0) {
			id = getActiveQuickWindowId();
		}
		
		List<QuickWidget> widgets = getQuickWidgetList(id);
		for (QuickWidget widget : widgets) {
			if ((parentName.length() == 0 || widget.getParentName().equals(parentName)) &&
					// Position is only set on tabbuttons and treeitems
					// so only look for these 
					(widget.getType() == QuickWidgetType.TABBUTTON ||
					widget.getType() == QuickWidgetType.TREEITEM ||
					widget.getType() == QuickWidgetType.THUMBNAIL) &&
					widget.getRow() == row && widget.getColumn() == column) {
				return widget;
			}
		}
		return null;
	}

	public QuickWindow getQuickWindow(QuickWidgetSearchType property, String value)
	{
		List<QuickWindow> windows = getQuickWindowList();
		for (QuickWindow window : windows) {
			if (property.equals(QuickWidgetSearchType.NAME)){
				if (window.getName().equals(value)) {
					return window;
				}
			}
		}
		return null;
	}

	public List<QuickWidget> getQuickWidgetList(int id) {
		if (id <= 0)
			id = getActiveQuickWindowId();
		
		DesktopWindowID.Builder winBuilder = DesktopWindowID.newBuilder();
		winBuilder.clearWindowID();
		if (id >= 0)
			winBuilder.setWindowID(id);
		else {
			winBuilder.setWindowID(activeWindowId);
			id = activeWindowId;
		}
		
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_QUICK_WIDGETS, winBuilder);
		QuickWidgetInfoList.Builder builder = QuickWidgetInfoList.newBuilder();
		builder.clear();
		buildPayload(response, builder);
		QuickWidgetInfoList list = builder.build();
		
		List<QuickWidgetInfo> widgetList = list.getQuickwidgetListList();
		List<QuickWidget> quickWidgetList = new LinkedList<QuickWidget>();
		
		for (QuickWidgetInfo widgetInfo : widgetList) {
			quickWidgetList.add(new QuickWidget(desktopUtils, systemInputManager, widgetInfo, id));
		}
		return quickWidgetList;
	}
	
	public List<QuickWindow> getQuickWindowList() {
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_WINDOWS, null);
		DesktopWindowList.Builder builder = DesktopWindowList.newBuilder();
		builder.clear();
		buildPayload(response, builder);
		DesktopWindowList list = builder.build();
		
		List<DesktopWindowInfo> windowInfoList = list.getWindowListList();
		List<QuickWindow> windowList = new LinkedList<QuickWindow>();
		
		for (DesktopWindowInfo windowInfo : windowInfoList) {
			//System.out.println("Window id =" + window.getWindowID() + ", name=" + window.getName() + ", type="+ window.getWindowType());
			windowList.add(new QuickWindow(windowInfo));
		}
		return windowList;
	}
	
	public List<DesktopWindowInfo> getDesktopWindowInfoList() {
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_WINDOWS, null);
		DesktopWindowList.Builder builder = DesktopWindowList.newBuilder();
		buildPayload(response, builder);
		DesktopWindowList list = builder.build();
		
		List<DesktopWindowInfo> windowList = list.getWindowListList();
		
		/*
		for (DesktopWindowInfo window : windowList) {
			//System.out.println("Window id =" + window.getWindowID() + ", name=" + window.getName() + ", type="+ window.getWindowType());
		}
		*/
		
		return windowList;
	}
	
	public int getQuickWindowID(String name) {
		QuickWindow win = getQuickWindow(name);
		if (win != null) 
			return win.getWindowID();
		else 
			return -1;
	}
	
	public QuickWindow getQuickWindow(String name) {
		List<DesktopWindowInfo> windowList = getDesktopWindowInfoList();
		for (DesktopWindowInfo window : windowList) {
			if (window.getName().equals(name))
				return new QuickWindow(window);
		}
		return null;
	}
	
	public QuickWindow getQuickWindowById(int windowId) {
		List<DesktopWindowInfo> windowList = getDesktopWindowInfoList();
		for (DesktopWindowInfo window : windowList) {
			if (window.getWindowID() == windowId)
				return new QuickWindow(window);
		}
		return null;
	}
	
	public String getQuickWindowName(int win_id) {
		QuickWindow window = getQuickWindowById(win_id);
		return (window == null ? "" : window.getName());
		/*List<DesktopWindowInfo> windowList = getWindowList();
		for (DesktopWindowInfo window : windowList) {
			if (window.getWindowID() == win_id)
				return window.getName();
		}
		return "";*/
	}
}
