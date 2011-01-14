package com.opera.core.systems;

//import java.util.HashMap;
//import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

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

	public OperaDesktopDriver(OperaDriverSettings settings){
		super(settings);
	}
	
	/**
	 * For testing override this method.
	 */
	protected void init() {
		super.init();
		
		desktopWindowManager = services.getDesktopWindowManager();
		systemInputManager = services.getSystemInputManager();
		desktopUtils = services.getDesktopUtils();
		
		// If the Opera Binary isn't set we are assuming Opera is up and we 
		// can ask it for the location of itself
		if (this.settings != null && this.settings.getOperaBinaryLocation() == null) {
			String opera_path = desktopUtils.getOperaPath();
			
			logger.info("OperaBinaryLocation retrieved from Opera: " + opera_path);

			if (!opera_path.isEmpty()) {
				this.settings.setOperaBinaryLocation(opera_path);
				
				// Get pid of Opera, needed to wait for it to quit after calling quit_opera
				int pid = desktopUtils.getOperaPid();
				
				// Now create the OperaLauncherRunner that we have the binary path
				this.operaRunner = new OperaLauncherRunner(this.settings);
				
				// Quit and wait for opera to quit properly
				this.services.quit(this.operaRunner, pid);
				
				// Work around stop and restart Opera so the Launcher has control of it now
				// Initialising the services will start Opera if the OperaLauncherRunner is
				// setup correctly
				this.init();
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
	 */
	public void quitOpera() {
		if (this.operaRunner != null){
			if (this.operaRunner.isOperaRunning()) {
				this.operaRunner.stopOpera();
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
				
				// Reset the runner
				this.operaRunner = null;
			}
		}
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
	
	public void resetOperaPrefs(String newPrefs) {
		resetOperaPrefs(this.getLargePreferencesPath(), getLargePreferencesPath(), getCachePreferencesPath(), newPrefs);
	}
	
	public void resetOperaPrefs(String largePrefsPath, String smallPrefspath, String cachePrefsPath, String newPrefs) {
		quitOpera();
		deleteFolder(largePrefsPath);
		copyFolder(newPrefs, largePrefsPath);
		startOpera();
	}
	
	private void startOpera() {
		init();
	}
	
	private boolean deleteFolder(String folderpath) {
		File dir = new File(folderpath);
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteFolder(children[i]);
				if (!success) {
					//return false;
				}
	        }
		}
        // The directory/file is now empty so delete it
        return dir.delete();
	}
	
	private void copyFolder(String from, String to) {
		try {
			copyFolder(new File(from), new File(to));
		} catch (IOException ex) {
			
		}
	}
	
	private void copyFolder(File srcPath, File dstPath) throws IOException {
		
		if (srcPath.isDirectory()){
			
			// Create destination folder if needed
			if (!dstPath.exists()){
				dstPath.mkdir();
			}
			
			String files[] = srcPath.list();
			for(int i = 0; i < files.length; i++){
				copyFolder(new File(srcPath, files[i]), new File(dstPath, files[i]));
			}
		}
		else {
			if(!srcPath.exists()){
				System.out.println("File or directory does not exist.");
			}
			else
			{
				InputStream in = new FileInputStream(srcPath);
				OutputStream out = new FileOutputStream(dstPath); 

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				
				in.close();
				out.close();
			}
		}
	}
}


