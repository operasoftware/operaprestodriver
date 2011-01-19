package com.opera.core.systems;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.opera.core.systems.profile.ProfileUtils;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.ums.SystemInputManager;
import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaDesktopDriver extends OperaDriver {
	private IDesktopWindowManager desktopWindowManager;
	private SystemInputManager systemInputManager;
	private IDesktopUtils desktopUtils;
	private ProfileUtils profileUtils;
	private boolean	firstTestRun = true;

	public OperaDesktopDriver(OperaDriverSettings settings){
		super(settings);
	}
	
	/**
	 * Initializes services and starts Opera
	 * If OperaBinaryLocation is not set, the binary location is retrieved from the connected
	 * Opera instance, before shutting it down, waiting for it to quit properly, 
	 * and then restarting it under the control of the LauncherRunner.
	 * 
	 */
	protected void init() {
		super.init();
		
		desktopWindowManager = services.getDesktopWindowManager();
		systemInputManager = services.getSystemInputManager();
		desktopUtils = services.getDesktopUtils();

		// Opera will be running at this point so we can retrieve and
		// store all the profile folders
		settings.SetLargePrefsFolder(getLargePreferencesPath());
		settings.SetSmallPrefsFolder(getSmallPreferencesPath());
		settings.SetCachePrefsFolder(getCachePreferencesPath());
		profileUtils = new ProfileUtils(settings);

		// If the Opera Binary isn't set we are assuming Opera is up and we 
		// can ask it for the location of itself
		if (this.settings != null && this.settings.getOperaBinaryLocation() == null
				&& this.settings.getNoRestart() == false) {
			String opera_path = getOperaPath();
			
			logger.info("OperaBinaryLocation retrieved from Opera: " + opera_path);

			if (!opera_path.isEmpty()) {
				this.settings.setOperaBinaryLocation(opera_path);
				
				// Get pid of Opera, needed to wait for it to quit after calling quit_opera
				int pid = desktopUtils.getOperaPid();
				
				
				// Now create the OperaLauncherRunner that we have the binary path
				this.operaRunner = new OperaLauncherRunner(this.settings);
				
				// Quit and wait for opera to quit properly
				this.services.quit(this.operaRunner, pid);
				
				// Delete the profile to start the first test with a clean profile
				this.profileUtils.deleteProfile();
				
				// Work around stop and restart Opera so the Launcher has control of it now
				// Initialising the services will start Opera if the OperaLauncherRunner is
				// setup correctly
				startOpera();
			}
		}
	}

	// TODO: FIXME
	protected Map<String, String> getServicesList() {
		Map<String, String> versions = super.getServicesList();
		// This is the minimum versions of the services this version
		// of the web-driver require work
		versions.put("desktop-window-manager", "2.0");
		versions.put("system-input", "1.0");
		versions.put("desktop-utils", "2.0");
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
	public void quitDriver() {
		super.shutdown();
	}

	/**
	 * Quit Opera
	 * 
	 * Quits Opera, cuts the connection to free the port and shutdown the services 
	 */
	public void quitOpera() {
		// running opera under the launcher
		if (this.operaRunner != null){
			if (this.operaRunner.isOperaRunning()) {
				// Quit Opera
				this.operaRunner.stopOpera();
				
				// Cut off the services connection to free the port
				this.services.shutdown();
			}
		}
		else {
			// Quit with action as opera wasn't started with the launcher
			String opera_path = desktopUtils.getOperaPath();
			logger.info("OperaBinaryLocation retrieved from Opera: " + opera_path);
			
			int pid = 0;
			if (!opera_path.isEmpty()) {
				this.settings.setOperaBinaryLocation(opera_path);
				pid = desktopUtils.getOperaPid();
			}
				
			// Now create the OperaLauncherRunner that we have the binary path
			// So we can control the shutdown
			this.operaRunner = new OperaLauncherRunner(this.settings);
			
			// Quit and wait for opera to quit properly
			this.services.quit(this.operaRunner, pid);
			
		}
	}

	/**
	 * @return active window id
	 */
	public int getActiveQuickWindowID() {
		return desktopWindowManager.getActiveQuickWindowId();
	}

	/**
	 * @param winName
	 * @return list of widgets in the window with name winName
	 *       If winName is empty, it gets the widgets in the active window
	 */
	public List<QuickWidget> getQuickWidgetList(String winName) {
		int id = getQuickWindowID(winName);
		if (id >= 0 || winName.length() == 0) {
			return getQuickWidgetList(id);
		} 
		// Couldn't find window with winName
		return null;
	}
	
	/**
	 * @param winId - windowId of window to get widgets in
	 * @return list of widgets in the window with id winId
	 *       If winId -1, gets the widgets in the active window
	 */
	public List<QuickWidget> getQuickWidgetList(int winId) {
		return desktopWindowManager.getQuickWidgetList(winId);
	}
	
	
	/**
	 * 
	 * 
	 * @returnlist of windows
	 */
	public List<QuickWindow> getQuickWindowList() {
		return desktopWindowManager.getQuickWindowList();
	}

	/**
	 * @param name
	 * @return window id of window with windowName 
	 */
	public int getQuickWindowID(String windowName) {
		return desktopWindowManager.getQuickWindowID(windowName);
	}
	
	/**
	 * @param windowName
	 * @return QuickWindow with the given name
	 */
	public QuickWindow getQuickWindow(String windowName) {
		return desktopWindowManager.getQuickWindow(windowName);
	}
	
	/**
	 * @param windowId 
	 * @param widgetName
	 * @return QuickWidget with the given name in the window with id windowId
	 */
	public QuickWidget findWidgetByName(int windowId, String widgetName){
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.NAME, widgetName);
	}

	/**
	 * 
	 * @param windowId
	 * @param widgetName
	 * @param parentName
	 * @return QuickWidget with widgetName and parent in the window given by windowId
	 */
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
	public String getQuickWindowName(int windowId) {
		return desktopWindowManager.getQuickWindowName(windowId);
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
	 * @return 
	 */
	public void keyPress(String key, List<ModifierPressed> modifiers) {
		systemInputManager.keyPress(key, modifiers);
	}
	
	public void keyUp(String key, List<ModifierPressed> modifiers) {
		systemInputManager.keyUp(key, modifiers);
	}
	public void keyDown(String key, List<ModifierPressed> modifiers) {
		systemInputManager.keyDown(key, modifiers);
	}
	
	public int getQuickWindowCount() {
		//TODO FIXME
		//return desktopWindowManager.getOpenQuickWindowCount();
		return 0;
	}
	
	/**
	 * Execute opera action
	 * @param using - action_name
	 * @param data -  data parameter
	 * @param dataString - data string parameter
	 * @param dataStringParam - parameter to data string
	 */
	public void operaDesktopAction(String using, int data, String dataString, String dataStringParam) {
		exec.action(using, data, dataString, dataStringParam);
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
	
	// ---------------------- 
	
	/**
	 *
	 * resetOperaPrefs - restart Opera after copying over newPrefs to profile, if newPrefs
	 *                   folder exists.
	 * 
	 * Copies prefs in folder newPrefs to the profile for the connected Opera instance.
	 * Will first quit Opera, then delete the old prefs, and copy the new prefs over, then
	 * restart Opera with the new prefs.
	 * 
	 * @param newPrefs - path to where new prefs to be copied into the prefs folders are located
	 */
	public void resetOperaPrefs(String newPrefs) {
		// Always delete and copy over a test profile except for when running
		// the first test which doesn't have a profile to copy over
		if (firstTestRun == false || new File(newPrefs).exists())
		{
			// Quit and wait for opera to quit properly
			quitOpera();
			
			// Cleanup old profile 
			profileUtils.deleteProfile();
			// Copy in the profile for the test (only if it exists)
			profileUtils.copyProfile(newPrefs);
			
			// Relaunch Opera and the webdriver service connection
			startOpera();
		}
		
		// No longer the first test run
		firstTestRun = false;
	}

	/**
	 * deleteOperaPrefs - delete the profile for the connected Opera instance.
	 *                    should only be called after the given Opera instance has quit
	 * 
	 */
	public void deleteOperaPrefs() {
		profileUtils.deleteProfile();
	}
	
	/**
	 *  Start Opera. This will reinitialize services.
	 * 
	 */
	private void startOpera() {
		init();
	}
	

}


