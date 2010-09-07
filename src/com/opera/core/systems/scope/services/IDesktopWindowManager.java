package com.opera.core.systems.scope.services;

import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.WebDriverException;

import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.scope.DesktopWindowManagerCommand;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowID;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfoList;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch;
import com.opera.core.systems.scope.protos.UmsProtos.Response;

public interface IDesktopWindowManager {

	int getActiveWindowId();

	void closeWindow(Integer window);

	void closeAllWindows();

	List<Integer> getWindowHandles();
	
	int getOpenWindowCount();
	
	void init();
	
	// OBS: Temporary, for testing
	void getWidgetList(int id);
	QuickWindow getWindow(String title);
	int getWindowID(String title);
	List<DesktopWindowInfo> getWindowList();
	int getQuickWidgetID(int id, String property, String value);
	QuickWidget getQuickWidget(int id, String property, String value);
		
}
