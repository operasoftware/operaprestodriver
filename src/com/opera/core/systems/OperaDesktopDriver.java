/*
Copyright 2008-2011 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.opera.core.systems;

import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.runner.launcher.OperaLauncherRunnerSettings;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetInfo.QuickWidgetType;
import com.opera.core.systems.scope.protos.DesktopWmProtos.QuickWidgetSearch.QuickWidgetSearchType;
import com.opera.core.systems.scope.protos.SystemInputProtos.ModifierPressed;
import com.opera.core.systems.scope.services.IDesktopUtils;
import com.opera.core.systems.scope.services.IDesktopWindowManager;
import com.opera.core.systems.scope.services.ISystemInput;
import com.opera.core.systems.settings.OperaDriverSettings;
import com.opera.core.systems.util.ProfileUtils;

import org.openqa.selenium.Capabilities;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An extension of OperaDriver for testing the interface of the Opera desktop browser.
 *
 * @author Adam Minchinton, Karianne Ekern
 */
public class OperaDesktopDriver extends OperaDriver {

  private OperaLauncherRunnerSettings settings;

  private IDesktopWindowManager desktopWindowManager;
  private ISystemInput systemInputManager;
  private IDesktopUtils desktopUtils;
  private ProfileUtils profileUtils;
  private boolean firstTestRun = true;

  private String largePreferencesPath;
  private String smallPreferencesPath;
  private String cachePreferencesPath;

  public OperaDesktopDriver() {
    super();
    initDesktopDriver();
  }

  /**
   * Constructor that starts Opera if it's not running.
   *
   * @param settings settings for binary path to Opera, prefs directory, and arguments
   */
  @Deprecated
  public OperaDesktopDriver(OperaDriverSettings settings) {
    this(settings.getCapabilities());
  }

  /**
   * Constructor that starts Opera if it's not running.
   *
   * @param c Settings for binary path to Opera, prefs directory, arguments and more.
   */
  public OperaDesktopDriver(Capabilities c) {
    // OperaDriver constructor will initialize services and start Opera
    // if the binaryPath is set in settings (by calling init in OperaDriver)
    super(c);
    initDesktopDriver();
  }

  private void setPrefsPaths() {
    // Opera will be running at this point so we can retrieve and
    // store all the profile folders
    largePreferencesPath = getLargePreferencesPath();
    smallPreferencesPath = getSmallPreferencesPath();
    cachePreferencesPath = getCachePreferencesPath();
    profileUtils =
        new ProfileUtils(largePreferencesPath, smallPreferencesPath, cachePreferencesPath,
                         capabilities);
  }

  private void setServices() {
    desktopWindowManager = services.getDesktopWindowManager();
    systemInputManager = services.getSystemInputManager();
    desktopUtils = services.getDesktopUtils();
  }

  private void startOpera() {
    // Reinitialize services, and start Opera if binaryLoc is set
    super.init();
    setServices();
    setPrefsPaths();
  }

  /**
   * Initializes services and starts Opera.
   *
   * If OperaBinaryLocation is not set, the binary location is retrieved from the connected Opera
   * instance, before shutting it down, waiting for it to quit properly, and then restarting it
   * under the control of the {@link OperaLauncherRunner}.
   */
  private void initDesktopDriver() {
    //super.init();

    setServices();
    setPrefsPaths();

    // Start Opera if it is not already running

    // If the Opera Binary isn't set we are assuming Opera is up and we
    // can ask it for the location of itself
    if (capabilities != null && capabilities.getCapability(OperaDriver.BINARY) == null
        && !((Boolean) capabilities.getCapability(OperaDriver.NO_RESTART))) {

      String operaPath = getOperaPath();

      logger.info("OperaBinaryLocation retrieved from Opera: " + operaPath);

      if (operaPath.length() > 0) {

        capabilities.setCapability(OperaDriver.BINARY, operaPath);
        settings.setBinary(operaPath);

        // Get pid of Opera, needed to wait for it to quit
        int pid = desktopUtils.getOperaPid();

        // Now create the OperaLauncherRunner that we have the binary path
        runner = new OperaLauncherRunner(settings);

        // Quit and wait for opera to quit properly
        services.quit(runner, pid);

        // Delete the profile to start the first test with a clean profile
        profileUtils.deleteProfile();

        // Work around stop and restart Opera so the Launcher has control of it now
        // Initialising the services will start Opera if the OperaLauncherRunner is
        // setup correctly
        startOpera();
      }
    }
  }

  @Override
  protected Map<String, String> getServicesList() {
    Map<String, String> versions = super.getServicesList();
    // This is the minimum versions of the services this version
    // of the web-driver require work
    versions.put("desktop-window-manager", "2.0");
    versions.put("system-input", "1.0");
    versions.put("desktop-utils", "2.0");
    return versions;
  }

  /**
   * Shuts down the driver (but not Opera)
   */
  public void quitDriver() {
    logger.fine("Opera Desktop Driver shutting down");
    services.shutdown();
    if (runner != null) {
      runner.shutdown();
    }
  }

  /**
   * Quits Opera, cuts the connection to free the port and shuts down the services.
   */
  public void quitOpera() {
    // running opera under the launcher
    if (runner != null) {
      if (runner.isOperaRunning() || runner.hasOperaCrashed()) {
        // Cut off the services connection to free the port
        services.shutdown();

        // Quit Opera
        runner.stopOpera();
      }
    } else {

      // Quit with action as opera wasn't started with the launcher
      String operaPath = desktopUtils.getOperaPath();
      logger.info("OperaBinaryLocation retrieved from Opera: " + operaPath);

      int pid = 0;
      if (operaPath.length() > 0) {
        capabilities.setCapability(OperaDriver.BINARY, operaPath);
        settings.setBinary(operaPath);
        pid = desktopUtils.getOperaPid();
      }

      // Now create the OperaLauncherRunner that we have the binary path
      // So we can control the shutdown
      runner = new OperaLauncherRunner(settings);

      // Quit and wait for opera to quit properly (calls services.shutdown)
      services.quit(runner, pid);

    }
  }

  /**
   * Gets the id of the active QuickWindow.
   *
   * @return id of active window
   */
  public int getActiveQuickWindowID() {
    return desktopWindowManager.getActiveQuickWindowId();
  }

  /**
   * Gets a list of all widgets in the window with the given window name.
   *
   * @param windowName name of window to list widgets inside
   * @return list of widgets in the window with name windowName If windowName is empty, it gets the
   *         widgets in the active window
   */
  public List<QuickWidget> getQuickWidgetList(String windowName) {
    int id = getQuickWindowID(windowName);

    if (id >= 0 || windowName.isEmpty()) {
      return getQuickWidgetList(id);
    }
    return Collections.emptyList();
  }

  /**
   * Gets a list of all widgets in the window with the given window id.
   *
   * @param windowId - windowId of window to get widgets in
   * @return list of widgets in the window with id windowId If windowId -1, gets the widgets in the
   *         active window
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
   * Get a menu based on its name (Note; the menubar is also seen as a menu)
   *
   * @param menuName (as found in standard_menu.ini)
   * @return QuickMenu with the given name, or null if no such menu is found
   */
  public QuickMenu getQuickMenu(String menuName) {
    return desktopWindowManager.getQuickMenu(menuName);
  }

  /**
   * Get a menu based on its name and windowid - makes it possible to get the menubar of a specific
   * main window
   *
   * @param menuName Name of the menu
   * @param windowId WindowId of the menu (the window it is attached to)
   * @return QuickMenu with given menuName and windowId, or null if no such menu can be found.
   */
  public QuickMenu getQuickMenu(String menuName, int windowId) {
    return desktopWindowManager.getQuickMenu(menuName, windowId);
  }

  /**
   * @return list of all open menus, as QuickMenus
   */
  public List<QuickMenu> getQuickMenuList() {
    return desktopWindowManager.getQuickMenuList();
  }

  /**
   * @return list of all QuickMenuItems in open menus
   */
  public List<QuickMenuItem> getQuickMenuItemList() {
    return desktopWindowManager.getQuickMenuItemList();
  }

  /**
   * @param windowName name of the window
   * @return window id of the QuickWindow with name windowName, or -1 if no such window exists
   */
  public int getQuickWindowID(String windowName) {
    return desktopWindowManager.getQuickWindowID(windowName);
  }

  /**
   * @param windowName name of the window
   * @return QuickWindow with the given name, or null if no such window exists
   */
  public QuickWindow getQuickWindow(String windowName) {
    return desktopWindowManager.getQuickWindowByName(windowName);
  }

  /**
   * Finds widget by name in the window specified by windowId.
   *
   * @param windowId   window id of parent window
   * @param widgetName name of widget to find
   * @return QuickWidget with the given name in the window with id windowId, or null if no such
   *         widget exists.
   */
  public QuickWidget findWidgetByName(QuickWidgetType type, int windowId, String widgetName) {
    return desktopWindowManager
        .getQuickWidget(type, windowId, QuickWidgetSearchType.NAME, widgetName);
  }

  /**
   * Finds widget by name and parent in the window with the specified window id.
   *
   * @param windowId   id of parent window
   * @param widgetName name of widget
   * @param parentName name of parent widget
   * @return QuickWidget with widgetName and parent in the window given by windowId, or null if no
   *         such widget exists
   */
  public QuickWidget findWidgetByName(QuickWidgetType type, int windowId, String widgetName,
                                      String parentName) {
    return desktopWindowManager
        .getQuickWidget(type, windowId, QuickWidgetSearchType.NAME, widgetName, parentName);
  }

  /**
   * Finds widget with the text specified in the window with the given window id.
   *
   * Note, if there are several widgets in this window with the same text, the widget returned can
   * be any one of those
   *
   * @param windowId - id of parent window
   * @param text     - text of widget
   * @return QuickWidget with the given text in the specified window, or null if no such widget
   *         exists.
   */
  public QuickWidget findWidgetByText(QuickWidgetType type, int windowId, String text) {
    return desktopWindowManager.getQuickWidget(type, windowId, QuickWidgetSearchType.TEXT, text);
  }

  /**
   * Finds widget by text and parent widget name.
   *
   * @param windowId   id of parent window
   * @param text       text of widget
   * @param parentName name of parent widget
   * @return QuickWidget with the given text and parent in the window with id windowId, or null if
   *         no such widget exists
   */
  public QuickWidget findWidgetByText(QuickWidgetType type, int windowId, String text,
                                      String parentName) {
    return desktopWindowManager
        .getQuickWidget(type, windowId, QuickWidgetSearchType.TEXT, text, parentName);
  }

  /**
   * Finds widget with the text specified by string id in the window with the given id.
   *
   * @param windowId id of parent window
   * @param stringId string id of the widget
   * @return QuickWidget or null if no matching widget found
   */
  public QuickWidget findWidgetByStringId(QuickWidgetType type, int windowId, String stringId) {
    String text = desktopUtils.getString(stringId, true);
    return findWidgetByText(type, windowId, text);
  }

  /**
   * Finds widget with the text specified by string id and parentName in the specified window.
   *
   * @param windowId   id of parent window
   * @param stringId   stringid of the widget
   * @param parentName name of parent widget
   * @return QuickWidget, or null if no matching widget found
   */
  public QuickWidget findWidgetByStringId(QuickWidgetType type, int windowId, String stringId,
                                          String parentName) {
    String text = desktopUtils.getString(stringId, true);
    return findWidgetByText(type, windowId, text, parentName);
  }

  /**
   * Finds widget by specified position. Used for widgets that have a position only, e.g.
   * treeviewitems and tabs.
   *
   * @param windowId id of parent window
   * @param row      row of widget within its parent
   * @param column   column of widget within its parent
   * @return QuickWidget matching, or null if no matching widget is  found
   */
  public QuickWidget findWidgetByPosition(QuickWidgetType type, int windowId, int row, int column) {
    return desktopWindowManager.getQuickWidgetByPos(type, windowId, row, column);
  }

  /**
   * Finds widget by specified position. Used for widgets that have a position, e.g. treeviewitems
   * and tabs.
   *
   * @param windowId   id of parent window
   * @param row        row of widget within its parent
   * @param column     column of widget within its parent
   * @param parentName name of parent widget
   * @return QuickWidget, or null if no matching widget is found
   */
  public QuickWidget findWidgetByPosition(QuickWidgetType type, int windowId, int row, int column,
                                          String parentName) {
    return desktopWindowManager.getQuickWidgetByPos(type, windowId, row, column, parentName);
  }

  /**
   * @param menuItemText Menu item text
   */
  public void pressQuickMenuItem(String menuItemText, boolean popMenu) { // Note: Used for mac
    desktopWindowManager.pressQuickMenuItem(menuItemText, popMenu);
  }

  /**
   * Get a QuickMenuItem by its action
   *
   * @param action name of action of item (as specified in standard_menu.ini)
   * @return QuickMenuItem
   */
  public QuickMenuItem getQuickMenuItemByAction(String action) {
    return desktopWindowManager.getQuickMenuItemByAction(action);
  }

  /**
   * Get a quickmenuitem by its submenuname
   *
   * @param submenu name of submenu of item (as specified in standard_menu.ini)
   */
  public QuickMenuItem getQuickMenuItemBySubmenu(String submenu) {
    return desktopWindowManager.getQuickMenuItemBySubmenu(submenu);
  }

  /**
   * @param name Name of menuitem For a command/action item, this is its action name For a
   *             command/action item, with a parameter to its action, this is "action, parameter"
   *             (e.g. "Open link, www.elg.no") For an item that opens a submenu, this is the
   *             submenuname (all as found in standard_menu.ini)
   */
  public QuickMenuItem getQuickMenuItemByName(String name) {
    return desktopWindowManager.getQuickMenuItemByName(name);
  }

  /**
   * Get a menuitem by its name and the windowid of the window the menu it is in is in. This is only
   * relevant for the menubar. Makes it possible to distinguish between menubar items in different
   * main windows.
   */
  public QuickMenuItem getQuickMenuItemByName(String name, int window_id) {
    return desktopWindowManager.getQuickMenuItemByName(name, window_id);
  }

  /**
   * Get an item by its text. Not language independant, and therefore not a recommended way to get
   * an item.
   *
   * @param text - text of the item (as shown in UI).
   */
  public QuickMenuItem getQuickMenuItemByText(String text) {
    return desktopWindowManager.getQuickMenuItemByText(text);
  }

  /**
   * Get an item by its position (row). Rows starts counting at 0, and note that also menu
   * separators are counted. Note that this is only unique within a single menu (specified by
   * parentName), if parentName is null, this retrieves the first match.
   *
   * @param row      position (row) in menu of item
   * @param menuName name of menu item is in
   */
  public QuickMenuItem getQuickMenuItemByPosition(int row, String menuName) {
    return desktopWindowManager.getQuickMenuItemByPosition(row, menuName);
  }

  /**
   * @param key      accelerator key in menu of item (the letter that's underlined in the menu item
   *                 text) Note: not platform independant, as it cannot be used on mac.
   * @param menuName name of menu item is in
   */
  public QuickMenuItem getQuickMenuItemByAccKey(String key, String menuName) {
    return desktopWindowManager.getQuickMenuItemByAccKey(key, menuName);
  }

  /**
   * @param shortcut shortcut of item
   */
  public QuickMenuItem getQuickMenuItemByShortcut(String shortcut) {
    return desktopWindowManager.getQuickMenuItemByShortcut(shortcut);
  }

  /**
   * Get an item in a language independant way from its stringId.
   *
   * @param stringId StringId as found in standard_menu.ini
   */
  public QuickMenuItem getQuickMenuItemByStringId(String stringId) {
    String text = desktopUtils.getString(stringId, true);
    return desktopWindowManager.getQuickMenuItemByText(text);
  }


  /**
   * Find a Window by its name.
   *
   * @param windowName name of window
   * @return QuickWindow or null if no window with windowName is found
   */
  public QuickWindow findWindowByName(String windowName) {
    return desktopWindowManager.getQuickWindow(QuickWidgetSearchType.NAME, windowName);
  }


  /**
   * Find window by window id.
   *
   * @param windowId id of window
   * @return QuickWindow or null if no window with the id found
   */
  public QuickWindow findWindowById(int windowId) {
    return desktopWindowManager.getQuickWindowById(windowId);
  }


  /**
   * Gets the name of the window from its window id.
   *
   * @param windowId window id of window
   * @return String: name of the window with id windowId
   */
  public String getQuickWindowName(int windowId) {
    return desktopWindowManager.getQuickWindowName(windowId);
  }

  /**
   * @return the string specified by the id @param enum_text
   */
  public String getString(String enumText, boolean stripAmpersand) {
    return desktopUtils.getString(enumText, stripAmpersand);
  }

  /**
   * Gets the path to the connected Opera instance.
   *
   * @return the path to the connected Opera instance
   */
  public String getOperaPath() {
    return desktopUtils.getOperaPath();
  }

  /**
   * @return large preferences path
   */
  public String getLargePreferencesPath() {
    return desktopUtils.getLargePreferencesPath();
  }

  /**
   * @return small preferences path
   */
  public String getSmallPreferencesPath() {
    return desktopUtils.getSmallPreferencesPath();
  }

  /**
   * @return cache preferences path
   */
  public String getCachePreferencesPath() {
    return desktopUtils.getCachePreferencesPath();
  }

  /**
   * Press Key with modifiers held down.
   *
   * @param key       key to press
   * @param modifiers modifiers held
   */
  public void keyPress(String key, List<ModifierPressed> modifiers) {
    systemInputManager.keyPress(key, modifiers);
  }

  /**
   * Release key.
   *
   * @param key       key to press
   * @param modifiers modifiers held
   */
  public void keyUp(String key, List<ModifierPressed> modifiers) {
    systemInputManager.keyUp(key, modifiers);
  }

  /**
   * Press Key.
   *
   * @param key       key to press
   * @param modifiers modifiers held
   */
  public void keyDown(String key, List<ModifierPressed> modifiers) {
    systemInputManager.keyDown(key, modifiers);
  }

  /**
   * Gets number of open windows.
   *
   * @return number of open windows
   */
  public int getQuickWindowCount() {
    return desktopWindowManager.getOpenQuickWindowCount();
  }

  /**
   * Executes an opera action.
   *
   * @param using           - action_name
   * @param data            -  data parameter
   * @param dataString      - data string parameter
   * @param dataStringParam - parameter to data string
   */
  public void operaDesktopAction(String using, int data, String dataString,
                                 String dataStringParam) {
    exec.action(using, data, dataString, dataStringParam);
  }

  /**
   * Starts a process of waiting for some event.
   *
   * After this call, messages to the driver about window events are not thrown away, so that the
   * notification about window shown is not lost because of other events or messages
   */
  public void waitStart() {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a window failed because Opera is not connected.");
    }

    services.waitStart();
  }

  /**
   * Waits for any window update event.
   */
  public void waitForWindowUpdated() {
    waitForWindowUpdated("");
  }

  /**
   * Waits for any window activated event.
   */
  public void waitForWindowActivated() {
    waitForWindowActivated("");
  }

  /**
   * Waits for any window close event.
   */
  public void waitForWindowClose() {
    waitForWindowClose("");
  }

  /**
   * Waits until the window is shown, and then returns the window id of the window
   *
   * @param windowName - window to wait for shown event on
   * @return id of window
   */
  public int waitForWindowShown(String windowName) {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a window failed because Opera is not connected.");
    }

    return services
        .waitForDesktopWindowShown(windowName, OperaIntervals.WINDOW_EVENT_TIMEOUT.getValue());
  }

  /**
   * Waits until the window is updated, and then returns the window id of the window.
   *
   * @param windowName - window to wait for shown event on
   * @return id of window
   */
  public int waitForWindowUpdated(String windowName) {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a window failed because Opera is not connected.");
    }

    return services
        .waitForDesktopWindowUpdated(windowName, OperaIntervals.WINDOW_EVENT_TIMEOUT.getValue());
  }

  /**
   * Waits until the window is activated, and then returns the window id of the window.
   *
   * @param windowName - window to wait for shown event on
   * @return id of window
   */
  public int waitForWindowActivated(String windowName) {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a window failed because Opera is not connected.");
    }

    return services
        .waitForDesktopWindowActivated(windowName, OperaIntervals.WINDOW_EVENT_TIMEOUT.getValue());

  }

  /**
   * Waits until the window is closed, and then returns the window id of the window.
   *
   * @param windowName - window to wait for shown event on
   * @return id of window
   */
  public int waitForWindowClose(String windowName) {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a window failed because Opera is not connected.");
    }

    return services
        .waitForDesktopWindowClosed(windowName, OperaIntervals.WINDOW_EVENT_TIMEOUT.getValue());
  }

  /**
   * Waits until the window is loaded and then returns the window id of the window.
   *
   * @param windowName - window to wait for shown event on
   * @return id of window
   */
  public int waitForWindowLoaded(String windowName) {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a window failed because Opera is not connected.");
    }

    return services
        .waitForDesktopWindowLoaded(windowName, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
  }

  /**
   * Waits until the menu is shown, and then returns the name of the window
   *
   * @param menuName window to wait for shown event on
   * @return id of window
   */
  public String waitForMenuShown(String menuName) {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a window failed because Opera is not connected.");
    }

    return services.waitForMenuShown(menuName, OperaIntervals.MENU_EVENT_TIMEOUT.getValue());
  }

  /**
   * Waits until the menu is closed, and then returns the name of the window
   *
   * @param menuName window to wait for shown event on
   * @return id of window
   */
  public String waitForMenuClosed(String menuName) {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a window failed because Opera is not connected.");
    }

    return services.waitForMenuClosed(menuName, OperaIntervals.MENU_EVENT_TIMEOUT.getValue());
  }


  /**
   * Waits until the menu item is pressed and then returns the text of the menu item pressed
   *
   * @param menuItemText - window to wait for shown event on
   * @return text of the menu item
   */
  public String waitForMenuItemPressed(String menuItemText) {
    if (services.getConnection() == null) {
      throw new CommunicationException(
          "waiting for a menu item to be pressed failed because Opera is not connected.");
    }

    return services
        .waitForMenuItemPressed(menuItemText, OperaIntervals.MENU_EVENT_TIMEOUT.getValue());
  }

  /**
   * resetOperaPrefs - restarts Opera after copying over newPrefs to profile, if newPrefs folder
   * exists.
   *
   * Copies prefs in folder newPrefs to the profile for the connected Opera instance. Will first
   * quit Opera, then delete the old prefs, and copy the new prefs over, then restart Opera with the
   * new prefs.
   *
   * Does nothing if profile used is default main user profile
   *
   * @param newPrefs - path to where new prefs to be copied into the prefs folders are located
   */

  // This presupposes Opera is already running
  // meaning operaBinaryLocation will be set already
  public void resetOperaPrefs(String newPrefs) {
    // Always delete and copy over a test profile except for when running
    // the first test which doesn't have a profile to copy over
    if (!firstTestRun || new File(newPrefs).exists()) {
      if (!profileUtils.isMainProfile(smallPreferencesPath) &&
          !profileUtils.isMainProfile(largePreferencesPath) &&
          !profileUtils.isMainProfile(cachePreferencesPath)) {
        // Quit and wait for opera to quit properly
        quitOpera();

        // Cleanup old profile
        if (profileUtils.deleteProfile() == false)
          logger.severe("Could not delete profile");

        /* Copy the profile for the test. This method will return false in case the copying fails or if there is no
        profile to be copied, so we should check that first here.
         */
        if (new File(newPrefs).exists() == false)
          logger.finer("The '" + newPrefs + "' directory doesn't exist, omitting profile copying.");
        else
          if (profileUtils.copyProfile(newPrefs) == false)
            logger.severe("Failed to copy profile from '" + newPrefs);
          else
            logger.finer("Profile from '" + newPrefs +"' copied OK");

        // Relaunch Opera and the webdriver service connection
        startOpera();
      } else {
        logger.warning("Running tests in main user profile");
      }
    }

    // No longer the first test run
    firstTestRun = false;

  }

  /**
   * Deletes the profile for the connected Opera instance.
   *
   * Should only be called after the given Opera instance has quit
   */
  public void deleteOperaPrefs() {
    // Only delete if Opera is currently not running
    // Don't delete in no-launcher mode
    if (runner != null && !runner.isOperaRunning()) {
      // Will only delete profile if it's not a default main profile
      profileUtils.deleteProfile();
    } else {
      logger.warning("Cannot delete profile while Opera is running");
    }
  }

  /**
   * @return Process id of Opera browser that is connected to this driver.
   */
  public int getPid() {
    return desktopUtils.getOperaPid();
  }

  // Note: Should only be used in launcher mode

  /**
   * @return true if Opera is running, and running under the launcher
   */
  public boolean isOperaRunning() {
    return runner != null && isOperaRunning();
  }

}