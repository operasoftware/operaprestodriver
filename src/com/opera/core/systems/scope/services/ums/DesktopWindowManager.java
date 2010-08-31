package com.opera.core.systems.scope.services.ums;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;

import com.opera.core.systems.scope.AbstractService;
import com.opera.core.systems.scope.DesktopWindowManagerCommand;
import com.opera.core.systems.ScopeServices;

import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
//import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowID;
import com.opera.core.systems.scope.services.IDesktopWindowManager;

public class DesktopWindowManager extends AbstractService implements IDesktopWindowManager {

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
		/*
		Response response = executeCommand(DesktopWindowManagerCommand.GET_ACTIVE_WINDOW, null);
		WindowID.Builder builder = WindowID.newBuilder();
		buildPayload(response, builder);
		return builder.build();
		*/
		return 0;
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

}
