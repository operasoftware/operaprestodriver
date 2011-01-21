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

/**
 * Desktop Driver
 *
 * @author Adam Minchinton, Karianne Ekern
 *
 */
public class OperaDesktopDriver extends OperaDriver {
	private IDesktopWindowManager desktopWindowManager;
	private SystemInputManager systemInputManager;
	private IDesktopUtils desktopUtils;
	private ProfileUtils profileUtils;
	private boolean	firstTestRun = true;

	/**
	 * Constructor that starts Opera if it's not running
	 * 
	 * @param settings settings for binary path to Opera, prefs directory, and arguments
	 */
	public OperaDesktopDriver(OperaDriverSettings settings) {
		super(settings);
	}

	/**
	 * Initializes services and starts Opera.
	 * 
	 * If OperaBinaryLocation is not set, the binary location is retrieved from the connected
	 * Opera instance, before shutting it down, waiting for it to quit properly,
	 * and then restarting it under the control of the {@link OperaLauncherRunner}.
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
				&& !this.settings.getNoRestart()) {
			String operaPath = getOperaPath();

			logger.info("OperaBinaryLocation retrieved from Opera: " + operaPath);

			if (!operaPath.isEmpty()) {
				this.settings.setOperaBinaryLocation(operaPath);

				// Get pid of Opera, needed to wait for it to quit
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

	protected Map<String, String> getServicesList() {
		Map<String, String> versions = super.getServicesList();
		// This is the minimum versions of the services this version
		// of the web-driver require work
		versions.put("desktop-window-manager", "2.0");
		versions.put("system-input", "1.0");
		versions.put("desktop-utils", "2.0");
		return versions;
	}

	/*protected IDesktopWindowManager getDesktopWindowManager() {
		return desktopWindowManager;
	}

	protected SystemInputManager getSystemInputManager() {
		return systemInputManager;
	}*/

	/**
	 * Shuts down the driver without quiting Opera.
	 */
	public void quitDriver() {
		super.shutdown();
	}

	/**
	 * Quits Opera, cuts the connection to free the port and shuts down the services.
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
		} else {
			// Quit with action as opera wasn't started with the launcher
			String operaPath = desktopUtils.getOperaPath();
			logger.info("OperaBinaryLocation retrieved from Opera: " + operaPath);

			int pid = 0;
			if (!operaPath.isEmpty()) {
				this.settings.setOperaBinaryLocation(operaPath);
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
	 * Gets the id of the active QuickWindow
	 * 
	 * @return id of active window
	 */
	public int getActiveQuickWindowID() {
		return desktopWindowManager.getActiveQuickWindowId();
	}

	/**
	 * Gets a list of all widgets in the window with the given window name
	 *
	 * @param windowName name of window to list widgets inside
	 * @return list of widgets in the window with name windowName
	 *       If windowName is empty, it gets the widgets in the active window
	 */
	public List<QuickWidget> getQuickWidgetList(String windowName) {
		int id = getQuickWindowID(windowName);
		
		if (id >= 0 || windowName.length() == 0) {
			return getQuickWidgetList(id);
		}
		return null;
	}

	/**
	 * Gets a list of all widgets in the window with the given window id.
	 * 
	 * @param windowId - windowId of window to get widgets in
	 * @return list of widgets in the window with id windowId
	 *       If windowId -1, gets the widgets in the active window
	 */
	public List<QuickWidget> getQuickWidgetList(int windowId) {
		return desktopWindowManager.getQuickWidgetList(windowId);
	}


	/**
	 * Gets a list of all open QuickWindows.
	 *
	 * @return list of windows
	 */
	public List<QuickWindow> getQuickWindowList() {
		return desktopWindowManager.getQuickWindowList();
	}

	/**
	 * 
	 * @param windowName name of the window
	 * @return window id of the QuickWindow with name windowName, or -1
	 *   if no such window exists
	 */
	public int getQuickWindowID(String windowName) {
		return desktopWindowManager.getQuickWindowID(windowName);
	}

	/**
	 * @param windowName name of the window
	 * @return QuickWindow with the given name, or null if no such window exists
	 */
	public QuickWindow getQuickWindow(String windowName) {
		return desktopWindowManager.getQuickWindow(windowName);
	}

	/**
	 * Finds widget with the given name in the window with the given window id
	 * 
	 * @param windowId window id of parent window
	 * @param widgetName name of widget to find
	 * 
	 * @return QuickWidget with the given name in the window with id windowId, or null
	 *   if no such widget exists.
	 */
	public QuickWidget findWidgetByName(int windowId, String widgetName) {
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.NAME, widgetName);
	}

	/**
	 * Finds widget with the given name and parent with name as specified in the window
	 * with the specified window id
	 *
	 * @param windowId id of parent window
	 * @param widgetName name of widget
	 * @param parentName name of parent widget
	 * @return QuickWidget with widgetName and parent in the window given by windowId,
	 *   or null if no such widget exists
	 */
	public QuickWidget findWidgetByName(int windowId, String widgetName, String parentName) {
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.NAME, widgetName, parentName);
	}

	/**
	 * Finds widget with the text specified in the window with the given window id.
	 * 
	 * Note, if there are several widgets in this window with the same text, the widget 
	 * returned can be any one of those
	 * 	
	 * @param windowId - id of parent window
	 * @param text - text of widget
	 * @return QuickWidget with the given text in the specified window, or null if no
	 *   such widget exists.
	 */
	public QuickWidget findWidgetByText(int windowId, String text) {
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.TEXT, text);
	}

	/**
	 * Find widget by text and parent widget name in the given window.
	 * 
	 * @param windowId id of parent window
	 * @param text text of widget
	 * @param parentName name of parent widget
	 * @return QuickWidget with the given text and parent in the window with id windowId, or
	 *  null if no such widget exists
	 */
	public QuickWidget findWidgetByText(int windowId, String text, String parentName) {
		return desktopWindowManager.getQuickWidget(windowId, QuickWidgetSearchType.TEXT, text, parentName);
	}

	/**
	 * Finds widget with the text specified by string id in the window with the given id.
	 * 	
	 * @param windowId id of parent window
	 * @param stringId string id of the widget
	 * @return QuickWidget or null if no matching widget found
	 */
	public QuickWidget findWidgetByStringId(int windowId, String stringId) {
		String text = desktopUtils.getString(stringId);
		return findWidgetByText(windowId, text);
	}

	/**
	 * Finds widget with the text specified by string id and widget with parentName in the 
	 * specified window.
	 * 
	 * @param windowId id of parent window
	 * @param stringId stringid of the widget
	 * @param parentName name of parent widget
	 * @return QuickWidget, or null if no matching widget found
	 */
	public QuickWidget findWidgetByStringId(int windowId, String stringId, String parentName) {
		String text = desktopUtils.getString(stringId);
		return findWidgetByText(windowId, text, parentName);
	}

	/**
	 * Find widget by specified position. Used for widgets that have a position only,
	 * e.g. treeviewitems and tabs. 
	 * 
	 * @param windowId id of parent window
	 * @param row row of widget within its parent
	 * @param column column of widget within its parent
	 * @return QuickWidget matching, or null if no matching widget is  found
	 */
	public QuickWidget findWidgetByPosition(int windowId, int row, int column) {
		return desktopWindowManager.getQuickWidgetByPos(windowId, row, column);
	}

	/**
	 * Finds widget by specified position. Used for widgets that have a position,
	 * e.g. treeviewitems and tabs.
	 * 
	 * @param windowId id of parent window
	 * @param row row of widget within its parent
	 * @param column column of widget within its parent
	 * @param parentName name of parent widget
	 * @return QuickWidget, or null if no matching widget is found
	 */
	public QuickWidget findWidgetByPosition(int windowId, int row, int column, String parentName) {
		return desktopWindowManager.getQuickWidgetByPos(windowId, row, column, parentName);
	}

	/**
	 * Finds a Window by its name
	 * 
	 * @param windowName name of window
	 * @return QuickWindow or null if no window with windowName is found
	 */
	public QuickWindow findWindowByName(String windowName) {
		return desktopWindowManager.getQuickWindow(QuickWidgetSearchType.NAME, windowName);
	}

	/**
	 * Find window by window id
	 * 
	 * @param windowId id of window
	 * @return QuickWindow or null if no window with the id found
	 */
	public QuickWindow findWindowById(int windowId) {
		return desktopWindowManager.getQuickWindowById(windowId);
	}


	/**
	 * Gets the name of the window from its window id
	 * 
	 * @param windowId window id of window
	 * @return String: name of the window with id windowId
	 */
	public String getQuickWindowName(int windowId) {
		return desktopWindowManager.getQuickWindowName(windowId);
	}

	/**
	 *
	 * @param enumText
	 * @return the string specified by the id @param enum_text
	 */
	public String getString(String enumText) {
		return desktopUtils.getString(enumText);
	}

	/**
	 * Gets the path to the connected Opera instance
	 * 
	 * @return the path to the connected Opera instance
	 */
	public String getOperaPath() {
		return desktopUtils.getOperaPath();
	}

	/**
	 * 
	 * @return large preferences path
	 */
	public String getLargePreferencesPath() {
		return desktopUtils.getLargePreferencesPath();
	}

	/**
	 * 
	 * @return small preferences path
	 */
	public String getSmallPreferencesPath() {
		return desktopUtils.getSmallPreferencesPath();
	}

	/**
	 * 
	 * @return cache preferences path
	 */
	public String getCachePreferencesPath() {
		return desktopUtils.getCachePreferencesPath();
	}

	/**
	 * Press Key with modifiers held down
	 *
	 * @param key key to press
	 * @param modifier modifiers held
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

	/**
	 * 
	 * @return number of open windows
	 */
	public int getQuickWindowCount() {
		return desktopWindowManager.getOpenQuickWindowCount();
	}

	/**
	 * Executes an opera action.
	 * 
	 * @param using - action_name
	 * @param data -  data parameter
	 * @param dataString - data string parameter
	 * @param dataStringParam - parameter to data string
	 */
	public void operaDesktopAction(String using, int data, String dataString, String dataStringParam) {
		exec.action(using, data, dataString, dataStringParam);
	}

	/*
	 * Starts a process of waiting for some event.
	 * 
	 * After this call, messages to the driver about window events are not thrown away,
	 * so that the notification about window shown is not lost because of other events or messages 
	 */
	public void waitStart() {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");

		services.waitStart();
	}

	/**
	 * Waits for any window update event
	 */
	public void waitForWindowUpdated() {
		waitForWindowUpdated("");
	}

	/**
	 * Waits for any window activated event
	 */
	public void waitForWindowActivated() {
		waitForWindowActivated("");
	}

	/**
	 * Waits for any window close event
	 */
	public void waitForWindowClose() {
		waitForWindowClose("");
	}

	/**
	 * Waits until the window is shown, and then returns the
	 * window id of the window
	 *
	 * @param windowName - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowShown(String windowName) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");

		return services.waitForDesktopWindowShown(windowName, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	/**
	 * Waits until the window is updated,
	 * and then returns the window id of the window.
	 *
	 * @param windowName - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowUpdated(String windowName) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");

		return services.waitForDesktopWindowUpdated(windowName, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	/**
	 * Waits until the window is activated, 
	 * and then returns the window id of the window.
	 *
	 * @param windowName - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowActivated(String windowName) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");

		return services.waitForDesktopWindowActivated(windowName, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());

	}

	/**
	 * Waits until the window is closed, 
	 * and then returns the window id of the window.
	 * 
	 * @param windowName - window to wait for shown event on
	 * @return id of window
	 * @throws CommuncationException if no connection
	 */
	public int waitForWindowClose(String windowName) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");
		
		return services.waitForDesktopWindowClosed(windowName, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}
	
	public int waitForWindowLoaded(String windowName) {
		if (services.getConnection() == null)
			throw new CommunicationException("waiting for a window failed because Opera is not connected.");

		return services.waitForDesktopWindowLoaded(windowName, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	/**
	 *
	 * resetOperaPrefs - restarts Opera after copying over newPrefs to profile,
	 *                    if newPrefs folder exists.
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
		if (!firstTestRun || new File(newPrefs).exists()) {
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
	 * Deletes the profile for the connected Opera instance.
	 * 
	 * Should only be called after the given Opera instance has quit
	 * Presupposes the connected Opera instance is running on same node
	 */
	public void deleteOperaPrefs() {
		profileUtils.deleteProfile();
	}

	/**
	 *  Starts Opera. This will reinitialize services.
	 */
	private void startOpera() {
		init();
	}
}


