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

import com.opera.core.systems.interaction.OperaAction;
import com.opera.core.systems.interaction.UserInteraction;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.handlers.PbActionHandler;
import com.opera.core.systems.scope.internal.OperaFlags;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.OperaKeys;
import com.opera.core.systems.scope.protos.PrefsProtos.GetPrefArg.Mode;
import com.opera.core.systems.scope.protos.PrefsProtos.Pref;
import com.opera.core.systems.scope.services.ICookieManager;
import com.opera.core.systems.scope.services.ICoreUtils;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IPrefs;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.settings.OperaDriverSettings;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Alert;
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
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OperaDriver extends RemoteWebDriver implements TakesScreenshot {

  // Want to thin some of these out, but will need some re-thinking.

  /**
   * (String) How verbose the logging should be.  Available levels are: SEVERE (highest value),
   * WARNING, INFO, CONFIG, FINE, FINER, FINEST (lowest value), ALL.  Default is INFO.
   */
  public static final String LOGGING_LEVEL = "opera.logging.level";

  /**
   * (String) Where to send the output of the logging.  Default is to not write to file.
   */
  public static final String LOGGING_FILE = "opera.logging.file";

  /**
   * (String) Path to the Opera binary to use.
   */
  public static final String BINARY = "opera.binary";

  /**
   * (String) Path to the launcher binary to use.
   */
  public static final String LAUNCHER = "opera.launcher";

  /**
   * (String) Arguments to pass to Opera, separated by spaces.
   */
  public static final String ARGUMENTS = "opera.arguments";

  /**
   * (Boolean) Whether to use Opera idle.
   */
  public static final String USE_OPERAIDLE = "opera.use_operaidle";

  /**
   * (Integer) The X display to use.  (Only works on *nix OSes.)
   */
  public static final String DISPLAY = "opera.display";

  /**
   * (Boolean) Whether to auto-start Opera.
   */
  public static final String AUTOSTART = "opera.autostart";

  /**
   * (Boolean) Whether to restart.
   */
  public static final String NO_RESTART = "opera.no_restart";

  /**
   * (Boolean) Whether to quit Opera when OperaDriver is shut down.
   */
  public static final String NO_QUIT = "opera.no_quit";

  /**
   * (Boolean) Whether to guess the path to Opera if it isn't set in <code>opera.binary</code>.
   */
  public static final String GUESS_BINARY_PATH = "opera.guess_binary_path";

  /**
   * (String) The profile configuration we are using, for example "desktop" or "core-gogi".
   */
  public static final String BINARY_PROFILE = "opera.binary_profile";

  /*
   * These are "protected" and not "private" so that we can extend this class
   * and add methods to access these variable in tests.
   */
  protected DesiredCapabilities capabilities;
  protected OperaRunner operaRunner;

  protected IEcmaScriptDebugger debugger;
  protected IOperaExec exec;
  protected IPrefs prefs;
  protected IWindowManager windowManager;
  protected ICoreUtils coreUtils;
  protected ICookieManager cookieManager;

  protected ScopeServices services;
  protected ScopeActions actionHandler;

  protected final Logger logger = Logger.getLogger(this.getClass().getName());
  private FileHandler logFile = null;

  protected Set<Integer> objectIds = new HashSet<Integer>();
  private String version;

  /**
   * Constructor that starts Opera with the default set of capabilities.
   */
  public OperaDriver() {
    this((DesiredCapabilities) null);
  }

  @Deprecated
  public OperaDriver(OperaDriverSettings settings) {
    this(settings.getCapabilities());
  }

  /**
   * Constructor that starts Opera with a set of desired capabilities.
   *
   * @param c a DesiredCapabilities object containing various settings for the driver and the
   *          browser.
   */
  public OperaDriver(DesiredCapabilities c) {
    capabilities = getDefaultCapabilities();

    if (c != null) {
      capabilities.merge(c);
    }

    // Set the logging level for main logger instance
    Level
        logLevel =
        Level.parse(((String) capabilities.getCapability(LOGGING_LEVEL)).toUpperCase());
    Logger root = Logger.getLogger("");
    root.setLevel(logLevel);

    // Write log to file?
    if (capabilities.getCapability(LOGGING_FILE) != null) {
      try {
        logFile =
            new FileHandler((String) capabilities.getCapability(LOGGING_FILE),
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
      OperaPaths paths = new OperaPaths();

      if (((Boolean) capabilities.getCapability(GUESS_BINARY_PATH)) &&
          capabilities.getCapability(BINARY) == null) {
        capabilities.setCapability(BINARY, paths.operaPath());
      } else if (capabilities.getCapability(BINARY) == null) {
        // Don't guess, only check environment variable
        String path = System.getenv("OPERA_PATH");

        if (path != null && path.length() > 0) {
          capabilities.setCapability(BINARY, path);
        }
      }

      if (capabilities.getCapability(LAUNCHER) == null) {
        capabilities.setCapability(LAUNCHER, paths.launcherPath());
      }

      if (capabilities.getCapability(BINARY) != null) {
        this.operaRunner = new OperaLauncherRunner(capabilities);
      }
    }

    start();
  }

  /**
   * Gets the default capabilities of OperaDriver.
   *
   * @return a DesiredCapabilities object with default settings
   */
  // TODO: Make this private when not needed in OperaDriverSettings
  public static DesiredCapabilities getDefaultCapabilities() {
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setJavascriptEnabled(true);

    capabilities.setCapability(LOGGING_LEVEL, "INFO");
    capabilities.setCapability(LOGGING_FILE, (String) null);

    capabilities.setCapability(BINARY, (String) null);
    capabilities.setCapability(ARGUMENTS, "");

    capabilities.setCapability(LAUNCHER, (String) null);

    capabilities.setCapability(DISPLAY, (Integer) null);

    capabilities.setCapability(AUTOSTART, true);
    capabilities.setCapability(NO_RESTART, false);
    capabilities.setCapability(NO_QUIT, false);
    capabilities.setCapability(GUESS_BINARY_PATH, true);

    capabilities.setCapability(USE_OPERAIDLE, false);

    return capabilities;
  }

  private void start() {
    try {
      this.init();
    } catch (Exception e) {
      // Will make sure to kill any eventual launcher process that was started.
      this.quit();
      throw new WebDriverException(e);
    }

    logger.fine("init() done");
  }

  @Deprecated
  public void shutdown() {
    quit();
  }

  public void quit() {
    logger.fine("Opera Driver shutting down");
    services.quit();
    if (operaRunner != null) {
      operaRunner.shutdown();
    }
    if (logFile != null) {
      logFile.close();
    }
  }

  /**
   * For testing override this method.
   */
  protected void init() {
    createScopeServices();

    // Launch Opera if the runner has been setup
    if (operaRunner != null) {
      operaRunner.startOpera();
    }

    services.init();
    debugger = services.getDebugger();
    debugger.setDriver(this);

    windowManager = services.getWindowManager();
    exec = services.getExec();
    coreUtils = services.getCoreUtils();
    actionHandler = new PbActionHandler(services);
    cookieManager = services.getCookieManager();
    //cookieManager.updateCookieSettings();
    prefs = services.getPrefs();
  }

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

  private void createScopeServices() {
    try {
      Map<String, String> versions = getServicesList();
      boolean manualStart = true;

      if (capabilities.getCapability(BINARY) != null) {
        manualStart = false;
      }

      services = new ScopeServices(versions, manualStart);
      // for profile-specific workarounds inside ScopeServives, WaitState ...
      services.setProfile((String) capabilities.getCapability(BINARY_PROFILE));
      services.startStpThread();
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
  }

  public Capabilities getCapabilities() {
    return capabilities;
  }

  public void get(String url) {
    get(url, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
  }

  public int get(String url, long timeout) {
    if (url == null) {
      throw new NullPointerException("Invalid url");
    }

    if (services.getConnection() == null) {
      throw new CommunicationException("Unable to open URL because Opera is not connected.");
    }

    gc();
    // As this is an artificial page load we release all the held keys.
    // Is the user click()s a link, any keys remain held.
    exec.releaseKeys();

    int activeWindowId = windowManager.getActiveWindowId();

    String oldUrl = getCurrentUrl();

    services.captureOperaIdle();
    windowManager.openUrl(activeWindowId, url);

    if (oldUrl == null || !url.replace(oldUrl, "").startsWith("#")) {

      if (useOperaIdle()) {
        try {
          // Wait for Opera to become idle
          if (timeout == OperaIntervals.PAGE_LOAD_TIMEOUT.getValue()) {
            timeout = OperaIntervals.OPERA_IDLE_TIMEOUT.getValue();
          }
          services.waitForOperaIdle(timeout);
        } catch (WebDriverException e) {
          /*
           * This could for example be a gif animation, preventing idle from
           * being passed.  Common case, and should not result in test error.
           */
          logger.warning("Opera Idle timed out, continue test... exception: " + e);
        }
      } else {
        // Wait for window is loaded
        services.waitForWindowLoaded(activeWindowId, timeout);
      }

    }

    if (OperaIntervals.ENABLE_DEBUGGER.getValue() == 1) {
      switchTo().defaultContent();
    }

    return windowManager.getLastHttpResponseCode().getAndSet(0);
  }

  public String getCurrentUrl() {
    return debugger.executeJavascript("return document.location.href");
  }

  private void gc() {
    debugger.releaseObjects();
    objectIds.clear();
  }

  public void close() {
    closeWindow();
    windowManager.filterActiveWindow();
  }

  /**
   * Closes all open windows.
   *
   * @deprecated Use <code>for (String win : driver.getWindowHandles()) { if
   *             (driver.getWindowCount() <= 1) break; driver.switchTo().window(win).close();
   *             }</code> on Desktop.
   */
  @Deprecated
  public void closeAll() {
    windowManager.closeAllWindows();
    windowManager.filterActiveWindow();
  }

  private void closeWindow() {
    windowManager.closeWindow(windowManager.getActiveWindowId());
  }

  /**
   * Stops the loading of the current page.
   *
   * @deprecated Use {@link #navigate()}.stop()
   */
  @Deprecated
  public void stop() {
    exec.action("Stop");
  }

  /**
   * The by string passed by remote web driver is different to the one used in the atoms. This
   * converts from the "nice" string to the atom string.
   *
   * @param by The "nice" by string for remote web driver.
   * @return The by string used in the atoms.
   */
  private String convertByToAtom(String by) {
    if ("class name".equals(by)) {
      by = "className";
    } else if ("css selector".equals(by)) {
      by = "css";
    } else if ("id".equals(by)) {
      ;
    } else if ("link text".equals(by)) {
      by = "linkText";
    } else if ("partial link text".equals(by)) {
      by = "partialLinkText";
    } else if ("tag name".equals(by)) {
      by = "tagName";
    } else if ("xpath".equals(by)) {
      by = "xpath";
    } else {
      throw new WebDriverException("Cannot find matching element locator to: " + by);
    }

    return by;
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
   * @param by    How to find the element.  Strings defined in RemoteWebDriver and {@link
   *              #convertByToAtom}.
   * @param using The value to use to find the element
   * @param el    The element to search within
   */
  protected WebElement findElement(String by, String using, OperaWebElement el) {
    if (using == null) {
      throw new IllegalArgumentException("Cannot find elements when the selector is null.");
    }

    String niceBy = by;
    by = convertByToAtom(by);
    using = escapeJsString(using);

    long start = System.currentTimeMillis();
    boolean isAvailable;
    Integer id;

    String script;
    if (el == null) {
      // Search the document
      script = "return " + OperaAtoms.FIND_ELEMENT.getValue() + "({" + by + ": \"" + using + "\"})";
    } else {
      // Search within an element
      script =
          "return " + OperaAtoms.FIND_ELEMENT.getValue() + "({" + by + ": \"" + using
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

    OperaIntervals.WAIT_FOR_ELEMENT.setValue(0L);

    if (isAvailable) {
      String
          error =
          debugger
              .callFunctionOnObject("return (locator instanceof Error) ? locator.message : ''", id);
      if (!error.isEmpty()) {
        throw new InvalidSelectorException(error);
      }

      Boolean
          isStale =
          Boolean.valueOf(debugger.callFunctionOnObject("locator.parentNode == undefined", id));

      if (isStale) {
        throw new StaleElementReferenceException("This element is no longer part of DOM");
      }

      return new OperaWebElement(this, id);
    } else {
      throw new NoSuchElementException("Cannot find element(s) with " + niceBy);
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
      throw new IllegalArgumentException("Cannot find elements when the selector is null.");
    }

    String niceBy = by;
    by = convertByToAtom(by);
    using = escapeJsString(using);

    Integer id;

    long start = System.currentTimeMillis();
    int count = 0;

    List<WebElement> elements;

    String script;
    if (el == null) {
      // Search in document
      script =
          "return " + OperaAtoms.FIND_ELEMENTS.getValue() + "({" + by + ": \"" + using + "\"})";
    } else {
      // Search within an element
      script =
          "return " + OperaAtoms.FIND_ELEMENTS.getValue() + "({" + by + ": \"" + using
          + "\"}, locator)";
    }

    do {
      if (el == null) {
        id = debugger.getObject(script);
      } else {
        id = debugger.executeScriptOnObject(script, el.getObjectId());
      }

      String
          error =
          debugger
              .callFunctionOnObject("return (locator instanceof Error) ? locator.message : ''", id);
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

    OperaIntervals.WAIT_FOR_ELEMENT.setValue(0L);

    if (id != null) {
      return elements;
    } else {
      throw new NoSuchElementException("Cannot find element(s) with " + niceBy);
    }
  }

  /**
   * Get the source of the last loaded page. <p/> The source will be of the modified DOM,
   * <em>not</em> the original HTML.  The page source returned is a representation of the underlying
   * DOM: do not expect it to be formatted or escaped in the same way as the response sent from the
   * web server.
   */
  public String getPageSource() {
    return debugger.executeJavascript(
        "return document.documentElement.outerHTML || (typeof window.XMLSerializer != 'undefined') ? (new window.XMLSerializer()).serializeToString(document) : ''"
    );
  }

  public String getTitle() {
    return debugger.executeJavascript("return top.document.title ? top.document.title : '';");
  }

  public String getWindowHandle() {
    return debugger
        .executeJavascript("return top.window.name ? top.window.name : top.document.title;");
  }

  public Set<String> getWindowHandles() {

    List<Integer> windowIds = windowManager.getWindowHandles();
    Set<String> handles = new TreeSet<String>();

    if (OperaIntervals.ENABLE_DEBUGGER.getValue() != 1) {
      for (Integer windowId : windowIds) {
        handles.add(windowId.toString());
      }
      return handles;
    }

    windowManager.clearFilter();

    for (Integer windowId : windowIds) {
      // windowManager.filterWindow(windowId);
      String handleName = debugger.executeJavascript(
          "return top.window.name ? top.window.name : (top.document.title ? top.document.title : 'undefined');",
          windowId
      );
      handles.add(handleName);
    }

    windowManager.filterActiveWindow();
    debugger.resetRuntimesList();
    return handles;
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
      // make sure we execute this one on "_top"
      debugger.resetFramePath();
      int
          framesLength =
          Integer.valueOf(debugger.executeJavascript("return document.frames.length"));

      if (frameIndex < 0 || frameIndex >= framesLength) {
        throw new NoSuchFrameException(
            "Invalid frame index : " + frameIndex);
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
        String name = debugger.executeJavascript(
            "return top.window.name ? top.window.name : top.document.title;",
            windowId
        );

        if (name.equals(windowName)) {
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

      defaultContent();  // set runtime to _top
      debugger.executeJavascript("window.focus()", false);  // steal focus

      return OperaDriver.this;
    }

    // TODO: Implement need to find a way to link an element to a runtime
    public WebDriver frame(WebElement frameElement) {
      throw new UnsupportedOperationException();
    }

    /*
     * This and more functionality will be provided natively in near future, in
     * progress.
     */
    public Alert alert() {
      throw new UnsupportedOperationException();
    }

  }

  /**
   * @return list of frames
   */
  public List<String> listFrames() {
    return debugger.listFramePaths();
  }

  /**
   * Escape characters for safe insertion in a Javascript string contained by double quotes (").
   *
   * @param string the string to escape
   * @return an escaped string
   */
  protected String escapeJsString(String string) {
    return escapeJsString(string, "\"");
  }

  /**
   * Escape characters for safe insertion in a JavaScript string.
   *
   * @param string the string to escape
   * @param quote  the type of quote to escape. Either " or '
   * @return the escaped string
   */
  private String escapeJsString(String string, String quote) {

    /*
     * This should be expanded to match all invalid characters (e.g. newlines)
     * but for the moment we'll trust we'll only get quotes.
     */
    Pattern escapePattern = Pattern.compile("([^\\\\])" + quote);

    /*
     * Prepend a space so that the regex can match quotes at the beginning of
     * the string.
     */
    Matcher m = escapePattern.matcher(" " + string);
    StringBuffer sb = new StringBuffer();

    while (m.find()) {
      /*
       * $1 -> inserts the character before the quote
       * \\\\\" -> \\", apparently just \" isn't treated literally.
       */
      m.appendReplacement(sb, "$1\\\\" + quote);
    }

    m.appendTail(sb);

    // Remove the prepended space.
    return sb.substring(1);
  }

  private WebElement findActiveElement() {
    return findSingleElement("document.activeElement;", "active element");
  }

  protected List<WebElement> processElements(Integer id) {
    List<Integer> ids = debugger.examineObjects(id);
    List<WebElement> toReturn = new ArrayList<WebElement>();
    for (Integer objectId : ids) {
      toReturn.add(new OperaWebElement(this, objectId));
    }
    return toReturn;
  }

  // FIXME when timeout has completed, send 'stop' command?
  public void waitForLoadToComplete() {
    if (useOperaIdle()) {
      // new opera wait for page
      services.waitForOperaIdle(OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
    } else {

      /*
       * Sometimes we get here before the next page has even *started*
       * loading, and so return too quickly. This sleep is enough to make
       * sure readyState has been set to "loading".
       */
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      long endTime = System.currentTimeMillis() + OperaIntervals.PAGE_LOAD_TIMEOUT.getValue();

      while (!"complete".equals(debugger.executeJavascript("return document.readyState"))) {
        if (System.currentTimeMillis() < endTime) {
          sleep(OperaIntervals.POLL_INVERVAL.getValue());
        } else {
          throw new WebDriverException("Timeout while loading page");
        }
      }
    }
  }

  public WebElement findElementByName(String using) {
    return findSingleElement(
        "document.getElementsByName('" + escapeJsString(using, "'") + "')[0];", "name"
    );
  }

  public List<WebElement> findElementsByName(String using) {
    return findMultipleElements(
        "document.getElementsByName('" + escapeJsString(using, "'") + "');", "name"
    );
  }

  public Navigation navigate() {
    return new OperaNavigation();
  }

  private class OperaNavigation implements Navigation {

    public void back() {
      services.captureOperaIdle();
      exec.action("Back");
      waitForLoadToComplete();
    }

    public void forward() {
      services.captureOperaIdle();
      exec.action("Forward");
      waitForLoadToComplete();
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
      waitForLoadToComplete();
    }
  }

  public Options manage() {
    return new OperaOptions();
  }

  private class OperaOptions implements Options {

    public void addCookie(Cookie cookie) {

      // TODO: Numeric overflow

      if (cookie.getExpiry() == null) {
        cookie = new Cookie(
            cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(),
            new Date(new Date().getTime() + (10 * 365 * 24 * 60 * 60 * 1000)), false
        );
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

      cookieManager.removeCookie(cookie.getDomain(), cookie.getPath(),
                                 cookie.getName());

      gc();

      /*
      Date dateInPast = new Date(0);
      Cookie toDelete = new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), dateInPast, false);
      addCookie(toDelete);
      */
    }

    public void deleteAllCookies() {
      if (cookieManager == null) {
        throw new UnsupportedOperationException(
            "Deleting cookies are not supported without the cookie-manager service");
      }

      cookieManager.removeAllCookies();

      /*
      Set<Cookie> cookies = getCookies();
      for (Cookie cookie : cookies) {
        deleteCookie(cookie);
      }
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
      String value = debugger.executeJavascript("var getCookieNamed = function(key)\n"+
                              "{"+
                              "var value = new RegExp(key + \"=([^;]*)\").exec(document.cookie);"+
                              "return value && decodeURIComponent(value[1]);"+
                              "}\n"+
                              "return getCookieNamed('" + name + "')");
      return (value == null) ? null : new Cookie(name, value);
      */
    }

    public Timeouts timeouts() {
      return new OperaTimeouts();
    }

    public ImeHandler ime() {
      // TODO Auto-generated method stub
      return null;
    }

  }

  private class OperaTimeouts implements Timeouts {

    public Timeouts implicitlyWait(long time, TimeUnit unit) {
      OperaIntervals.WAIT_FOR_ELEMENT.setValue(TimeUnit.MILLISECONDS.convert(time, unit));
      return this;
    }

    public Timeouts setScriptTimeout(long time, TimeUnit unit) {
      OperaIntervals.SCRIPT_TIMEOUT.setValue(TimeUnit.MILLISECONDS.convert(time, unit));
      return this;
    }

  }

  /**
   * Performs a special action, such as setting an Opera preference.
   *
   * @param using  The action to perform. For a list of actions call {@link #getOperaActionList()}
   *               at run time
   * @param params Parameters to pass to the action call
   */
  public void operaAction(String using, String... params) {
    exec.action(using, params);
  }

  @SuppressWarnings("unused")
  private Set<String> getOperaActionList() {
    return exec.getActionList();
  }

  private static void sleep(long timeInMillis) {
    try {
      Thread.sleep(timeInMillis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
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

    OperaIntervals.WAIT_FOR_ELEMENT.setValue(0L);

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

    OperaIntervals.WAIT_FOR_ELEMENT.setValue(0L);

    if (isAvailable) {
      Boolean
          isStale =
          Boolean.valueOf(debugger.callFunctionOnObject("locator.parentNode == undefined", id));

      if (isStale) {
        throw new StaleElementReferenceException("This element is no longer part of DOM");
      }

      return new OperaWebElement(this, id);
    } else {
      throw new NoSuchElementException("Cannot find element(s) with " + type);
    }
  }

  boolean hasTimeRemaining(long start) {
    return System.currentTimeMillis() - start < OperaIntervals.WAIT_FOR_ELEMENT.getValue();
  }

  /**
   * Takes a screenshot of the whole screen, including areas outside of the Opera browser window.
   *
   * @param timeout the number of milliseconds to wait before taking the screenshot
   * @param hashes  A previous screenshot MD5 hash.  If it matches the hash of this screenshot then
   *                no image data is returned.
   * @return a ScreenShotReply object
   */
  public ScreenShotReply saveScreenshot(long timeout, String... hashes) {
    return operaRunner.saveScreenshot(timeout, hashes);
  }

  // FIXME: CORE-39436 areas outside of the current viewport are black. This is
  // a problem with Opera, not OperaDriver.
  public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
    OperaWebElement body = (OperaWebElement) findElementByTagName("body");
    return target.convertFromPngBytes(body.saveScreenshot(0).getPng());
  }

  public boolean isOperaIdleAvailable() {
    return services.isOperaIdleAvailable();
  }

  private boolean useOperaIdle() {
    return (((Boolean) capabilities.getCapability(USE_OPERAIDLE)) && isOperaIdleAvailable());
  }

  public void setUseOperaIdle(boolean useIdle) {
    capabilities.setCapability(USE_OPERAIDLE, true);
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
      if (result.getClassName().equals("Array")
          || result.getClassName().equals("Object")) {
        return debugger.examineScriptResult(objectId);
      }
    }

    return object;
  }

  /**
   * Is JavaScript enables in this driver?
   *
   * @return true if JavaScript is enabled
   */
  public boolean isJavascriptEnabled() {
    return (OperaIntervals.ENABLE_DEBUGGER.getValue() == 1);
  }

  /**
   * @param action a string identifying the Opera Action to use.
   * @deprecated
   */
  @Deprecated
  public void executeActions(OperaAction action) {
    services.captureOperaIdle();
    List<UserInteraction> actions = action.getActions();
    for (UserInteraction userInteraction : actions) {
      userInteraction.execute(this);
    }
    waitForLoadToComplete();
  }

  /**
   * Presses and releases the given key.  If the key is "enter" then OperaDriver waits for the page
   * to finish loading.
   *
   * @param key A string containing the key to press. This can be a single character (e.g. "a") or a
   *            special key (e.g. "left"), and is matched case insensitively.  For a list of keys
   *            see {@link OperaKeys}.
   */
  public void key(String key) {
    if (key.equalsIgnoreCase("enter")) {
      services.captureOperaIdle();
    }

    keyDown(key);
    keyUp(key);

    if (key.equalsIgnoreCase("enter")) {
      waitForLoadToComplete();
    }
  }

  /**
   * Presses and holds the given key. You cannot press a key that is already down.
   *
   * @param key the key to press, see {@link #key(String)} for more information.
   */
  public void keyDown(String key) {
    exec.key(key, false);
  }

  /**
   * Releases the given key.
   *
   * @param key the key to release, see {@link #key(String)} for more information.
   */
  public void keyUp(String key) {
    exec.key(key, true);
  }

  /**
   * Releases all the currently pressed keys.
   */
  public void releaseKeys() {
    exec.releaseKeys();
  }

  /**
   * Types the given string as-is in to the browser window.  To press special keys use {@link
   * #key(String)}.
   *
   * @param using the string to type
   */
  public void type(String using) {
    exec.type(using);
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
        InputStream stream = res.openStream();
        version = IOUtils.toString(stream);
        stream.close();
      } catch (Exception e) {
        version = "(Unknown)";
      }
    }
    return version;
  }

  protected IEcmaScriptDebugger getScriptDebugger() {
    return debugger;
  }

  protected IOperaExec getExecService() {
    return exec;
  }

  protected IWindowManager getWindowManager() {
    return windowManager;
  }

  protected ScopeServices getScopeServices() {
    return services;
  }

  /**
   * Get the value of the requested preference.
   *
   * @param section the section the preference is in
   * @param key     the key name of the preference to get
   * @return the value of the preference
   */
  public String getPref(String section, String key) {
    return services.getPrefs().getPref(section, key, Mode.CURRENT);
  }

  /**
   * Gets the default value of the requested preference.
   *
   * @param section the section the preference is in
   * @param key     the key name of the preference
   * @return the default string value of the preference
   */
  public String getDefaultPref(String section, String key) {
    return services.getPrefs().getPref(section, key, Mode.DEFAULT);
  }

  /**
   * Returns a Map of preference names to preferences in the requested section.
   *
   * @param sort    whether to alphabetically sort the preference keys
   * @param section the section to retrieve the preferences from
   * @return a Map of preference names to preferences.
   */
  public Map<String, Pref> listPrefs(boolean sort, String section) {
    Map<String, Pref> map = new HashMap<String, Pref>();

    for (Pref p : prefs.listPrefs(sort, section)) {
      map.put(p.getKey(), p);
    }
    return map;
  }

  /**
   * Returns a Map of sections names mapping to a Map of preference names mapping to Pref objects.
   *
   * @return a map of preference objects
   */
  public Map<String, Map<String, Pref>> listAllPrefs() {
    List<Pref> allPrefs = prefs.listPrefs(true, null);

    HashMap<String, Map<String, Pref>> result = new HashMap<String, Map<String, Pref>>();

    for (Pref pref : allPrefs) {
      String section = pref.getSection();
      String key = pref.getKey();

      if (!result.containsKey(section)) {
        result.put(section, new HashMap<String, Pref>());
      }

      result.get(section).put(key, pref);
    }

    return result;
  }

  /**
   * Set the value of a preference using section and key as locators.
   *
   * @param section the section the preference is in
   * @param key     the key name of the preference to set
   * @param value   the value to set the preference to
   */
  public void setPref(String section, String key, String value) {
    services.getPrefs().setPrefs(section, key, value);
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

  public String selftest(List<String> modules, long timeout) {
    return services.selftest(modules, timeout);
  }

  /**
   * Methods to access Core service 1.2 metadata
   */
  public String getCoreVersion() {
    return coreUtils.getCoreVersion();
  }

  public String getOS() {
    return coreUtils.getOperatingSystem();
  }

  public String getProduct() {
    return coreUtils.getProduct();
  }

  public String getBinaryPath() {
    return coreUtils.getBinaryPath();
  }

  public String getUserAgent() {
    return coreUtils.getUserAgent();
  }

  public int getPID() {
    return coreUtils.getProcessID();
  }

  public OperaRunner getOperaRunner() {
    return operaRunner;
  }
}
