package com.opera.core.systems.scope.services.ums;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DesktopWindowManagerCommand;
import com.opera.core.systems.scope.WindowManagerCommand;
import com.opera.core.systems.ScopeServices;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfoList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.WmProtos.WindowFilter;
import com.opera.core.systems.scope.protos.WmProtos.WindowID;
import com.opera.core.systems.scope.protos.WmProtos.WindowInfo;
import com.opera.core.systems.scope.protos.WmProtos.WindowList;
//import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowID;
import com.opera.core.systems.scope.services.IDesktopWindowManager;

public class DesktopWindowManager extends AbstractService implements IDesktopWindowManager {
	
	private int activeWindowId = 0;

	 public DesktopWindowManager(ScopeServices services, String version) {
			super(services, version);
			
			String serviceName = "desktop-window-manager";
			
			if(!isVersionInRange(version, "5.0", serviceName))
				throw new UnsupportedOperationException(serviceName + " version " + version + " is not supported");
			
			services.setDesktopWindowManager(this);
			//windowFinder = JXPathContext.compile("/.[windowType='normal']/windowID");
		}
	  
	public void setActiveWindowId(Integer windowId) {
		// TODO Auto-generated method stub

	}

	public int getActiveWindowId() {
		// TODO Auto-generated method stub
		Response response = executeCommand(DesktopWindowManagerCommand.GET_ACTIVE_WINDOW, null);
		DesktopWindowID.Builder builder = DesktopWindowID.newBuilder();
		buildPayload(response, builder);
		activeWindowId = builder.build().getWindowID();
		return activeWindowId;
	}
	
	

	public void addWindow(DesktopWindowInfo window) {
		// TODO Auto-generated method stub

	}

	public void removeWindow(Integer windowId) {
		// TODO Auto-generated method stub

	}

	public void closeWindow(Integer window) {
		// TODO Auto-generated method stub

	}

	public void closeAllWindows() {
		// TODO Auto-generated method stub

	}

	public void init() {
		// TODO Auto-generated method stub

	}

	public void setActiveWindow(String title) {
		// TODO Auto-generated method stub

	}

	public List<Integer> getWindowHandles() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getOpenWindowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void resetWindowsList() {
		// TODO Auto-generated method stub

	}
	
	// OBS: temporary
	public void getWidgetList(int id) {
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
			//FIXME workaround for CORE-25866
			//if(window.getTitle().length() > 0)
			System.out.println("Widget id =" + widget.getId() + ", title=" + widget.getTitle() + ", type="+ widget.getType() + ", visible = " + widget.getVisible());
		}
	}
	
	public List<DesktopWindowInfo> getWindowList() {
		Response response = executeCommand(DesktopWindowManagerCommand.LIST_WINDOWS, null);
		DesktopWindowList.Builder builder = DesktopWindowList.newBuilder();
		buildPayload(response, builder);
		DesktopWindowList list = builder.build();
		
		List<DesktopWindowInfo> windowList = list.getWindowListList();
		
		for (DesktopWindowInfo window : windowList) {
			//FIXME workaround for CORE-25866
			//if(window.getTitle().length() > 0)
			System.out.println("Window id =" + window.getWindowID() + ", title=" + window.getTitle() + ", type="+ window.getWindowType() + ", titleid=" + window.getTitleid());
		}
		return windowList;
	}
	
	public int getWindowID(String title) {
		List<DesktopWindowInfo> windowList = getWindowList();
		for (DesktopWindowInfo window : windowList) {
			if (window.getTitle().equals(title))
				return window.getWindowID();
		}
		return -1; // uh
	}
	

}
