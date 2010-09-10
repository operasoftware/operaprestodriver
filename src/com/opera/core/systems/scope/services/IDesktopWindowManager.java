package com.opera.core.systems.scope.services;

import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;

import com.opera.core.systems.QuickWidget;
import com.opera.core.systems.QuickWindow;
import com.opera.core.systems.scope.protos.DesktopWmProtos.DesktopWindowInfo;

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
	QuickWidget getQuickWidget(int id, String property, String value);
	String getString(String enum_text);
}
