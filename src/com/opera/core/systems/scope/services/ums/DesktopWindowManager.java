package com.opera.core.systems.scope.services.ums;

import java.util.LinkedList;
import java.util.List;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DesktopWindowManagerCommand;
import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.ScopeServices;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfoList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearchType;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopStringID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopStringText;
import com.opera.core.systems.scope.services.IDesktopWindowManager;

public class DesktopWindowManager extends AbstractService implements IDesktopWindowManager {
	
	private int activeWindowId = 0;
	private final SystemInputManager systemInputManager;
	
	 public DesktopWindowManager(SystemInputManager inputManager, ScopeServices services, String version) {
			super(services, version);
			
			this.systemInputManager = inputManager;
			String serviceName = "desktop-window-manager";
			
			if(!isVersionInRange(version, "2.0", serviceName))
				throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
			
			services.setDesktopWindowManager(this);
		}
	 
	 public void init() {}
	  
	 public int getActiveWindowId() {
		Response response = executeCommand(DesktopWindowManagerCommand.GET_ACTIVE_WINDOW, null);
		DesktopWindowID.Builder builder = DesktopWindowID.newBuilder();
		buildPayload(response, builder);
		activeWindowId = builder.build().getWindowID();
		return activeWindowId;
	}
	
	/*public List<Integer> getWindowHandles() {
		List<DesktopWindowInfo> windows = getWindowList();
		List<Integer> handles = new LinkedList<Integer>();
		for (DesktopWindowInfo info : windows) {
			handles.add(new Integer(info.getWindowID()));
		}
		return handles;
	}*/

	public int getOpenWindowCount() {
		List<DesktopWindowInfo> windows = getWindowList();
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
		if (id < 0) {
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
			return new QuickWidget(this, systemInputManager, info, id);
		}
		catch (WebDriverException e) { 
			//System.out.println("Catching webdriver exception");
			return null;
		}
	}
	
	// TODO: FIXME: Do this without getting the list
	// parentName is set to name, pos or text depending on widget.getParentType
	public QuickWidget getQuickWidget(int id, QuickWidgetSearchType property, String value, String parentName)
	{
		if (id < 0) {
			id = getActiveWindowId();
		}
		List<QuickWidget> widgets = getQuickWidgetList(id);
		for (QuickWidget widget : widgets) {
			if (property.equals(QuickWidgetSearchType.NAME)){
				if (widget.getParentName().equals(parentName) 
							&& widget.getName().equals(value)) {
					return widget;
				}
			}
			else if (property.equals(QuickWidgetSearchType.TEXT)) {
				if (widget.getParentName().equals(parentName) 
							&& widget.getText().equals(value)) {
					return widget;
				}
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
			id = getActiveWindowId();
		
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
			quickWidgetList.add(new QuickWidget(this, systemInputManager, widgetInfo, id));
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
	
	public List<DesktopWindowInfo> getWindowList() {
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
	
	public int getWindowID(String name) {
		QuickWindow win = getWindow(name);
		if (win != null) 
			return win.getWindowID();
		else 
			return -1;
	}
	
	public QuickWindow getWindow(String name) {
		List<DesktopWindowInfo> windowList = getWindowList();
		for (DesktopWindowInfo window : windowList) {
			if (window.getName().equals(name))
				return new QuickWindow(window);
		}
		return null;
	}
	
	public String getWindowName(int win_id) {
		List<DesktopWindowInfo> windowList = getWindowList();
		for (DesktopWindowInfo window : windowList) {
			if (window.getWindowID() == win_id)
				return window.getName();
		}
		return "";
	}
	
	public String getString(String enum_text) {
		DesktopStringID.Builder stringBuilder = DesktopStringID.newBuilder();
		stringBuilder.setEnumText(enum_text);
		
		Response response = executeCommand(DesktopWindowManagerCommand.GET_STRING, stringBuilder);

		DesktopStringText.Builder stringTextBuilder = DesktopStringText.newBuilder();
		buildPayload(response, stringTextBuilder);
		DesktopStringText string_text = stringTextBuilder.build();

		return string_text.getText();
	}
}
