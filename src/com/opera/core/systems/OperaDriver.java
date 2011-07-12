/*
 * Copyright 2008-2011 Opera Software ASA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opera.core.systems;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.HasInputDevices;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keyboard;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Mouse;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.InvalidCoordinatesException;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.opera.core.systems.interaction.OperaAction;
import com.opera.core.systems.interaction.UserInteraction;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.runner.OperaRunner;
import com.opera.core.systems.runner.launcher.OperaLauncherRunner;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.handlers.PbActionHandler;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.OperaKeys;
import com.opera.core.systems.scope.internal.OperaMouseKeys;
import com.opera.core.systems.scope.protos.PrefsProtos.Pref;
import com.opera.core.systems.scope.protos.PrefsProtos.GetPrefArg.Mode;
import com.opera.core.systems.scope.services.ICookieManager;
import com.opera.core.systems.scope.services.ICoreUtils;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IPrefs;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.settings.OperaDriverSettings;

public class OperaDriver extends RemoteWebDriver implements WebDriver,
  FindsByLinkText, FindsById, FindsByXPath, FindsByName, FindsByTagName,
  FindsByClassName, FindsByCssSelector, SearchContext, JavascriptExecutor,
  TakesScreenshot, HasInputDevices {

  /*
   * These are "protected" and not "private" so that we can extend this class
   * and add methods to access these variable in tests.
   */
  protected OperaDriverSettings settings;
  protected OperaRunner operaRunner;

  protected IEcmaScriptDebugger debugger;
  protected IOperaExec exec;
  protected IPrefs prefs;
  protected IWindowManager windowManager;
  protected ICoreUtils coreUtils;
  protected ICookieManager cookieManager;

  protected ScopeServices services;
  protected ScopeActions actionHandler;

  private OperaMouse mouse;
  private OperaKeyboard keyboard;

  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  protected Set<Integer> objectIds = new HashSet<Integer>();
  private String version;

  public OperaDriver() {
    this(makeSettings());
  }

  @Deprecated
  public OperaDriver(boolean autoStart) {
    this(autoStart ? makeSettings() : null);
  }

  /**
   * Constructor that starts Opera.
   *
   * @param settings  an OperaDriverSettings object containing various settings
   *                  for the driver and the browser.
   */
  public OperaDriver(OperaDriverSettings settings) {
    logger.fine("Constructing OperaDriver with settings");
    if (settings != null) {
      this.settings = settings;

      if (settings.getAutostart()) {
        OperaPaths paths = new OperaPaths();

        if (settings.guessOperaPath() && settings.getOperaBinaryLocation() == null) {
          settings.setOperaBinaryLocation(paths.operaPath());
        } else if (settings.getOperaBinaryLocation() == null) {
          // Don't guess, only check environment variable
          String path = System.getenv("OPERA_PATH");

          if (path != null && path.length() > 0) {
            settings.setOperaBinaryLocation(path);
          }
        }

        if (settings.getOperaLauncherBinary() == null) {
          settings.setOperaLauncherBinary(paths.launcherPath());
        }

        if (settings.getOperaBinaryLocation() != null) {
          this.operaRunner = new OperaLauncherRunner(this.settings);
        }
      }
    } else {
      // Create a default settings object
      this.settings = new OperaDriverSettings();
      this.settings.setAutostart(false);
    }

    try {
      this.init();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error initializing OperaDriver with exception ", e);

      // Will make sure to kill any eventual launcher process that was started.
      this.quit();
      throw new WebDriverException(e);
    }

    logger.fine("init() done");
  }

  /**
   * Make a new settings object, automatically finding the Opera and launcher
   * binaries.
   *
   * @return A new settings object that is correctly set up.
   */
  private static OperaDriverSettings makeSettings() {
    OperaDriverSettings settings = new OperaDriverSettings();

    OperaPaths paths = new OperaPaths();

    settings.setOperaBinaryLocation(paths.operaPath());
    settings.setOperaLauncherBinary(paths.launcherPath());

    settings.setOperaBinaryArguments("");

    return settings;
  }

  @Deprecated
  public void shutdown() {
    quit();
  }

  public void quit() {
    logger.fine("Opera Driver shutting down");
    services.quit();
    if (operaRunner != null) operaRunner.shutdown();
  }

  /**
   * For testing override this method.
   */
  protected void init() {
    createScopeServices();

    // Launch Opera if the runner has been setup
    if (operaRunner != null) {
      logger.fine("Calling startOpera");
      operaRunner.startOpera();
    }

    services.init();
    debugger = services.getDebugger();
    windowManager = services.getWindowManager();
    exec = services.getExec();
    mouse = new OperaMouse(this);
    keyboard = new OperaKeyboard(this);
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
    return versions;
  }

  private void createScopeServices() {
    try {
      Map<String, String> versions = getServicesList();
      boolean manualStart = true;

      if (settings.getOperaBinaryLocation() != null) {
        manualStart = false;
      }

      services = new ScopeServices(versions, manualStart);
      services.startStpThread();
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
  }

  public Capabilities getCapabilities() {
    DesiredCapabilities capabilities = DesiredCapabilities.opera();
    capabilities.setJavascriptEnabled(true);
    return capabilities;
  }

  public void get(String url) {
    get(url, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
  }

  public int get(String url, long timeout) {
    if (url == null) throw new NullPointerException("Invalid url");

    if (services.getConnection() == null) throw new CommunicationException("Unable to open URL because Opera is not connected.");

    gc();
    // As this is an artificial page load we release all the held keys.
    // Is the user click()s a link, any keys remain held.
    exec.releaseKeys();

    int activeWindowId = windowManager.getActiveWindowId();

    String oldUrl = getCurrentUrl();

    services.captureOperaIdle();
    actionHandler.get(url);

    if (oldUrl == null || !url.replace(oldUrl, "").startsWith("#")) {

      if (useOperaIdle()) {
        try {
          // Wait for Opera to become idle
          if (timeout == OperaIntervals.PAGE_LOAD_TIMEOUT.getValue()) timeout = OperaIntervals.OPERA_IDLE_TIMEOUT.getValue();
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

    if (OperaIntervals.ENABLE_DEBUGGER.getValue() == 1) switchTo().defaultContent();

    return windowManager.getLastHttpResponseCode().getAndSet(0);
  }

  public String getCurrentUrl() {
    return debugger.executeJavascript("return document.location.href");
  }

  private void gc() {
    debugger.releaseObjects();
    objectIds.clear();
  }

  @SuppressWarnings("unused")
  private String getText(){
    return debugger.executeJavascript("var visibleText = \"\";\n" +
      "    var travers = function(ele)\n" +
      "    {\n" +
      "      var children = ele.childNodes, child = null, i = 0, computedStyle = null;\n" +
      "      for( ; child = children[i]; i++)\n" +
      "      {\n" +
      "        switch (child.nodeType)\n" +
      "        {\n" +
      "          case document.ELEMENT_NODE:\n" +
      "          {\n" +
      "            computedStyle = getComputedStyle(child, null);\n" +
      "            if( computedStyle.getPropertyValue('display') != \"none\" &&\n" +
      "                computedStyle.getPropertyValue('visibility') != \"hidden\" &&\n" +
      "                !/^select$/i.test(child.nodeName) )\n" +
      "            {\n" +
      "              travers(child);\n" +
      "            }\n" +
      "            break;\n" +
      "          }\n" +
      "          case document.CDATA_SECTION_NODE:\n" +
      "          case document.TEXT_NODE:\n" +
      "          {\n" +
      "            visibleText += child.nodeValue;\n" +
      "          }\n" +
      "        }\n" +
      "\n" +
      "      }\n" +
      "      if( /^select|input$/i.test(ele.nodeName) &&\n" +
      "                /^text|button|file|$/i.test(ele.type) )\n" +
      "      {\n" +
      "        visibleText += ele.value\n" +
      "      }\n" +
      "    };\n" +
      "    travers(document);\n" +
      "    return visibleText;");
  }

  public void close() {
    closeWindow();
    windowManager.filterActiveWindow();
  }

  /**
   * Closes all open windows.
   *
   * @deprecated Use <code>for (String win : driver.getWindowHandles()) {
   *   if (driver.getWindowCount() <= 1) break;
   *   driver.switchTo().window(win).close();
   * }</code> on Desktop.
   */
  @Deprecated
  public void closeAll() {
    windowManager.closeAllWindows();
    windowManager.filterActiveWindow();
  }

  private void closeWindow() {
    exec.action("Close page");
  }

  /**
   * Stops the loading of the current page.
   * @deprecated Use {@link #navigate()}.stop()
   */
  @Deprecated
  public void stop() {
    exec.action("Stop");
  }

  public WebElement findElement(By by) {
    return by.findElement(this);
  }

  public List<WebElement> findElements(By by) {
    return by.findElements(this);
  }

  /**
   * Get the source of the last loaded page.
   *
   * The source will be of the modified DOM, <em>not</em> the original HTML. The
   * page source returned is a representation of the underlying DOM: do not
   * expect it to be formatted or escaped in the same way as the response sent
   * from the web server.
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
    return debugger.executeJavascript("return top.window.name ? top.window.name : top.document.title;");
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

  public Options manage() {
    return new OperaOptions();
  }

  public Navigation navigate() {
    return new OperaNavigation();
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
      int framesLength = Integer.valueOf(debugger.executeJavascript("return document.frames.length"));

      if (frameIndex < 0 || frameIndex >= framesLength) throw new NoSuchFrameException(
        "Invalid frame index : " + frameIndex);

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
   * Escape characters for safe insertion in a Javascript string contained by
   * double quotes (").
   *
   * @param string  the string to escape
   *
   * @return        an escaped string
   */
  protected String escapeJsString(String string) {
    return escapeJsString(string, "\"");
  }

  /**
   * Escape characters for safe insertion in a JavaScript string.
   *
   * @param string  the string to escape
   * @param quote   the type of quote to escape. Either " or '
   *
   * @return        the escaped string
   */
  protected String escapeJsString(String string, String quote) {

    /*
     * This should be expanded to match all invalid characters (e.g. newlines)
     * but for the moment we'll trust we'll only get quotes.
     */
    Pattern escapePattern = Pattern.compile("([^\\\\])"+quote);

    /*
     * Prepend a space so that the regex can match quotes at the beginning of
     * the string.
     */
    Matcher m = escapePattern.matcher(" "+string);
    StringBuffer sb = new StringBuffer();

    while (m.find()) {
      /*
       * $1 -> inserts the character before the quote
       * \\\\\" -> \\", apparently just \" isn't treated literally.
       */
      m.appendReplacement(sb, "$1\\\\\"");
    }

    m.appendTail(sb);

    // Remove the prepended space.
    return sb.substring(1);
  }

  private WebElement findActiveElement() {
    return findSingleElement("document.activeElement;", "active element");
  }

  public WebElement findElementByLinkText(String using) {
    return findSingleElement(
      "(function(){\n" +
        "var links = document.getElementsByTagName('a'), element = null;\n" +
        "for (var i = 0; i < links.length && !element; ++i) {\n" +
        "if(links[i].textContent.replace(/\\s+/g, ' ').trim() == \"" + escapeJsString(using) + "\".replace(/\\s+/g, ' ')) {\n" +
        "element = links[i];\n" +
        "}\n" +
        "}\n" +
        "return element;\n})()", "link text"
    );
  }

  public WebElement findElementByPartialLinkText(String using) {
    return findSingleElement(
      "(function(){\n" +
        "var links = document.getElementsByTagName('a'), element = null;\n" +
        "for (var i = 0; i < links.length && !element; ++i) {\n" +
        "if(links[i].textContent.replace(/\\s+/g, ' ').indexOf('" + escapeJsString(using, "'") + "') > -1){\n" +
        "element = links[i];\n" +
        "}\n" +
        "}\n" +
        "return element;\n})()", "link text"
    );
  }

  public List<WebElement> findElementsByLinkText(String using) {
    return findMultipleElements(
      "(function(){\n"+
        "var links = document.links, link = null, i = 0, elements = [];\n" +
        "for( ; link = links[i]; i++)\n" +
        "{\n" +
        "if(link.textContent.replace(/\\s+/g, ' ').trim() == \"" + escapeJsString(using) +"\".replace(/\\s+/g, ' '))\n" +
        "{\n" +
        "elements.push(link);\n" +
        "}\n" +
        "}\n" +
        "return elements; })()", "link text"
    );
  }

  protected List<WebElement> processElements(Integer id) {
    List<Integer> ids = debugger.examineObjects(id);
    List<WebElement> toReturn = new ArrayList<WebElement>();
    for (Integer objectId : ids)
      toReturn.add(new OperaWebElement(this, objectId));
    return toReturn;
  }

  public List<WebElement> findElementsByPartialLinkText(String using) {

    return findMultipleElements(
      "(function(){\n" +
        "var links = document.links, link = null, i = 0, elements = [];\n" +
        "for( ; link = links[i]; i++)\n" +
        "{\n" +
        "if(link.textContent.replace(/\\s+/g, ' ').indexOf('" + escapeJsString(using, "'") +"') > -1)\n" +
        "{\n" +
        "elements.push(link);\n" +
        "}\n" +
        "}\n" +
        "return elements; })()", "partial link text"
    );
  }

  public WebElement findElementById(String using) {
    return findSingleElement("document.getElementById('" + escapeJsString(using, "'") + "');", "id");
  }

  /**
   * This method breaks web standards
   */
  public List<WebElement> findElementsById(String using) {
    return findMultipleElements(
      "var alls = document.all, element = null, i = 0, elements = [];\n" +
        "for( ; element = alls[i]; i++)\n" +
        "{\n" +
        "if(element.getAttribute('id') == '" + escapeJsString(using, "'") +"')\n" +
        "{\n" +
        "elements.push(element);\n" +
        "}\n" +
        "}\n" +
        "return elements;", "by id"
    );
  }

  public WebElement findElementByXPath(String using) {
    return findSingleElement(
      "document.evaluate(\"" +
        escapeJsString(using) +
        "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;\n", "xpath"
    );
  }

  public WebElement findElementByClassName(String using) {
    return findSingleElement(
      "document.getElementsByClassName('" + escapeJsString(using, "'") + "')[0];", "class name"
    );
  }

  public List<WebElement> findElementsByClassName(String using) {
    return findMultipleElements(
      "document.getElementsByClassName('" + escapeJsString(using, "'") + "');\n", "class name"
    );
  }

  public List<WebElement> findElementsByXPath(String using) {
    return findMultipleElements(
      "var result = document.evaluate(\"" +
        escapeJsString(using) +
        "\", document, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n" +
        "var elements = new Array();\n" +
        "var element = result.iterateNext();\n" +
        "while (element) {\n" + "  elements.push(element);\n" +
        " element = result.iterateNext();\n" + "}\n" +
        "return elements", "XPath"
    );
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

  public WebElement findElementByTagName(String using) {
    if (using.contains(":")) {  // has prefix
      String[] tagInfo = using.split(":");

      return findSingleElement(
        "(function() { var elements = document.getElementsByTagName('" + tagInfo[1] + "'), element = null;" +
          "for( var i = 0; i < elements.length; i++ ) {" +
          "if( elements[i].prefix == '" + tagInfo[0] + "' ) {" +
          "element = elements[i];" +
          "break;" +
          "}" +
          "}" +
          "return element; })()", "tag name"
      );
    }

    return findSingleElement("document.getElementsByTagName('" + escapeJsString(using, "'") +"')[0];", "tag name");
  }


  public List<WebElement> findElementsByTagName(String using) {
    if (using.contains(":")) {  // has prefix
      String[] tagInfo = using.split(":");

      return findMultipleElements(
        "(function() { var elements = document.getElementsByTagName('" + tagInfo[1] + "'), output = [];" +
          "for( var i = 0; i < elements.length; i++ ) {" +
          "if( elements[i].prefix == '" + tagInfo[0] + "' ) {" +
          "output.push(elements[i]);" +
          "}" +
          "}" +
          "return output; })()", "tag name"
      );
    }

    return findMultipleElements("document.getElementsByTagName('"+ escapeJsString(using, "'") + "');\n", "tag name");
  }

  public WebElement findElementByCssSelector(String using) {
    return findSingleElement("document.querySelector('" + escapeJsString(using, "'") + "');", "selector");
  }

  public List<WebElement> findElementsByCssSelector(String using) {
    return findMultipleElements(
      "(function(){ var results = document.querySelectorAll('" +
        escapeJsString(using, "'") +
        "'), returnValue = [], i=0;for(;returnValue[i]=results[i];i++); return returnValue;})()",
      "selector"
    );
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

  // TODO: Numeric overflow
  private class OperaOptions implements Options {
    public void addCookie(Cookie cookie) {
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
        throw new UnsupportedOperationException("Deleting cookies are not supported without the cookie-manager service");
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
        throw new UnsupportedOperationException("Deleting cookies are not supported without the cookie-manager service");
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
        throw new UnsupportedOperationException("Setting cookies are not supported without the cookie-manager service");
      }

      return cookieManager.getCookie(debugger.executeJavascript("window.location.hostname"), null);
    }

    public Cookie getCookieNamed(String name) {
      Set<Cookie> allCookies = getCookies();

      for (Cookie cookie : allCookies) {
        if (cookie.getName().equals(name)) return cookie;
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
   * @param using The action to perform. For a list of actions call
   *          {@link #getOperaActionList()} at run time
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

      if (elements != null) count = elements.size();

      if (count == 0 && hasTimeRemaining(start)) sleep(OperaIntervals.EXEC_SLEEP.getValue());
      else break;

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
      Boolean isStale = Boolean.valueOf(debugger.callFunctionOnObject("locator.parentNode == undefined", id));

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
   * Takes a screenshot of the whole screen, including areas outside of the
   * Opera browser window.
   *
   * @param timeout the number of milliseconds to wait before taking the
   *                screenshot
   * @param hashes  A previous screenshot MD5 hash.  If it matches the hash of
   *                this screenshot then no image data is returned.
   *
   * @return        a ScreenShotReply object
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
    return (settings.getUseOperaIdle() && isOperaIdleAvailable());
  }

  public void setUseOperaIdle(boolean useIdle) {
    settings.setUseOperaIdle(useIdle);
  }

  public Object executeScript(String script, Object... args) {
    Object object = debugger.scriptExecutor(script, args);

    // We probably have an element _or_ a list.
    if (object instanceof ScriptResult) {
      ScriptResult result = (ScriptResult) object;
      Integer objectId = result.getObjectId();

      if (objectId == null) return null;
      if (result.getClassName().endsWith("Element")) return new OperaWebElement(this, objectId);
      if (result.getClassName().equals("NodeList")) return processElements(objectId);
      if (result.getClassName().equals("Array")
        || result.getClassName().equals("Object")) return debugger.examineScriptResult(objectId);
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
   * @deprecated
   *
   * @param action  a string identifying the Opera Action to use.
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
   * Presses and releases the given key.  If the key is "enter" then OperaDriver
   * waits for the page to finish loading.
   *
   * @param key  A string containing the key to press. This can be a single
   *             character (e.g. "a") or a special key (e.g. "left"), and is
   *             matched case insensitively.  For a list of keys see
   *             {@link OperaKeys}.
   */
  public void key(String key) {
    if (key.equalsIgnoreCase("enter")) services.captureOperaIdle();

    keyDown(key);
    keyUp(key);

    if (key.equalsIgnoreCase("enter")) {
      waitForLoadToComplete();
    }
  }

  /**
   * Presses and holds the given key. You cannot press a key that is already
   * down.
   *
   * @param key  the key to press, see {@link #key(String)} for more
   *             information.
   */
  public void keyDown(String key) {
    exec.key(key, false);
  }

  /**
   * Releases the given key.
   *
   * @param key  the key to release, see {@link #key(String)} for more
   *             information.
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
   * Types the given string as-is in to the browser window.  To press special
   * keys use {@link #key(String)}.
   *
   * @param using  the string to type
   */
  public void type(String using) {
    exec.type(using);
  }

  public void mouseEvent(int x, int y, int value) {
    exec.mouseAction(x, y, value, 1);
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
   * @param section  the section the preference is in
   * @param key      the key name of the preference to get
   *
   * @return         the value of the preference
   */
  public String getPref(String section, String key) {
    return services.getPrefs().getPref(section, key, Mode.CURRENT);
  }

  /**
   * Gets the default value of the requested preference.
   *
   * @param section  the section the preference is in
   * @param key      the key name of the preference
   *
   * @return         the default string value of the preference
   */
  public String getDefaultPref(String section, String key) {
    return services.getPrefs().getPref(section, key, Mode.DEFAULT);
  }

  /**
   * Returns a Map of preference names to preferences in the requested section.
   *
   * @param sort     whether to alphabetically sort the preference keys
   * @param section  the section to retrieve the preferences from
   *
   * @return         a Map of preference names to preferences.
   */
  public Map<String, Pref> listPrefs(boolean sort, String section) {
    Map<String, Pref> map = new HashMap<String, Pref>();

    for (Pref p : prefs.listPrefs(sort, section)) {
      map.put(p.getKey(), p);
    }
    return map;
  }

  /**
   * Returns a Map of sections names mapping to a Map of preference names
   * mapping to Pref objects.
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
   * @param section  the section the preference is in
   * @param key      the key name of the preference to set
   * @param value    the value to set the preference to
   */
  public void setPref(String section, String key, String value) {
    services.getPrefs().setPrefs(section, key, value);
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

}
