package com.opera.core.systems;

//import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;

public class OperaDesktopDriver extends OperaDriver {
	
	private IDesktopWindowManager desktopWindowManager;
	private SystemInputManager systemInputManager;
	private IDesktopUtils desktopUtils;

	public OperaDesktopDriver(){
		super();
	}

	public OperaDesktopDriver(String executableLocation, String... arguments){
		super(executableLocation, arguments);
	}
	
	/**
	 * For testing override this method.
	 */
	protected void init() {
		super.init();
		desktopWindowManager = services.getDesktopWindowManager();
		systemInputManager = services.getSystemInputManager();
		desktopUtils = services.getDesktopUtils();
	}

	// TODO: FIXME
	protected Map<String, String> getServicesList() {
		Map<String, String> versions = super.getServicesList();
		// This is the minimum versions of the services this version
		// of the web-driver require work
		versions.put("desktop-window-manager", "2.0");
		versions.put("system-input", "1.0");
		versions.put("desktop-utils", "1.0");
		return versions;
	}
	
	protected IDesktopWindowManager getDesktopWindowManager() {
		return desktopWindowManager;
	}
	
	protected SystemInputManager getSystemInputManager() {
		return systemInputManager;
	}

	/**
	 * Shutdown the driver without quiting Opera
	 */
	public void shutdown() {
		services.shutdown();
	}

	/**
	 * @return active window id
	 */
	public int getActiveWindowID() {
		return desktopWindowManager.getActiveWindowId();
	}

	/**
	 * @param win_name
	 * @return list of widgets in the window with name win_name
	 *       If no winName is empty, it gets the widgets in the active window
	 */
	public List<QuickWidget> getQuickWidgetList(String winName) {
		int id = getWindowID(winName);
		if (id >= 0 || winName.length() == 0) {
			return getQuickWidgetList(id);
		} 
		// Couldn't find window with winName
		return null;
	}
	
	public List<QuickWidget> getQuickWidgetList(int id) {
		return desktopWindowManager.getQuickWidgetList(id);
	}
	
	
	public List<QuickWindow> getWindowList() {
		return desktopWindowManager.getQuickWindowList();
	}

	/**
	 * @param name
	 * @return window id of window with window_name 
	 */
	public int getWindowID(String windowName) {
		return desktopWindowManager.getWindowID(windowName);
	}
	
	/**
	 * @param name
	 * @return QuickWindow with the given name
	 */
	public QuickWindow getWindow(String windowName) {
		return desktopWindowManager.getWindow(windowName);
	}
	
	/**
	 * @param windowId
	 * @param widgetName
	 * @return QuickWidget with the given name in the window with id windowId
	 */
	public QuickWidget findWidgetByName(int windowId, String widgetName){
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.NAME, widgetName);
	}

	public QuickWidget findWidgetByName(int windowId, String widgetName, String parentName){
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.NAME, widgetName, parentName);
	}

	public QuickWidget findWidgetByText(int windowId, String text){
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.TEXT, text);
	}
	
	public QuickWidget findWidgetByText(int windowId, String text, String parentName){
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.TEXT, text, parentName);
	}

	public QuickWidget findWidgetByStringId(int windowId, String stringId){
		String text = desktopUtils.getString(stringId);
		return findWidgetByText(windowId, text);
	}
	
	public QuickWidget findWidgetByStringId(int windowId, String stringId, String parentName){
		String text = desktopUtils.getString(stringId);
		return findWidgetByText(windowId, text, parentName);
	}

	public QuickWidget findWidgetByPosition(int windowId, int row, int column){
		return desktopWindowManager.getQuickWidgetByPos(windowId, row, column);
	}
	
	public QuickWidget findWidgetByPosition(int windowId, int row, int column, String parentName){
		return desktopWindowManager.getQuickWidgetByPos(windowId, row, column, parentName);
	}

	public QuickWindow findWindowByName(String windowName){
		return desktopWindowManager.getQuickWindow(QuickWidgetSearchType.NAME, windowName);
	}
	
	public QuickWindow findWindowById(int windowId){
		return desktopWindowManager.getQuickWindowById(windowId);
	}


	/**
	 * @param windowId
	 * @return String: name of the window
	 */
	public String getWindowName(int windowId) {
		return desktopWindowManager.getWindowName(windowId);
	}
	
	/**
	 * 
	 * @param enum_text
	 * @return the string specified by the id @param enum_text
	 */
	public String getString(String enum_text){
		return desktopUtils.getString(enum_text);
	}

	public String getOperaPath() {
		return desktopUtils.getOperaPath();
	}

	public String getLargePreferencesPath() {
		return desktopUtils.getLargePreferencesPath();
	}

	public String getSmallPreferencesPath() {
		return desktopUtils.getSmallPreferencesPath();
	}

	public String getCachePreferencesPath() {
		return desktopUtils.getCachePreferencesPath();
	}

	/**
	 * 
	 * @param key
	 * @param modifier
	 * @return the string specified by the id @param enum_text
	 */
	
	public void keyPress(String key, List<ModifierPressed> modifiers) {
		/*for (ModifierPressed mod : modifiers) {
			if (mod != null)
				System.out.print(mod.toString() + "(" + i + ",");
		}
		System.out.println(")");*/
		systemInputManager.keyPress(key, modifiers);
	}
	
	public void keyUp(String key, List<ModifierPressed> modifiers) {
		systemInputManager.keyUp(key, modifiers);
	}
	public void keyDown(String key, List<ModifierPressed> modifiers) {
		systemInputManager.keyDown(key, modifiers);
	}
	
	/**
	 *
	 */
	/*public void keyPress(String key, ModifierPressed modifier) {
		ArrayList<ModifierPressed> mods = new ArrayList<ModifierPressed>();
		mods.add(modifier);
		systemInputManager.keyPress(key, mods);
	}*/
	
	
	public int getWindowCount() {
		//TODO FIXME
		//return desktopWindowManager.getOpenWindowCount();
		return 0;
	}
	
	/**
	 * Execute opera action
	 * @param using - action_name
	 * @param params - parameters to the action
	 */
	public void operaDesktopAction(String using, String... params) 
	{
        // Desktop never wants the window
		exec.action(using, 0, params);
	}
	
	/*
	 * Starts a process of waiting for a window to show
	 * After this call, messages to the driver about window events are not thrown away,
	 * so that the notification about window shown is not lost because of other events or messages 
	 */
	public void waitStart() {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		services.waitStart();
	}

	/**
	 * Wait for any window update event
	 */
	public void waitForWindowUpdated() {
		waitForWindowUpdated("");
	}

	/**
	 * Wait for any window activated event
	 */
	public void waitForWindowActivated() {
		waitForWindowActivated("");
	}

	/**
	 * Wait for any window close event
	 */
	public void waitForWindowClose() {
		waitForWindowClose("");
	}

	/**
	 * Wait until the window given by the @param win_name is shown, and then returns the
	 * window id of this window
	 * 
	 * @param win_name - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowShown(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowShown(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	/**
	 * Wait until the window given by the @param win_name is updated, and then returns the
	 * window id of this window
	 * 
	 * @param win_name - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowUpdated(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowUpdated(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	/**
	 * Wait until the window given by the @param win_name is activated, and then returns the
	 * window id of this window
	 * 
	 * @param win_name - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowActivated(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowActivated(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
		
	}

	/**
	 * Wait until the window given by the @param win_name is closed, and then returns the
	 * window id of this window
	 * 
	 * @param win_name - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowClose(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowClosed(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}
	
	public int waitForWindowLoaded(String win_name) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowLoaded(win_name, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}
	
	
}


