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
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopStringID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopStringText;
import com.opera.core.systems.scope.services.IDesktopWindowManager;

public class DesktopWindowManager extends AbstractService implements IDesktopWindowManager {
	
	private int activeWindowId = 0;

	 public DesktopWindowManager(ScopeServices services, String version) {
			super(services, version);
			
			String serviceName = "desktop-window-manager";
			
			if(!isVersionInRange(version, "5.0", serviceName))
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
	
	public void closeWindow(Integer window) {
		// TODO FIXME
	}

	public void closeAllWindows() {
		// TODO FIXME
	}

	public List<Integer> getWindowHandles() {
		List<DesktopWindowInfo> windows = getWindowList();
		List<Integer> handles = new LinkedList<Integer>();
		for (DesktopWindowInfo info : windows) {
			handles.add(new Integer(info.getWindowID()));
		}
		return handles;
	}

	public int getOpenWindowCount() {
		List<DesktopWindowInfo> windows = getWindowList();
		return windows.size();
	}

	private void printQuickWidget(QuickWidgetInfo info) {
		System.out.println("Widget id =" + info.getWidgetID() + ", name=" + info.getName() + ", type="+ info.getType());
		System.out.println("    visible = " + info.getVisible() + ", enabled = " + info.getState());
		System.out.println("    text = " + info.getText());
		
	}
	
	public QuickWidget getQuickWidget(int id, String property, String value)
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
			
			printQuickWidget(info);
			return new QuickWidget(info);
		}
		catch (WebDriverException e) { 
			return null;
		}
	}
	
	// OBS: temporary
	public void getWidgetList(int id) {
		// Sets the internal member for the active window
		getActiveWindowId();
		
		DesktopWindowID.Builder winBuilder = DesktopWindowID.newBuilder();
		winBuilder.clearWindowID();
		if (id >= 0)
			winBuilder.setWindowID(id);
		else
			winBuilder.setWindowID(activeWindowId);
		
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_QUICK_WIDGETS, winBuilder);
		QuickWidgetInfoList.Builder builder = QuickWidgetInfoList.newBuilder();
		buildPayload(response, builder);
		QuickWidgetInfoList list = builder.build();
		
		List<QuickWidgetInfo> widgetList = list.getQuickwidgetListList();
		
		for (QuickWidgetInfo widget : widgetList) {
			printQuickWidget(widget);
		}
	}
	
	public List<DesktopWindowInfo> getWindowList() {
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_WINDOWS, null);
		DesktopWindowList.Builder builder = DesktopWindowList.newBuilder();
		buildPayload(response, builder);
		DesktopWindowList list = builder.build();
		
		List<DesktopWindowInfo> windowList = list.getWindowListList();
		
		for (DesktopWindowInfo window : windowList) {
			System.out.println("Window id =" + window.getWindowID() + ", name=" + window.getName() + ", type="+ window.getWindowType());
		}
		
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
