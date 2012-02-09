/*
Copyright 2008-2012 Opera Software ASA

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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.common.lang.OperaStrings;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.preferences.OperaScopePreferences;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.OperaRunnerSettings;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.runner.launcher.OperaLauncherRunnerSettings;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.handlers.PbActionHandler;
import com.opera.core.systems.scope.internal.OperaFlags;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.ICookieManager;
import com.opera.core.systems.scope.services.ICoreUtils;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.util.CapabilitiesSanitizer;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Beta;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.Keyboard;
import org.openqa.selenium.Mouse;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteLogs;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * OperaDriver is an implementation of the WebDriver interface that allows you to drive the Opera
 * web browser.  The driver uses the Scope protocol to communicate with Opera directly from Java.
 *
 * The implementation is vendor-supported and developed by Opera Software and volunteers.
 */
public class OperaDriver extends RemoteWebDriver implements TakesScreenshot {

  /**
   * (String) How verbose the logging should be. Available levels are: SEVERE (highest value),
   * WARNING, INFO, CONFIG, FINE, FINER, FINEST (lowest value), ALL. Default is INFO.
   */
  public static final String LOGGING_LEVEL = "opera.logging.level";

  /**
   * (String) Where to send the output of the logging. Default is to not write to file.
   */
  public static final String LOGGING_FILE = "opera.logging.file";

  /**
   * (String) Path to the Opera binary to use. If not specified, OperaDriver will guess the path to
   * your Opera installation (typically <code>/usr/bin/opera</code> or <code>C:\Program
   * Files\Opera\opera.exe</code>).
   */
  public static final String BINARY = "opera.binary";

  /**
   * (String) Arguments to pass to Opera, separated by spaces. See <code>opera -help</code> for
   * available command-line switches.
   */
  public static final String ARGUMENTS = "opera.arguments";

  /**
   * (String) The host Opera should connect to. Unless you're starting Opera manually you won't need
   * this.
   */
  public static final String HOST = "opera.host";

  /**
   * (Integer) The port to Opera should connect to. 0 = Random, -1 = Opera default (for use with
   * Opera < 11.60).
   */
  public static final String PORT = "opera.port";

  /**
   * (String) Path to the launcher binary to use. The launcher is a gateway between OperaDriver and
   * the Opera browser, and is being used for controlling the binary and taking external
   * screenshots. If left blank, OperaDriver will use the launcher supplied with the package.
   */
  public static final String LAUNCHER = "opera.launcher";

  /**
   * (Object) Directory to use for the Opera profile. If an {@link OperaProfile} object that will be
   * used when starting opera. If null a random temporary directory is used. If "", an empty string,
   * then the default <code>.autotest</code> profile directory will be used (for backwards
   * compatibility with Opera < 11.60).
   */
  public static final String PROFILE = "opera.profile";

  /**
   * (Boolean) Whether to use Opera's alternative implicit wait implementation. It will use an
   * in-browser heuristic to guess when a page has finished loading, allowing us with great accuracy
   * tell whether there are any planned events in the document. This functionality is useful for
   * very simple test cases, but not designed for real-world testing. It is disabled by default.
   */
  public static final String OPERAIDLE = "opera.idle";

  /**
   * (Integer) The X display to use. (Only works on *nix OSes.)
   */
  public static final String DISPLAY = "opera.display";

  /**
   * (Boolean) Whether to auto-start the Opera binary. If false, OperaDriver will wait for a
   * connection from the browser. Go to "opera:debug", enter the correct port number, and hit
   * "Connect" to connect manually.
   */
  public static final String AUTOSTART = "opera.autostart";

  /**
   * (Boolean) Whether to restart.
   */
  public static final String NO_RESTART = "opera.no_restart";

  /**
   * (Boolean) Whether to quit Opera when OperaDriver is shut down. If enabled, it will keep the
   * browser running after the driver is shut down.
   */
  public static final String NO_QUIT = "opera.no_quit";

  /**
   * (Boolean) Whether to guess the path to Opera if it isn't set in <code>opera.binary</code>.
   */
  public static final String GUESS_BINARY_PATH = "opera.guess_binary_path";

  /**
   * (String) The product we are using, for example "desktop" or "core".
   */
  public static final String PRODUCT = "opera.product";

  /**
   * (String) Rendering backend used by internal gogi builds of Opera.  If null or an empty string,
   * "software" is used by default.
   *
   * <strong>Warning:</strong> This is a temporary workaround for launcher not taking external
   * window-only screenshots on Windows.
   */
  @Deprecated
  @SuppressWarnings("unused")
  public static final String BACKEND = "opera.backend";

  /*
   * These are "protected" and not "private" so that we can extend this class and add methods to
   * access these variable in tests.
   */
  protected DesiredCapabilities capabilities;
  protected OperaLauncherRunnerSettings settings;
  protected OperaRunner runner;

  protected IEcmaScriptDebugger debugger;
  protected IOperaExec exec;
  protected IWindowManager windowManager;
  protected ICoreUtils coreUtils;
  protected ICookieManager cookieManager;

  protected ScopeServices services;
  protected ScopeActions actionHandler;

  private OperaScopePreferences preferences;

  protected final Logger logger = Logger.getLogger(this.getClass().getName());
  private FileHandler logFile = null;

  protected Set<Integer> objectIds = new HashSet<Integer>();
  private String version;

  private int assignedWindowIds = 0;

  /**
   * Constructor that starts Opera with the default set of capabilities.
   */
  public OperaDriver() {
    this((Capabilities) null);
  }

  /**
   * Starts Opera with the given profile.
   *
   * @param profile the profile to start Opera with
   */
  public OperaDriver(OperaProfile profile) {
    this(profile.getCapabilities());
  }

  /**
   * Starts Opera with the given set of desired capabilities.
   *
   * @param c a {@link DesiredCapabilities} object containing various settings for the driver and
   *          the browser
   */
  public OperaDriver(Capabilities c) {
    capabilities = (DesiredCapabilities) getDefaultCapabilities();

    if (c != null) {
      capabilities.merge(CapabilitiesSanitizer.sanitize(c));
    }

    // Set the logging level for main logger instance
    Level logLevel;
    Object typelessLevel = capabilities.getCapability(LOGGING_LEVEL);
    if (typelessLevel instanceof String) {
      logLevel = Level.parse(((String) typelessLevel).toUpperCase());
    } else if (typelessLevel instanceof Level) {
      logLevel = (Level) typelessLevel;
    } else {
      throw new WebDriverException("Unknown logging level: " + typelessLevel.toString());
    }
    Logger root = Logger.getLogger("");
    root.setLevel(logLevel);

    // Write log to file?
    if (capabilities.getCapability(LOGGING_FILE) != null) {
      try {
        logFile = new FileHandler((String) capabilities.getCapability(LOGGING_FILE),
                                  OperaFlags.APPEND_TO_LOGFILE);
        logFile.setFormatter(new SimpleFormatter());
      } catch (IOException e) {
        throw new WebDriverException("Unable to write to file: " + e);
      }
    }

    if (logFile != null) {
      root.addHandler(logFile);
    }

    // Set logging levels on all handlers
    for (Handler h : root.getHandlers()) {
      h.setLevel(logLevel);
    }

    if ((Boolean) capabilities.getCapability(AUTOSTART)) {
      if (((Boolean) capabilities.getCapability(GUESS_BINARY_PATH))
          && capabilities.getCapability(BINARY) == null) {
        capabilities.setCapability(BINARY, OperaPaths.operaPath());
      } else if (capabilities.getCapability(BINARY) == null) {
        // Don't guess, only check environment variable
        String path = System.getenv("OPERA_PATH");
        if (path != null && !path.isEmpty()) {
          capabilities.setCapability(BINARY, path);
        }
      }

      settings = new OperaLauncherRunnerSettings();
      settings.setBinary((String) capabilities.getCapability(BINARY));
      settings.setHost((String) capabilities.getCapability(HOST));
      settings.setPort((Integer) capabilities.getCapability(PORT));
      settings.setLoggingLevel(OperaLauncherRunner.toLauncherLoggingLevel(logLevel));

      OperaArguments arguments = new OperaCoreArguments();
      OperaArguments parsed = OperaArguments.parse((String) capabilities.getCapability(ARGUMENTS));
      arguments.merge(parsed);
      settings.setArguments(arguments);

      String launcher = (String) capabilities.getCapability(LAUNCHER);
      if (launcher != null) {
        settings.setLauncher(launcher);
      }

      if (capabilities.getCapability(PROFILE) instanceof String) {
        settings.setProfile((String) capabilities.getCapability(PROFILE));
      } else if (capabilities.getCapability(PROFILE) instanceof OperaProfile) {
        settings.setProfile((OperaProfile) capabilities.getCapability(PROFILE));
      }

      String backend = (String) capabilities.getCapability(BACKEND);
      if (backend != null && !backend.isEmpty()) {
        settings.setBackend(backend);
      }

      // Synchronize settings for runner and capabilities
      capabilities.merge(settings.toCapabilities());

      if (capabilities.getCapability(BINARY) != null) {
        runner = new OperaLauncherRunner(settings);
      }
    } else {
      // If we're not autostarting then we don't want to randomise the port.
      capabilities.setCapability(PORT, (int) OperaIntervals.SERVER_PORT.getValue());
    }

    logger.config(capabilities.toString());
    start();
  }

  /**
   * Gets the default capabilities of OperaDriver.
   *
   * @return a DesiredCapabilities object with default settings
   */
  protected static Capabilities getDefaultCapabilities() {
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    OperaLauncherRunnerSettings defaultSettings = OperaLauncherRunnerSettings.getDefaultSettings();

    capabilities.setJavascriptEnabled(true);

    capabilities.setCapability(LOGGING_LEVEL, defaultSettings.getLoggingLevel());
    capabilities.setCapability(LOGGING_FILE, (String) null);

    File binary = defaultSettings.getBinary();
    capabilities.setCapability(BINARY, (binary == null) ? (String) null : binary.getPath());
    capabilities.setCapability(ARGUMENTS, defaultSettings.getArguments().toString());

    capabilities.setCapability(HOST, defaultSettings.getHost());
    capabilities.setCapability(PORT, defaultSettings.getPort());

    capabilities.setCapability(LAUNCHER, defaultSettings.getLauncher().getPath());
    capabilities.setCapability(DISPLAY, defaultSettings.getDisplay());
    capabilities.setCapability(PROFILE, new OperaProfile());
    capabilities.setCapability(PRODUCT, defaultSettings.getProduct().toString());

    capabilities.setCapability(AUTOSTART, true);
    capabilities.setCapability(NO_RESTART, false);
    capabilities.setCapability(NO_QUIT, false);
    capabilities.setCapability(GUESS_BINARY_PATH, true);
    capabilities.setCapability(OPERAIDLE, false);

    capabilities.setCapability(BACKEND, defaultSettings.getBackend());

    return capabilities;
  }

  /**
   * Start Opera, and handle any exceptions that might occur.
   */
  private void start() {
    try {
      init();
    } catch (Exception e) {
      quit();
      throw new WebDriverException(e);
    }
  }

  /**
   * Initialise required Scope services.
   */
  protected void init() {
    createScopeServices();

    // Launch Opera if the runner has been setup
    if (runner != null) {
      runner.startOpera();
    }

    services.init();
    debugger = services.getDebugger();
    debugger.setDriver(this);

    windowManager = services.getWindowManager();
    exec = services.getExec();
    coreUtils = services.getCoreUtils();
    actionHandler = new PbActionHandler(services);
    cookieManager = services.getCookieManager();
    // cookieManager.updateCookieSettings();
    preferences = new OperaScopePreferences(services.getPrefs());

    // Get product from Opera
    capabilities.setCapability(PRODUCT, utils().getProduct());
  }

  /**
   * @return a map of service names to the minimum versions we require.
   */
  protected Map<String, String> getServicesList() {
    Map<String, String> versions = new HashMap<String, String>();
    versions.put("ecmascript-debugger", "5.0");
    versions.put("window-manager", "2.0");
    versions.put("exec", "2.0");
    versions.put("core", "1.0");
    versions.put("cookie-manager", "1.0");
    versions.put("prefs", "1.0");
    versions.put("selftest", "1.1");
    return versions;
  }

  /**
   * Set up the Scope connection and thread.
   */
  private void createScopeServices() {
    try {
      Map<String, String> versions = getServicesList();
      boolean manualStart = true;

      if (capabilities.getCapability(BINARY) != null) {
        manualStart = false;
      }

      services =
          new ScopeServices(versions, (Integer) capabilities.getCapability(PORT), manualStart);
      // for profile-specific workarounds inside ScopeServices, WaitState ...
      services.setProduct((String) capabilities.getCapability(PRODUCT));
      services.startStpThread();
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
  }

  public Capabilities getCapabilities() {
    return capabilities;
  }

  public void quit() {
    logger.fine("OperaDriver shutting down");

    // This will only delete the profile directory if we created it
    ((OperaProfile) capabilities.getCapability(PROFILE)).cleanUp();

    // This method can be called from start(), before services are created
    if (services != null) {
      services.shutdown();
    }
    if (runner != null) {
      runner.shutdown();
    }
    if (logFile != null) {
      logFile.close();
    }
  }

  public void get(String url) {
    get(url, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
  }

  public int get(String url, long timeout) {
    if (url == null) {
      throw new NullPointerException("Invalid URL");
    }

    if (services.getConnection() == null) {
      throw new CommunicationException("Unable to open URL because Opera is not connected");
    }

    gc();

    // As this is an artificial page load we release all the held keys. If the user clicks a link,
    // any keys remain held.
    exec.releaseKeys();

    int activeWindowId = windowManager.getActiveWindowId();

    String oldUrl = getCurrentUrl();

    services.captureOperaIdle();
    windowManager.openUrl(activeWindowId, url);

    if (oldUrl == null || (url.replace(oldUrl, "").length() == 0
                           || url.replace(oldUrl, "").charAt(0) != '#')) {
      if (useOperaIdle()) {
        try {
          // Use idle timeout (which is lower) if timeout has not been manually set.
          if (timeout == OperaIntervals.PAGE_LOAD_TIMEOUT.getValue()) {
            timeout = OperaIntervals.OPERA_IDLE_TIMEOUT.getValue();
          }
          services.waitForOperaIdle(timeout);
        } catch (WebDriverException e) {
          // This could for example be a gif animation, preventing idle from being passed. Common
          // case, and should not result in test error.
          logger.warning("idle: Timed out with exception: " + e);
        }
      } else {
        try {
          services.waitForWindowLoaded(activeWindowId, timeout);
        } catch (ResponseNotReceivedException e) {
          // This might be expected
          logger.fine("Response not received, returning control to user");
        }
      }
    }

    if (OperaFlags.ENABLE_DEBUGGER) {
      switchTo().defaultContent();
    }

    return windowManager.getLastHttpResponseCode().getAndSet(0);
  }

  public String getCurrentUrl() {
    return debugger.executeJavascript("return document.location.href");
  }

  public void close() {
    closeWindow();
    if (windowManager.getOpenWindowCount() == 0) {
      quit();
    } else {
      windowManager.filterActiveWindow();
    }
  }

  public WebElement findElement(By by) {
    return by.findElement(this);
  }

  protected WebElement findElement(String by, String using) {
    return findElement(by, using, null);
  }

  /**
   * Find a single element using the selenium atoms.
   *
   * @param by    how to find the element, strings defined in RemoteWebDriver
   * @param using the value to use to find the element
   * @param el    the element to search within
   * @return an element
   */
  protected WebElement findElement(String by, String using, OperaWebElement el) {
    if (using == null) {
      throw new IllegalArgumentException("Cannot find elements when the selector is null");
    }

    using = OperaStrings.escapeJsString(using);

    long start = System.currentTimeMillis();
    boolean isAvailable;
    Integer id;

    String script;
    if (el == null) {
      // Search the document
      script =
          "return " + OperaAtoms.FIND_ELEMENT + "({\"" + by + "\": \"" + using + "\"})";
    } else {
      // Search within an element
      script =
          "return " + OperaAtoms.FIND_ELEMENT + "({\"" + by + "\": \"" + using
          + "\"}, locator)";
    }

    do {
      if (el == null) {
        id = debugger.getObject(script);
      } else {
        id = debugger.executeScriptOnObject(script, el.getObjectId());
      }

      isAvailable = (id != null);

      if (!isAvailable) {
        sleep(OperaIntervals.EXEC_SLEEP.getValue());
      }
    } while (!isAvailable && hasTimeRemaining(start));

    if (isAvailable) {
      String error =
          debugger.callFunctionOnObject("return (locator instanceof Error) ? locator.message : ''",
                                        id);
      if (!error.isEmpty()) {
        throw new InvalidSelectorException(error);
      }

      Boolean isStale =
          Boolean.valueOf(debugger.callFunctionOnObject("locator.parentNode == undefined", id));

      if (isStale) {
        throw new StaleElementReferenceException("This element is no longer part of DOM");
      }

      return new OperaWebElement(this, id);
    } else {
      throw new NoSuchElementException("Cannot find element(s) with " + by);
    }
  }

  public List<WebElement> findElements(By by) {
    return by.findElements(this);
  }

  protected List<WebElement> findElements(String by, String using) {
    return findElements(by, using, null);
  }

  protected List<WebElement> findElements(String by, String using, OperaWebElement el) {
    if (using == null) {
      throw new IllegalArgumentException("Cannot find elements when the selector is null");
    }

    using = OperaStrings.escapeJsString(using);

    Integer id;

    long start = System.currentTimeMillis();
    int count = 0;

    List<WebElement> elements;

    String script;
    if (el == null) {
      // Search in document
      script =
          "return " + OperaAtoms.FIND_ELEMENTS + "({\"" + by + "\": \"" + using + "\"})";
    } else {
      // Search within an element
      script =
          "return " + OperaAtoms.FIND_ELEMENTS + "({\"" + by + "\": \"" + using
          + "\"}, locator)";
    }

    do {
      if (el == null) {
        id = debugger.getObject(script);
      } else {
        id = debugger.executeScriptOnObject(script, el.getObjectId());
      }

      String error =
          debugger.callFunctionOnObject("return (locator instanceof Error) ? locator.message : ''",
                                        id);
      if (!error.isEmpty()) {
        throw new InvalidSelectorException(error);
      }

      elements = processElements(id);

      if (elements != null) {
        count = elements.size();
      }

      if (count == 0 && hasTimeRemaining(start)) {
        sleep(OperaIntervals.EXEC_SLEEP.getValue());
      } else {
        break;
      }

    } while (true);

    if (id != null) {
      return elements;
    } else {
      throw new NoSuchElementException("Cannot find element(s) with " + by);
    }
  }

  /**
   * Get the source of the last loaded page. The source will be of the modified DOM, <em>not</em>
   * the original HTML. The page source returned is a representation of the underlying DOM: do not
   * expect it to be formatted or escaped in the same way as the response sent from the web server.
   *
   * @return source code of document
   */
  public String getPageSource() {
    return debugger
        .executeJavascript(
            "return document.documentElement.outerHTML || (typeof window.XMLSerializer != 'undefined') ? (new window.XMLSerializer()).serializeToString(document) : ''");
  }

  public String getTitle() {
    return debugger.executeJavascript("return top.document.title ? top.document.title : '';");
  }

  public String getWindowHandle() {
    return getWindowHandle(null);
  }

  public Set<String> getWindowHandles() {

    List<Integer> windowIds = windowManager.getWindowHandles();
    Set<String> handles = new TreeSet<String>();

    if (!OperaFlags.ENABLE_DEBUGGER) {
      for (Integer windowId : windowIds) {
        handles.add(windowId.toString());
      }
      return handles;
    }

    windowManager.clearFilter();

    for (Integer windowId : windowIds) {
      String handleName = getWindowHandle(windowId);
      handles.add(handleName);
    }

    windowManager.filterActiveWindow();
    debugger.resetRuntimesList();
    return handles;
  }

  private String getWindowHandle(Integer windowId) {
    String windowName;

    String script = "return top.window.name;";

    if (windowId == null) {
      windowName = debugger.executeJavascript(script);
    } else {
      windowName = debugger.executeJavascript(script, windowId);
    }
    if (windowName.isEmpty()) {
      windowName = "operadriver-window" + (assignedWindowIds++);
      script = "top.window.name = '" + windowName + "';";

      if (windowId == null) {
        windowName = debugger.executeJavascript(script);
      } else {
        windowName = debugger.executeJavascript(script, windowId);
      }
    }

    return windowName;
  }

  public int getWindowCount() {
    return windowManager.getWindowHandles().size();
  }

  public TargetLocator switchTo() {
    return new OperaTargetLocator();
  }

  private class OperaTargetLocator implements TargetLocator {

    public WebElement activeElement() {
      return OperaDriver.this.findActiveElement();
    }

    public WebDriver defaultContent() {
      // change to _top
      windowManager.filterActiveWindow();
      debugger.resetFramePath();

      return OperaDriver.this;
    }

    public WebDriver frame(int frameIndex) {
      int framesLength =
          Integer.valueOf(debugger.executeJavascript("return document.frames.length"));

      if (frameIndex < 0 || frameIndex >= framesLength) {
        throw new NoSuchFrameException("Invalid frame index: " + frameIndex);
      }

      debugger.changeRuntime(frameIndex);

      return OperaDriver.this;
    }

    public WebDriver frame(String frameName) {
      debugger.changeRuntime(frameName);
      return OperaDriver.this;
    }

    public WebDriver window(String windowName) {
      windowManager.clearFilter();

      List<Integer> windowIds = windowManager.getWindowHandles();
      Integer id = 0;

      for (Integer windowId : windowIds) {
        String name =
            debugger.executeJavascript("return top.window.name || top.document.title || null",
                                       windowId);

        if (name != null && name.equals(windowName)) {
          id = windowId;
          break;
        }
      }

      if (id == 0) {
        throw new NoSuchWindowException("Window with name " + windowName + " not found");
      }

      windowManager.setActiveWindowId(id);

      windowManager.filterActiveWindow();
      debugger.resetRuntimesList();

      defaultContent(); // set runtime to _top
      debugger.executeJavascript("window.focus()", false); // steal focus

      return OperaDriver.this;
    }

    // TODO: Implement need to find a way to link an element to a runtime
    public WebDriver frame(WebElement frameElement) {
      String script = "return " + OperaAtoms.GET_FRAME_INDEX + "(locator)";
      // PageObject fields are wrapped by PageFactory
      while (frameElement instanceof WrapsElement) {
        frameElement = ((WrapsElement) frameElement).getWrappedElement();
      }
      Long frameIndex =
          (Long) debugger.callFunctionOnObject(script,
                                               ((OperaWebElement) frameElement).getObjectId(),
                                               true);

      if (frameIndex == null) {
        throw new NoSuchFrameException("Non-frame element or frame not in current DOM");
      }

      debugger.changeRuntime(frameIndex.intValue());

      return OperaDriver.this;
    }

    /*
     * This and more functionality will be provided natively in near future, in progress.
     */
    public Alert alert() {
      throw new UnsupportedOperationException();
    }

  }

  public WebElement findElementByName(String using) {
    return findSingleElement(
        "document.getElementsByName('" + OperaStrings.escapeJsString(using, "'") + "')[0];",
        "name");
  }

  public List<WebElement> findElementsByName(String using) {
    return findMultipleElements(
        "document.getElementsByName('" + OperaStrings.escapeJsString(using, "'") + "');", "name");
  }

  public Navigation navigate() {
    return new OperaNavigation();
  }

  private class OperaNavigation implements Navigation {

    public void back() {
      services.captureOperaIdle();
      exec.action("Back");
      try {
        waitForLoadToComplete();
      } catch (ResponseNotReceivedException e) {
        logger.fine("Response not received, returning control to user");
      }
    }

    public void forward() {
      services.captureOperaIdle();
      exec.action("Forward");
      try {
        waitForLoadToComplete();
      } catch (ResponseNotReceivedException e) {
        logger.fine("Response not received, returning control to user");
      }
    }

    public void to(String url) {
      get(url);
    }

    public void to(URL url) {
      get(String.valueOf(url));
    }

    public void refresh() {
      services.captureOperaIdle();
      exec.action("Reload");
      try {
        waitForLoadToComplete();
      } catch (ResponseNotReceivedException e) {
        logger.fine("Response not received, returning control to user");
      }
    }

  }

  public OperaOptions manage() {
    return new OperaOptions();
  }

  public class OperaOptions implements Options {

    public void addCookie(Cookie cookie) {

      // TODO: Numeric overflow

      if (cookie.getExpiry() == null) {
        cookie =
            new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(),
                       new Date(new Date().getTime() + (10 * 365 * 24 * 60 * 60 * 1000)), false);
      }

      debugger.executeJavascript("document.cookie='" + cookie.toString() + "'", false);
    }

    public void deleteCookieNamed(String name) {
      Cookie cookie = getCookieNamed(name);
      deleteCookie(cookie);
    }

    public void deleteCookie(Cookie cookie) {
      if (cookieManager == null) {
        throw new UnsupportedOperationException(
            "Deleting cookies are not supported without the cookie-manager service");
      }

      cookieManager.removeCookie(cookie.getDomain(), cookie.getPath(), cookie.getName());

      gc();

      /*
       * Date dateInPast = new Date(0); Cookie toDelete = new Cookie(cookie.getName(),
       * cookie.getValue(), cookie.getDomain(), cookie.getPath(), dateInPast, false);
       * addCookie(toDelete);
       */
    }

    public void deleteAllCookies() {
      if (cookieManager == null) {
        throw new UnsupportedOperationException(
            "Deleting cookies are not supported without the cookie-manager service");
      }

      cookieManager.removeAllCookies();

      /*
       * Set<Cookie> cookies = getCookies(); for (Cookie cookie : cookies) { deleteCookie(cookie); }
       */
    }

    public Set<Cookie> getCookies() {
      if (cookieManager == null) {
        throw new UnsupportedOperationException(
            "Setting cookies are not supported without the cookie-manager service");
      }

      return cookieManager.getCookie(debugger.executeJavascript("window.location.hostname"), null);
    }

    public Cookie getCookieNamed(String name) {
      Set<Cookie> allCookies = getCookies();

      for (Cookie cookie : allCookies) {
        if (cookie.getName().equals(name)) {
          return cookie;
        }
      }

      return null;

      /*
       * String value = debugger.executeJavascript("var getCookieNamed = function(key)\n"+ "{"+
       * "var value = new RegExp(key + \"=([^;]*)\").exec(document.cookie);"+
       * "return value && decodeURIComponent(value[1]);"+ "}\n"+ "return getCookieNamed('" + name +
       * "')"); return (value == null) ? null : new Cookie(name, value);
       */
    }

    public OperaTimeouts timeouts() {
      return new OperaTimeouts();
    }

    public ImeHandler ime() {
      throw new UnsupportedOperationException("Not supported in OperaDriver yet");
    }

    @Beta
    public Window window() {
      throw new UnsupportedOperationException("Not supported in OperaDriver yet");
    }

    @Beta
    public Logs logs() {
      return new RemoteLogs(getExecuteMethod());
    }

  }

  public class OperaTimeouts implements Timeouts {

    public Timeouts implicitlyWait(long time, TimeUnit unit) {
      OperaIntervals.WAIT_FOR_ELEMENT.setValue(TimeUnit.MILLISECONDS.convert(time, unit));
      return this;
    }

    public Timeouts setScriptTimeout(long time, TimeUnit unit) {
      OperaIntervals.SCRIPT_TIMEOUT.setValue(TimeUnit.MILLISECONDS.convert(time, unit));
      return this;
    }

    public Timeouts pageLoadTimeout(long time, TimeUnit unit) {
      OperaIntervals.PAGE_LOAD_TIMEOUT.setValue(TimeUnit.MILLISECONDS.convert(time, unit));
      return this;
    }

  }

  // TODO: CORE-39436 areas outside of the current viewport is black, this is a problem with Opera not OperaDriver
  public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
    OperaWebElement body = (OperaWebElement) findElementByTagName("body");
    return target.convertFromPngBytes(body.saveScreenshot(0).getPng());
  }

  public Object executeScript(String script, Object... args) {
    Object object = debugger.scriptExecutor(script, args);

    // We probably have an element _or_ a list.
    if (object instanceof ScriptResult) {
      ScriptResult result = (ScriptResult) object;
      Integer objectId = result.getObjectId();

      if (objectId == null) {
        return null;
      }
      if (result.getClassName().endsWith("Element")) {
        return new OperaWebElement(this, objectId);
      }
      if (result.getClassName().equals("NodeList")) {
        return processElements(objectId);
      }
      if (result.getClassName().equals("Array") || result.getClassName().equals("Object")) {
        return debugger.examineScriptResult(objectId);
      }
    }

    return object;
  }

  public Object executeAsyncScript(String script, Object... args) {
    throw new UnsupportedOperationException();
  }

  public Keyboard getKeyboard() {
    return new OperaKeyboard(this);
  }

  public Mouse getMouse() {
    return new OperaMouse(this);
  }

  // Following methods are Opera-specific extensions to the WebDriver interface:

  /**
   * Gets a list of frames.
   *
   * @return list of frames
   */
  public List<String> listFrames() {
    return debugger.listFramePaths();
  }

  /**
   * Takes a screenshot of the whole screen, including areas outside of the Opera browser window.
   *
   * @param timeout the number of milliseconds to wait before taking the screenshot
   * @param hashes  A previous screenshot MD5 hash. If it matches the hash of this screenshot then
   *                no image data is returned.
   * @return a ScreenShotReply object
   */
  public ScreenShotReply saveScreenshot(long timeout, String... hashes) {
    return runner.saveScreenshot(timeout, hashes);
  }

  /**
   * Returns the version number of driver.
   *
   * @return version number
   */
  public String getVersion() {
    if (version == null) {
      URL res = OperaDriver.class.getClassLoader().getResource("VERSION");

      try {
        version = Resources.toString(res, Charsets.UTF_8);
      } catch (Exception e) {
        version = "(Unknown)";
      }
    }
    return version;
  }

  /**
   * Returns an interface for manipulating the preferences in the currently attached Opera
   * programmatically.  Some changes might require Opera to restart before the changes take affect.
   * The available preferences are found in <code>opera:config</code>.
   *
   * @return methods for interacting with preferences
   */
  public OperaScopePreferences preferences() {
    return preferences;
  }

  /**
   * Sets the given preference information.  If a previous preference with the same section and key
   * names exist, it will be replaced by the given preference.
   *
   * @param section the section name, can be case-insensitive
   * @param key     the key name, can be case-insensitive
   * @param value   the new value (will be treated as a {@link String} in Opera
   * @deprecated Please use {@link OperaDriver#preferences()} instead
   */
  @Deprecated
  public void setPref(String section, String key, String value) {
    preferences().set(section, key, value);
  }

  /**
   * Returns the preference with the given section and key values.
   *
   * @param section the section name, can be case-insensitive
   * @param key     the key name, can be case-insensitive
   * @return the preference requested
   * @deprecated Please use {@link OperaDriver#preferences()} instead
   */
  @Deprecated
  public String getPref(String section, String key) {
    return preferences().get(section, key).toString();
  }

  /**
   * Gets the {@link OperaDriver.OperaUtils} interface which is used for accessing the browser's
   * meta- and utility information, such as the operating system it's running on, its user agent
   * string, &c.
   *
   * @return utility methods for Opera
   */
  public OperaUtils utils() {
    return new OperaUtils();
  }

  /**
   * Interface for accessing the browser's meta- and utility information.
   */
  public class OperaUtils {

    /**
     * Which Core version this instance of the browser is using, e.g. "2.8.119".
     *
     * @return version number
     */
    public String getCoreVersion() {
      return coreUtils.getCoreVersion();
    }

    /**
     * A string which describes the operating system, e.g. "Windows NT 6.1".
     *
     * @return operating system identifier
     */
    // TODO(andreastt): Use Platform
    public String getOS() {
      return coreUtils.getOperatingSystem();
    }

    /**
     * Gets the current product.  For regular desktop builds this will be {@link
     * OperaProduct#DESKTOP} Other examples are {@link OperaProduct#MOBILE} and {@link
     * OperaProduct#CORE}.
     *
     * @return browser's product type
     */
    public OperaProduct getProduct() {
      return OperaProduct.get(coreUtils.getProduct());
    }

    /**
     * The full path to the currently running binary.
     *
     * @return full path to browser
     */
    public String getBinaryPath() {
      return coreUtils.getBinaryPath();
    }

    /**
     * The User-Agent string.  Typically something like <code>Opera/9.80 (Windows NT 6.1; U; en)
     * Presto/2.7.62 Version/11.01</code>.
     *
     * @return User-Agent string
     */
    public String getUserAgent() {
      return coreUtils.getUserAgent();
    }

    /**
     * The ID of the process we're currently talking to.  Might not be present if the build does not
     * support retrieving process IDs.
     *
     * @return process ID, or null if not available
     */
    public int getPID() {
      return coreUtils.getProcessID();
    }

  }

  /**
   * Executes selftests for the given module.
   *
   * @param modules the list of modules to run selftests for
   * @param timeout the time out before aborting the operation
   * @return results of the selftests
   */
  public String selftest(List<String> modules, long timeout) {
    return services.selftest(modules, timeout);
  }

  /**
   * Performs a special action, such as setting an Opera preference.
   *
   * For a list of actions call {@link #getOperaActionList()}.
   *
   * @param using  the action to perform.
   * @param params parameters to pass to the action call
   * @deprecated
   */
  @Deprecated
  public void operaAction(String using, String... params) {
    exec.action(using, params);
  }

  @Deprecated
  public Set<String> getOperaActionList() {
    return exec.getActionList();
  }

  // Following methods are used in OperaWebElement:

  protected IEcmaScriptDebugger getScriptDebugger() {
    return debugger;
  }

  protected IOperaExec getExecService() {
    return exec;
  }

  protected ScopeServices getScopeServices() {
    return services;
  }

  protected boolean hasTimeRemaining(long start) {
    return System.currentTimeMillis() - start < OperaIntervals.WAIT_FOR_ELEMENT.getValue();
  }

  protected List<WebElement> processElements(Integer id) {
    List<Integer> ids = debugger.examineObjects(id);
    List<WebElement> toReturn = new ArrayList<WebElement>();
    for (Integer objectId : ids) {
      toReturn.add(new OperaWebElement(this, objectId));
    }
    return toReturn;
  }

  protected void waitForLoadToComplete() throws ResponseNotReceivedException {
    if (useOperaIdle()) {
      services.waitForOperaIdle(OperaIntervals.OPERA_IDLE_TIMEOUT.getValue());
    } else {
      // Sometimes we get here before the next page has even *started* loading, and so return too
      // quickly. This sleep is enough to make sure readyState has been set to "loading".
      sleep(5);

      long endTime = System.currentTimeMillis() + OperaIntervals.PAGE_LOAD_TIMEOUT.getValue();

      while (!"complete".equals(debugger.executeJavascript("return document.readyState"))) {
        if (System.currentTimeMillis() < endTime) {
          sleep(OperaIntervals.POLL_INTERVAL.getValue());
        } else {
          throw new ResponseNotReceivedException("No response in a timely fashion");
        }
      }
    }
  }

  /**
   * Whether idle functionality is available.  Note that this is not the same as whether the idle
   * functionality is enabled.
   *
   * @return true if idle is available, false otherwise
   */
  protected boolean isOperaIdleAvailable() {
    return services.isOperaIdleAvailable();
  }

  // Following methods are used in SpartanRunner:

  /**
   * Enable or disable idle functionality during runtime.
   *
   * @param useIdle true if idle should be switched on, false if it should be switched off
   */
  protected void setUseOperaIdle(boolean useIdle) {
    capabilities.setCapability(OPERAIDLE, useIdle);
  }

  // Following methods are used internally:

  private void gc() {
    debugger.releaseObjects();
    objectIds.clear();
  }

  private void closeWindow() {
    windowManager.closeWindow(windowManager.getActiveWindowId());
  }

  private WebElement findActiveElement() {
    return findSingleElement("document.activeElement;", "active element");
  }

  private List<WebElement> findMultipleElements(String script, String type) {
    Integer id;

    long start = System.currentTimeMillis();
    int count = 0;

    List<WebElement> elements;

    do {
      id = debugger.getObject(script);
      elements = processElements(id);

      if (elements != null) {
        count = elements.size();
      }

      if (count == 0 && hasTimeRemaining(start)) {
        sleep(OperaIntervals.EXEC_SLEEP.getValue());
      } else {
        break;
      }

    } while (true);

    if (id != null) {
      return elements;
    } else {
      throw new NoSuchElementException("Cannot find element(s) with " + type);
    }
  }

  private WebElement findSingleElement(String script, String type) {
    long start = System.currentTimeMillis();
    boolean isAvailable;
    Integer id;

    do {
      id = debugger.getObject(script);
      isAvailable = (id != null);

      if (!isAvailable) {
        sleep(OperaIntervals.EXEC_SLEEP.getValue());
      }
    } while (!isAvailable && hasTimeRemaining(start));

    if (isAvailable) {
      Boolean isStale =
          Boolean.valueOf(debugger.callFunctionOnObject("locator.parentNode == undefined", id));

      if (isStale) {
        throw new StaleElementReferenceException("This element is no longer part of DOM");
      }

      return new OperaWebElement(this, id);
    } else {
      throw new NoSuchElementException("Cannot find element(s) with " + type);
    }
  }

  private boolean useOperaIdle() {
    return (((Boolean) capabilities.getCapability(OPERAIDLE)) && isOperaIdleAvailable());
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}