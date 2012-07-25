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

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import com.opera.core.systems.common.io.Closeables;
import com.opera.core.systems.common.lang.OperaStrings;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.preferences.OperaScopePreferences;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.internal.OperaDefaults;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.ICookieManager;
import com.opera.core.systems.scope.services.ICoreUtils;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.scope.services.ums.CoreUtils;

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
import org.openqa.selenium.Platform;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.logging.LocalLogs;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteLogs;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.opera.core.systems.OperaProduct.CORE;
import static com.opera.core.systems.OperaProduct.DESKTOP;
import static com.opera.core.systems.OperaProduct.MOBILE;
import static com.opera.core.systems.scope.services.ums.Selftest.SelftestResult;
import static org.openqa.selenium.Platform.WINDOWS;

/**
 * OperaDriver is a vendor-supported WebDriver implementation developed by Opera and volunteers that
 * enables programmatic automation of different Opera products.  It is a part of the Selenium
 * project.
 *
 * The driver implements the Scope protocol in Java to enable communication with Opera directly from
 * Java.
 */
public class OperaDriver extends RemoteWebDriver implements TakesScreenshot, RunsSelftest {

  /**
   * Different types of data stored by Opera.
   */
  public enum PrivateData {
    ALL,
    VISITED_LINKS,
    DISK_CACHE,
    IMAGE_CACHE,
    MEMORY_CACHE,
    SENSITIVE_DATA,
    SESSION_COOKIES,
    ALL_COOKIES,
    GLOBAL_HISTORY,
    CONSOLE,
    THUMBNAILS,
    WEBDATABASES,
    WEBSTORAGE,
    APPCACHE,
    GEOLOCATION_PERMISSIONS,
    SITE_PREFS
  }

  protected final OperaSettings settings;
  protected OperaRunner runner = null;

  private ScopeServices services;
  private IEcmaScriptDebugger debugger;
  private IOperaExec exec;
  private IWindowManager windowManager;
  private ICoreUtils coreUtils;
  private ICookieManager cookieManager;

  private OperaScopePreferences preferences;

  private OperaMouse mouse;
  private OperaKeyboard keyboard;
  private OperaProxy proxy;

  protected Set<Integer> objectIds = Sets.newHashSet();
  private int assignedWindowIds = 0;

  protected final Logger logger = Logger.getLogger(getClass().getName());
  protected static FileHandler logFile = null;

  /**
   * Constructor that starts Opera with the default set of capabilities.
   */
  public OperaDriver() {
    this(new OperaSettings());
  }

  /**
   * Starts Opera with the given profile.
   *
   * @param profile the profile to start Opera with
   */
  public OperaDriver(OperaProfile profile) {
    this(new OperaSettings.Builder().usingProfile(profile).get());
  }

  /**
   * Starts Opera with the given set of desired capabilities.
   *
   * @param capabilities a {@link DesiredCapabilities} object containing various settings for the
   *                     driver and the browser
   */
  public OperaDriver(Capabilities capabilities) {
    this(new OperaSettings().merge(capabilities));
  }

  /**
   * Starts Opera with the given settings.
   *
   * @param settings Opera specific settings
   */
  public OperaDriver(OperaSettings settings) {
    this.settings = settings;
    logger.config(settings.toString());
    start();
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
   * Initialize required Scope services.
   */
  protected void init() {
    if (settings.autostart()) {
      runner = new OperaLauncherRunner(settings);
    } else {
      settings.setPort(OperaDefaults.SERVER_DEFAULT_PORT_IDENTIFIER);
    }

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
    cookieManager = services.getCookieManager();
    //cookieManager.updateCookieSettings();
    preferences = new OperaScopePreferences(services.getPrefs());

    mouse = new OperaMouse(this);
    keyboard = new OperaKeyboard(this);
    proxy = new OperaProxy(this);

    // Get product from Opera
    settings.setProduct(utils().getProduct());

    // Enable popups for testing purposes
    preferences().set("User Prefs", "Ignore Unrequested Popups", false);

    // Update browser's proxy configuration
    proxy.parse(settings.getProxy());

    // Mobile has disabled JavaScript autofocus for usability reasons
    if (utils().getProduct().is(MOBILE)) {
      preferences().set("User Prefs", "Allow Autofocus Form Element", true);
    }
  }

  /**
   * List of required services for this version of OperaDriver to function.
   *
   * @return a map of service names to the minimum versions we require
   */
  protected Map<String, String> getRequiredServices() {
    ImmutableMap.Builder<String, String> versions = ImmutableMap.builder();
    versions.put("ecmascript-debugger", "5.0");
    versions.put("window-manager", "2.0");
    versions.put("exec", "2.0");
    versions.put("core", "1.0");
    versions.put("cookie-manager", "1.0");
    versions.put("prefs", "1.0");
    versions.put("selftest", "1.1");
    return versions.build();
  }

  /**
   * Set up the Scope connection and thread.
   */
  private void createScopeServices() {
    try {
      services = new ScopeServices(getRequiredServices(),
                                   settings.getPort(),
                                   !settings.autostart());
      services.startStpThread();
    } catch (IOException e) {
      throw new CommunicationException(e);
    }
  }

  public Capabilities getCapabilities() {
    return settings.toCapabilities();
  }

  public void quit() {
    gc();

    try {
      if (!settings.hasDetach()) {
        // Instruct Opera to quit and close the Scope connection.  If not successful, force Opera to
        // stop using the runner if available
        try {
          if (runner != null) {
            services.quit(runner);
          } else {
            services.quit();
          }
        } catch (Throwable t) {
          if (runner != null) {
            runner.stopOpera();
          } else {
            Throwables.propagateIfPossible(t);
          }
        }
      } else {
        // Just close the Scope connection, leave Opera running
        services.shutdown();
      }

      // Shutdown runner if one is present
      if (runner != null) {
        runner.shutdown();
      }
    } catch (RuntimeException e) {
      throw new WebDriverException(e);
    } finally {
      // Don't clean the profile if we were asked to detach the browser
      if (!settings.hasDetach()) {
        settings.profile().cleanUp();
      }
      Closeables.closeQuietly(logFile);
    }
  }

  public void get(String url) {
    get(url, OperaIntervals.PAGE_LOAD_TIMEOUT.getMs());
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

    if (oldUrl == null ||
        (url.replace(oldUrl, "").isEmpty() || url.replace(oldUrl, "").charAt(0) != '#')) {
      if (settings.useIdle() && services.isOperaIdleAvailable()) {
        try {
          // Use idle timeout (which is lower) if timeout has not been manually set.
          if (timeout == OperaIntervals.PAGE_LOAD_TIMEOUT.getMs()) {
            timeout = OperaIntervals.OPERA_IDLE_TIMEOUT.getMs();
          }
          services.waitForOperaIdle(timeout);
        } catch (WebDriverException e) {
          // This could for example be a gif animation, preventing idle from being passed. Common
          // case, and should not result in test error.
          logger.warning("idle: Timed out with exception: " + e.getMessage());
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

    if (OperaDefaults.ENABLE_DEBUGGER) {
      switchTo().defaultContent();
    }

    return windowManager.getLastHttpResponseCode().getAndSet(0);
  }

  public String getCurrentUrl() {
    return debugger.executeJavascript("return document.location.href");
  }

  public void close() {
    if (!getScopeServices().isConnected()) {
      return;
    }

    int windowCountBeforeClose = getWindowCount();
    if (windowCountBeforeClose >= 1) {
      windowManager.closeWindow(windowManager.getActiveWindowId());
    }

    // Gogi and Desktop on Windows does not close its last window, but we need to follow the
    // WebDriver spec
    OperaProduct product = utils().getProduct();

    if (getWindowCount() == 0 ||
        ((product.is(CORE) || (product.is(DESKTOP) && Platform.getCurrent().is(WINDOWS)))
         && windowCountBeforeClose == 1)) {
      quit();
    } else {
      windowManager.filterActiveWindow();
    }
  }

  public WebElement findElement(By by) {
    return by.findElement(this);
  }

  protected WebElement findElement(final String by, final String using) {
    return implicitlyWaitFor(new Callable<WebElement>() {
      public WebElement call() throws Exception {
        return findElement(by, using, null);
      }
    });
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
    checkNotNull(using, "Cannot find elements when the selector is null");

    using = OperaStrings.escapeJsString(using);
    boolean isAvailable;
    Integer id;
    String script;

    if (el == null) {
      // Search the document
      script =
          "return " + OperaAtom.FIND_ELEMENT + "({\"" + by + "\": \"" + using + "\"})";
    } else {
      // Search within an element
      script =
          "return " + OperaAtom.FIND_ELEMENT + "({\"" + by + "\": \"" + using
          + "\"}, locator)";
    }

    if (el == null) {
      id = debugger.getObject(script);
    } else {
      id = debugger.executeScriptOnObject(script, el.getObjectId());
    }

    isAvailable = (id != null);

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
          "return " + OperaAtom.FIND_ELEMENTS + "({\"" + by + "\": \"" + using + "\"})";
    } else {
      // Search within an element
      script =
          "return " + OperaAtom.FIND_ELEMENTS + "({\"" + by + "\": \"" + using
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
        sleep(OperaIntervals.IMPLICIT_WAIT.getMs());
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
    if (!services.isConnected()) {
      return ImmutableSet.of();
    }

    List<Integer> windowIds = windowManager.getWindowHandles();
    Set<String> handles = new TreeSet<String>();

    if (!OperaDefaults.ENABLE_DEBUGGER) {
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
      String script = "return " + OperaAtom.GET_FRAME_INDEX + "(locator)";
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

      //Date dateInPast = new Date(0); Cookie toDelete = new Cookie(cookie.getName(),
      //cookie.getValue(), cookie.getDomain(), cookie.getPath(), dateInPast, false);
      //addCookie(toDelete);
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

      //String value = debugger.executeJavascript("var getCookieNamed = function(key)\n"+ "{"+
      //"var value = new RegExp(key + \"=([^;]*)\").exec(document.cookie);"+
      //"return value && decodeURIComponent(value[1]);"+ "}\n"+ "return getCookieNamed('" + name +
      //"')"); return (value == null) ? null : new Cookie(name, value);
    }

    public OperaTimeouts timeouts() {
      return new OperaTimeouts() {
        public Timeouts implicitlyWait(long time, TimeUnit unit) {
          OperaIntervals.IMPLICIT_WAIT.setValue(new Duration(time, unit));
          return this;
        }

        public Timeouts setScriptTimeout(long time, TimeUnit unit) {
          OperaIntervals.SCRIPT_TIMEOUT.setValue(new Duration(time, unit));
          return this;
        }

        public Timeouts pageLoadTimeout(long time, TimeUnit unit) {
          OperaIntervals.PAGE_LOAD_TIMEOUT.setValue(new Duration(time, unit));
          return this;
        }

        public Timeouts responseTimeout(long time, TimeUnit unit) {
          OperaIntervals.RESPONSE_TIMEOUT.setValue(new Duration(time, unit));
          return this;
        }

        public Timeouts selftestTimeout(long time, TimeUnit unit) {
          OperaIntervals.SELFTEST_TIMEOUT.setValue(new Duration(time, unit));
          return this;
        }
      };
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
      return new RemoteLogs(getExecuteMethod(), LocalLogs.NULL_LOGGER);
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
      } else if (result.getClassName().endsWith("Element")) {
        return new OperaWebElement(this, objectId);
      } else if (result.getClassName().equals("NodeList")) {
        return processElements(objectId);
      } else if (result.getClassName().equals("Array") || result.getClassName().equals("Object")) {
        return debugger.examineScriptResult(objectId);
      }
    }

    return object;
  }

  public Object executeAsyncScript(String script, Object... args) {
    throw new UnsupportedOperationException();
  }

  public Keyboard getKeyboard() {
    return keyboard;
  }

  public Mouse getMouse() {
    return mouse;
  }

  public OperaProxy proxy() {
    return proxy;
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
   * Returns an interface for manipulating the preferences in the currently attached Opera
   * programmatically.  Some changes might require Opera to restart before the changes take affect.
   * The available preferences are found in <code>opera:config</code>.
   *
   * @return methods for interacting with preferences
   */
  public OperaScopePreferences preferences() {
    return preferences;
  }

  public OperaUtils utils() {
    return new OperaUtils() {
      public String getCoreVersion() {
        return coreUtils.getCoreVersion();
      }

      public Platform getPlatform() {
        return Platform.extractFromSysProperty(coreUtils.getOperatingSystem());
      }

      public OperaProduct getProduct() {
        return OperaProduct.get(coreUtils.getProduct());
      }

      public String getBinaryPath() {
        return coreUtils.getBinaryPath();
      }

      public String getUserAgent() {
        return coreUtils.getUserAgent();
      }

      public Integer getPID() {
        return coreUtils.getProcessID();
      }

      public void clearPrivateData(PrivateData... flags) {
        ((CoreUtils) coreUtils).clearPrivateData(flags);
      }
    };
  }

  /**
   * Executes the selftests for the given module.  WebDriver generally has a blocking API so this
   * method will return when the test is complete, or when {@link OperaIntervals#SELFTEST_TIMEOUT}
   * is reached.
   */
  public List<SelftestResult> selftest(String module) {
    return services.selftest(ImmutableList.of(module), OperaIntervals.SELFTEST_TIMEOUT.getMs());
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

  /**
   * Gets list of available Opera actions.
   *
   * @return list of available actions
   * @deprecated
   */
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

  protected List<WebElement> processElements(Integer id) {
    List<Integer> ids = debugger.examineObjects(id);
    List<WebElement> toReturn = new ArrayList<WebElement>();
    for (Integer objectId : ids) {
      toReturn.add(new OperaWebElement(this, objectId));
    }
    return toReturn;
  }

  protected void waitForLoadToComplete() throws ResponseNotReceivedException {
    if (settings.useIdle() && services.isOperaIdleAvailable()) {
      services.waitForOperaIdle(OperaIntervals.OPERA_IDLE_TIMEOUT.getMs());
    } else {
      // Sometimes we get here before the next page has even *started* loading, and so return too
      // quickly. This sleep is enough to make sure readyState has been set to "loading".
      sleep(5);

      long endTime = System.currentTimeMillis() + OperaIntervals.PAGE_LOAD_TIMEOUT.getMs();

      while (!"complete".equals(debugger.executeJavascript("return document.readyState"))) {
        if (System.currentTimeMillis() < endTime) {
          sleep(OperaIntervals.POLL_INTERVAL.getMs());
        } else {
          throw new ResponseNotReceivedException("No response in a timely fashion");
        }
      }
    }
  }

  /**
   * Implicitly wait for an element to become visible.
   *
   * Essentially it polls the client every {@link OperaIntervals#POLL_INTERVAL} until {@link
   * OperaIntervals#IMPLICIT_WAIT} is reached for callable <code>condition</code> to be true.
   *
   * @param condition a callable implementation
   * @param <X>       computes a result, or throws an exception if unable to do so
   * @return a non-null value if condition is met within implicit wait timeout, null otherwise
   */
  protected <X> X implicitlyWaitFor(Callable<X> condition) {
    long end = System.currentTimeMillis() + OperaIntervals.IMPLICIT_WAIT.getMs();
    Exception lastException = null;

    do {
      X toReturn = null;

      try {
        toReturn = condition.call();
      } catch (Exception e) {
        lastException = e;
      }

      if (toReturn instanceof Boolean && !(Boolean) toReturn) {
        continue;
      }

      if (toReturn != null) {
        return toReturn;
      }

      sleep(OperaIntervals.POLL_INTERVAL.getMs());
    } while (System.currentTimeMillis() < end);

    if (lastException != null) {
      if (lastException instanceof RuntimeException) {
        throw (RuntimeException) lastException;
      }

      throw new WebDriverException(lastException);
    }

    return null;
  }

  // Following methods are used in SpartanRunner:

  /**
   * Enable or disable idle functionality during runtime.
   *
   * @param enabled true if idle should be switched on, false if it should be switched off
   */
  @SuppressWarnings("unused")
  protected void setUseOperaIdle(boolean enabled) {
    settings.setIdle(enabled);
  }

  // Following methods are used internally:

  private boolean hasTimeRemaining(long start) {
    return System.currentTimeMillis() - start < OperaIntervals.IMPLICIT_WAIT.getMs();
  }

  /**
   * Releases protected ECMAScript objects to make them garbage collectible.  Released objects are
   * not necessarily freed immediately.  Releasing objects invalidates associated object ID's
   * immediately.
   */
  private void gc() {
    if ((services != null && services.isConnected()) && debugger != null) {
      debugger.releaseObjects();
    }
    objectIds.clear();
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
        sleep(OperaIntervals.POLL_INTERVAL.getMs());
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
        sleep(OperaIntervals.POLL_INTERVAL.getMs());
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

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}