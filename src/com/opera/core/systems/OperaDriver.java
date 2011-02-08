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
import java.util.logging.Logger;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.Speed;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;

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
import com.opera.core.systems.scope.protos.PrefsProtos.GetPrefArg.Mode;
import com.opera.core.systems.scope.protos.PrefsProtos.Pref;
import com.opera.core.systems.scope.services.ICookieManager;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IPrefs;
import com.opera.core.systems.scope.services.IWindowManager;
import com.opera.core.systems.settings.OperaDriverSettings;

/**
 * Opera's webdriver implementation.
 * See CREDITS and LICENSE
 *
 * @author Deniz Turkoglu, Stuart Knightley
 *
 */
public class OperaDriver implements WebDriver, FindsByLinkText, FindsById, FindsByXPath, FindsByName, FindsByTagName, FindsByClassName,
		FindsByCssSelector, SearchContext, JavascriptExecutor {

	// These are "protected" and not "private" so that we can extend this class
	// and add methods to access these variable in tests
	protected OperaDriverSettings settings;
	protected OperaRunner operaRunner;

	private boolean isDriverStarted = false; //Does this driver have a started opera? Makes it possible to restart opera without throwing out the driver.
	protected final Logger logger = Logger.getLogger(this.getClass().getName());

	protected IEcmaScriptDebugger debugger;
	protected IOperaExec exec;
	protected IPrefs prefs;
	protected IWindowManager windowManager;
	protected ICookieManager cookieManager;
	protected ScopeServices services;
	protected ScopeActions actionHandler;

	protected Set<Integer> objectIds = new HashSet<Integer>();

	public OperaDriver() {
		this(true);
	}

	public OperaDriver(boolean autoStart) {
		this(autoStart ? makeSettings() : null);
	}

	/**
	 * Constructor that starts opera.
	 */
	public OperaDriver(OperaDriverSettings settings){
    if (settings != null) {
      this.settings = settings;

      OperaPaths paths = new OperaPaths();
      if (settings.getOperaBinaryLocation() == null) {
        settings.setOperaBinaryLocation(paths.operaPath());
      }
      if (settings.getOperaLauncherBinary() == null) {
        settings.setOperaLauncherBinary(paths.launcherPath());
      }

      this.operaRunner = new OperaLauncherRunner(this.settings);
    }


    init();
  }

	 /**
   * Make a new settings object, automatically finding the Opera and launcher
   * binaries.
   * @return A new settings object that is correctly set up.
   */
  private static OperaDriverSettings makeSettings() {
    OperaDriverSettings settings = new OperaDriverSettings();
    settings.setRunOperaLauncherFromOperaDriver(true);

    OperaPaths paths = new OperaPaths();

    settings.setOperaBinaryLocation(paths.operaPath());
    settings.setOperaLauncherBinary(paths.launcherPath());

    settings.setOperaBinaryArguments("");

    return settings;
  }

	/**
	 * Shutdown webdriver, will kill opera and such if running.
	 */
	public void shutdown(){
		if(isDriverStarted)
			quit();
		else
			services.shutdown();
		if (operaRunner != null)
			operaRunner.shutdown();
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
		windowManager = services.getWindowManager();
		exec = services.getExec();
		actionHandler = new PbActionHandler(services);
		cookieManager = services.getCookieManager();
		//cookieManager.updateCookieSettings();
		prefs = services.getPrefs();
	}

	protected Map<String, String> getServicesList()
	{
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

			if(settings != null && settings.getOperaBinaryLocation() != null)
				manualStart = false;

			services = new ScopeServices(versions, manualStart);
			services.startStpThread();
		} catch (IOException e) {
			throw new WebDriverException(e);
		}
	}

	public void get(String url) {
            get(url, OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
	}

	public int get(String url, long timeout) {

		if (url == null)
			throw new NullPointerException("Invalid url");

		if (services.getConnection() == null)
			throw new CommunicationException(
					"Unable to open URL because Opera is not connected.");

		gc();

		int activeWindowId = windowManager.getActiveWindowId();

		String oldUrl = getCurrentUrl();

		actionHandler.get(url);

		if (!url.replace(oldUrl, "").startsWith("#")) {

			if (services.isOperaIdleAvailable()) {
				try {
					// Wait for opera is idle
					services.waitForOperaIdle(timeout);
				} catch (WebDriverException e) {
					// This could for example be a gif animation, preventing
					// idle from being passed.
					// Common case, and should not result in test error.
					System.out.println("Opera Idle timed out, continue test... exception: " + e);
				}
			} else {
				// Wait for window is loaded
				services.waitForWindowLoaded(activeWindowId, timeout);
			}

		}

		if (OperaIntervals.ENABLE_DEBUGGER.getValue() == 1)
			switchTo().defaultContent();

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
        return debugger.executeJavascript("var visibleText = \"\";\n"+
                "    var travers = function(ele)\n"+
                "    {\n"+
                "      var children = ele.childNodes, child = null, i = 0, computedStyle = null;\n"+
                "      for( ; child = children[i]; i++)\n"+
                "      {\n"+
                "        switch (child.nodeType)\n"+
                "        {\n"+
                "          case document.ELEMENT_NODE:\n"+
                "          {\n"+
                "            computedStyle = getComputedStyle(child, null);\n"+
                "            if( computedStyle.getPropertyValue('display') != \"none\" &&\n"+
                "                computedStyle.getPropertyValue('visibility') != \"hidden\" &&\n"+
                "                !/^select$/i.test(child.nodeName) )\n"+
                "            {\n"+
                "              travers(child);\n"+
                "            }\n"+
                "            break;\n"+
                "          }\n"+
                "          case document.CDATA_SECTION_NODE:\n"+
                "          case document.TEXT_NODE:\n"+
                "          {\n"+
                "            visibleText += child.nodeValue;\n"+
                "          }\n"+
                "        }\n"+
                "\n"+
                "      }\n"+
                "      if( /^select|input$/i.test(ele.nodeName) &&\n"+
                "                /^text|button|file|$/i.test(ele.type) )\n"+
                "      {\n"+
                "        visibleText += ele.value\n"+
                "      }\n"+
                "    };\n"+
                "    travers(document);\n"+
                "    return visibleText;");
    }

	public void close() {
		closeWindow();
		windowManager.filterActiveWindow();
	}

	/**
	 * Closes all open windows.
	 */
	public void closeAll() {
		windowManager.closeAllWindows();
		windowManager.filterActiveWindow();
	}

	private void closeWindow() {
		exec.action("Close page");
	}

	/**
	 * Stops the loading of the current page.
	 */
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
   * The source will be of the modified DOM, <em>not</em> the original HTML.
   * The page source returned is a representation of the underlying DOM: do not
   * expect it to be formatted or escaped in the same way as the response sent
   * from the web server.
	 */
	public String getPageSource() {
		return debugger.executeJavascript("return document.documentElement.outerHTML");
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

		if(OperaIntervals.ENABLE_DEBUGGER.getValue() != 1) {
			for (Integer windowId : windowIds) {
				handles.add(windowId.toString());
			}
			return handles;
		}

		windowManager.clearFilter();

		for (Integer windowId : windowIds) {
			//windowManager.filterWindow(windowId);
			String handleName = debugger.executeJavascript("return top.window.name ? top.window.name : (top.document.title ? top.document.title : 'undefined');", windowId);
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

	public void quit() {
		services.quit();
	}

	public TargetLocator switchTo() {
		return new OperaTargetLocator();
	}

	private class OperaTargetLocator implements TargetLocator {

		public WebElement activeElement() {
			return OperaDriver.this.findActiveElement();
		}

		public WebDriver defaultContent() {
			//change to _top
			windowManager.filterActiveWindow();
			debugger.resetFramePath();
			if(findElementsByTagName("frameset").size() > 0) {
				switchTo().frame(0);
			}
			//waitForLoadToComplete();
			return OperaDriver.this;
		}

		public WebDriver frame(int frameIndex) {
			//make sure we execute this one on "_top"
			debugger.resetFramePath();
			int framesLength = Integer.valueOf(debugger.executeJavascript("return document.frames.length"));

			if(frameIndex < 0 || frameIndex >= framesLength)
				throw new NoSuchFrameException("Invalid frame index : " + frameIndex);

			debugger.changeRuntime(frameIndex);

			return OperaDriver.this;
		}

		public WebDriver frame(String frameName) {
			debugger.resetFramePath();
			debugger.changeRuntime(frameName);
			return OperaDriver.this;
		}


		public WebDriver window(String windowName) {
			windowManager.clearFilter();

			List<Integer> windowIds = windowManager.getWindowHandles();

			Integer id = 0;

			for (Integer windowId : windowIds) {
				String name = debugger.executeJavascript("return top.window.name ? top.window.name : top.document.title;", windowId);
				if(name.equals(windowName)) {
					id = windowId;
					break;
				}
			}

			if(id == 0)
				throw new NoSuchWindowException("Window with name "  + windowName + " not found");
			windowManager.setActiveWindowId(id);

			windowManager.filterActiveWindow();
			debugger.resetRuntimesList();

			defaultContent(); //set runtime to _top
			debugger.executeJavascript("window.focus()", false); //steal focus
			return OperaDriver.this;
		}

		// TODO implement
		public WebDriver frame(WebElement frameElement) {
			throw new UnsupportedOperationException();
		}

		// This and more functionality will be provided natively in near future, in progress
		public Alert alert() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * @return list of frames available for chosing
	 */
	public List<String> listFrames(){
		return debugger.listFramePaths();
	}

	private WebElement findActiveElement() {
		return findSingleElement("document.activeElement;", "active element");
	}

	public WebElement findElementByLinkText(String using) {
		return findSingleElement("(function(){\n"+
	        "var links = document.getElementsByTagName('a'), element = null;\n"+
	        "for (var i = 0; i < links.length && !element; ++i) {\n"+
	        "if(links[i].textContent.replace(/\\s+/g, ' ').trim() == \"" + using +"\".replace(/\\s+/g, ' ')) {\n"+
	        "element = links[i];\n"+
	        "}\n"+
	        "}\n"+
	        "return element;\n})()", "link text");
	}

	public WebElement findElementByPartialLinkText(String using) {
		return findSingleElement("(function(){\n"+
		        "var links = document.getElementsByTagName('a'), element = null;\n"+
		        "for (var i = 0; i < links.length && !element; ++i) {\n"+
		        "if(links[i].textContent.replace(/\\s+/g, ' ').indexOf('" + using +"') > -1){\n"+
		        "element = links[i];\n"+
		        "}\n"+
		        "}\n"+
		        "return element;\n})()", "link text");
	}

	public List<WebElement> findElementsByLinkText(String using) {
		return findMultipleElements("(function(){\n"+
				"var links = document.links, link = null, i = 0, elements = [];\n"+
				"for( ; link = links[i]; i++)\n"+
				"{\n"+
				"if(link.textContent.replace(/\\s+/g, ' ').trim() == \"" + using +"\".replace(/\\s+/g, ' '))\n"+
				"{\n"+
				"elements.push(link);\n"+
					"}\n"+
				"}\n" +
				"return elements; })()", "link text");
	}

	protected List<WebElement> processElements(Integer id){
		List<Integer> ids = debugger.examineObjects(id);
		List<WebElement> toReturn = new ArrayList<WebElement>();
		for (Integer objectId : ids)
			toReturn.add(new OperaWebElement(this, objectId));
		return toReturn;
	}

	public List<WebElement> findElementsByPartialLinkText(String using) {

		return findMultipleElements("(function(){\n"+
				"var links = document.links, link = null, i = 0, elements = [];\n"+
				"for( ; link = links[i]; i++)\n"+
				"{\n"+
				"if(link.textContent.replace(/\\s+/g, ' ').indexOf('" + using +"') > -1)\n"+
				"{\n"+
				"elements.push(link);\n"+
					"}\n"+
				"}\n" +
				"return elements; })()", "partial link text");
	}

	public WebElement findElementById(String using) {
		//return findSingleElement("document.querySelector(\"#\" + " + using + ")", "id");
		return findSingleElement("document.getElementById('" + using + "');", "id");
	}

	/**
	 * This method breaks web standards
	 */
	public List<WebElement> findElementsById(String using) {
		return findMultipleElements("var alls = document.all, element = null, i = 0, elements = [];\n" +
				"for( ; element = alls[i]; i++)\n"+
				"{\n"+
				    "if(element.getAttribute('id') == '" + using +"')\n"+
				    "{\n"+
				      "elements.push(element);\n"+
				    "}\n"+
				  "}\n"+
				  "return elements;", "by id");

	}

	public WebElement findElementByXPath(String using) {
		return findSingleElement("document.evaluate(\"" + using +"\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE,  null).singleNodeValue;\n", "xpath");
	}

	public WebElement findElementByClassName(String using) {
		return findSingleElement("document.getElementsByClassName('" + using +"')[0];", "class name");
	}

	public List<WebElement> findElementsByClassName(String using) {
		return findMultipleElements("document.getElementsByClassName('"+ using + "');\n", "class name");
	}

	public List<WebElement> findElementsByXPath(String using) {
		return findMultipleElements("var result = document.evaluate(\""
						+ using
						+ "\", document, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n"
						+ "var elements = new Array();\n"
						+ "var element = result.iterateNext();\n"
						+ "while (element) {\n" + "  elements.push(element);\n"
						+ "  element = result.iterateNext();\n" + "}\n"
						+ "return elements", "XPath");
	}

	//FIXME when timeout has completed, send 'stop' command?
	public void waitForLoadToComplete() {
		if(services.isOperaIdleAvailable()){
			//new opera wait for page
			services.waitForOperaIdle(OperaIntervals.PAGE_LOAD_TIMEOUT.getValue());
		} else {
			//old bad opera wait for page
			long endTime = System.currentTimeMillis() + OperaIntervals.PAGE_LOAD_TIMEOUT.getValue();
			while (!"complete".equals(debugger.executeJavascript("return document.readyState"))) {
				if(System.currentTimeMillis() < endTime)
					sleep(OperaIntervals.POLL_INVERVAL.getValue());
				else
					throw new WebDriverException("Timeout while loading page");
			}
		}
	}

	public WebElement findElementByName(String using) {
		return findSingleElement("document.getElementsByName('" + using +"')[0];","name");
	}

	public List<WebElement> findElementsByName(String using) {
		return findMultipleElements("document.getElementsByName('"+ using + "');", "name");
	}

	private class OperaNavigation implements Navigation {
		public void back() {
			exec.action("Back");
			sleep(OperaIntervals.SCRIPT_RETRY_INTERVAL.getValue());
			for(int i = 0; i < 5; i++) {
				if(debugger.updateRuntime()) break;
				sleep(i * OperaIntervals.SCRIPT_RETRY_INTERVAL.getValue());
			}
			waitForLoadToComplete();
		}

		public void forward() {
			exec.action("Forward");
			sleep(OperaIntervals.SCRIPT_RETRY_INTERVAL.getValue());
			for(int i = 0; i < 5; i++) {
				if(debugger.updateRuntime()) break;
				sleep(i * OperaIntervals.SCRIPT_RETRY_INTERVAL.getValue());
			}
			waitForLoadToComplete();
		}

		public void to(String url) {
			get(url);
		}

		public void to(URL url) {
			get(String.valueOf(url));
		}

		public void refresh() {
			exec.action("Reload");
			waitForLoadToComplete();
		}

	}

	private class OperaOptions implements Options {
		public void addCookie(Cookie cookie) {
			if(cookie.getExpiry() == null)
				cookie = new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), new Date(new Date().getTime() + (10 * 365 * 24 * 60 * 60 * 1000)), false);
			debugger.executeJavascript("document.cookie='" + cookie.toString() + "'", false);
		}

		public void deleteCookieNamed(String name) {
			Cookie cookie = getCookieNamed(name);
			deleteCookie(cookie);
		}

		public void deleteCookie(Cookie cookie) {
			if(cookieManager == null)
				throw new UnsupportedOperationException("Deleting cookies are not supported without the cookie-manager service");

			cookieManager.removeCookie(cookie.getDomain(), cookie.getPath(), cookie.getName());
			gc();
			/*
			Date dateInPast = new Date(0);
			Cookie toDelete = new Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), dateInPast, false);
			addCookie(toDelete);
			*/
		}

		public void deleteAllCookies() {
			if(cookieManager == null)
				throw new UnsupportedOperationException("Deleting cookies are not supported without the cookie-manager service");

			cookieManager.removeAllCookies();
			/*
			Set<Cookie> cookies = getCookies();
			for (Cookie cookie : cookies) {
				deleteCookie(cookie);
			}
			*/
		}

		public Set<Cookie> getCookies() {
			if(cookieManager == null)
				throw new UnsupportedOperationException("Setting cookies are not supported without the cookie-manager service");

			return cookieManager.getCookie(debugger.executeJavascript("window.location.hostname"), null);
		}

		public Speed getSpeed() {
			throw new UnsupportedOperationException("getMouseSpeed");
		}

		public void setSpeed(Speed speed) {
			throw new UnsupportedOperationException("setMouseSpeed");
		}

		public Cookie getCookieNamed(String name) {
			Set<Cookie> allCookies = getCookies();

			for (Cookie cookie : allCookies) {
				if(cookie.getName().equals(name))
					return cookie;
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
	 * @param using The action to perform. For a list of actions call
	 * {@link #getOperaActionList()} at run time
	 * @param params Parameters to pass to the action call
	 */
	@SuppressWarnings("unused")
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

	public WebElement findElementByTagName(String using) {
		if(using.contains(":")) {//has prefix
			String[] tagInfo = using.split(":");
			return findSingleElement("(function() { var elements = document.getElementsByTagName('" + tagInfo[1] + "'), element = null;" +
					"for( var i = 0; i < elements.length; i++ ) {" +
					"if( elements[i].prefix == '" + tagInfo[0] + "' ) {" +
					"element = elements[i];" +
					"break;"+
					"}" +
					"}" +
					"return element; })()", "tag name");
		}
		return findSingleElement("document.getElementsByTagName('" + using +"')[0];", "tag name");
	}


	public List<WebElement> findElementsByTagName(String using) {
		if(using.contains(":")) {//has prefix
			String[] tagInfo = using.split(":");
			return findMultipleElements("(function() { var elements = document.getElementsByTagName('" + tagInfo[1] + "'), output = [];" +
					"for( var i = 0; i < elements.length; i++ ) {" +
					"if( elements[i].prefix == '" + tagInfo[0] + "' ) {" +
					"output.push(elements[i]);" +
					"}" +
					"}" +
					"return output; })()", "tag name");
		}
		return findMultipleElements("document.getElementsByTagName('"+ using + "');\n", "tag name");
	}

	public WebElement findElementByCssSelector(String using) {
            return findSingleElement("document.querySelector('" + using +"');", "selector");
	}

	public List<WebElement> findElementsByCssSelector(String using) {
            return findMultipleElements("(function(){ var results = document.querySelectorAll('"+ using + "'), returnValue = [], i=0;for(;returnValue[i]=results[i];i++); return returnValue;})()", "selector");
	}

	private final List<WebElement> findMultipleElements(String script, String type) {
		Integer id;

		long start = System.currentTimeMillis();
		int count = 0;

		List<WebElement> elements;

		do {
			id = debugger.getObject(script);
			elements = processElements(id);

			if(elements != null)
				count = elements.size();

			if (count == 0 && hasTimeRemaining(start))
				sleep(OperaIntervals.EXEC_SLEEP.getValue());
			else break;

		} while (true);

		OperaIntervals.WAIT_FOR_ELEMENT.setValue(0L);

		if (id != null) {
			return elements;
		} else {
			throw new NoSuchElementException("Cannot find element(s) with " + type);
		}
	}

	private final WebElement findSingleElement(String script, String type) {
		long start = System.currentTimeMillis();
		boolean isAvailable = false;

		do {
			isAvailable = isElementAvailable(script);

			if (!isAvailable && hasTimeRemaining(start))
				sleep(OperaIntervals.EXEC_SLEEP.getValue());
			else break;

		} while (true);

		OperaIntervals.WAIT_FOR_ELEMENT.setValue(0L);

		if (isAvailable) {
			Integer id = debugger.getObject(script);
			Boolean isStale = Boolean.valueOf(debugger.callFunctionOnObject("locator.parentNode == undefined", id));

			if(isStale)
				throw new StaleElementReferenceException("This element is no longer part of DOM");

			return new OperaWebElement(this, id);
		} else {
			throw new NoSuchElementException("Cannot find element(s) with " + type);
		}
	}


	boolean hasTimeRemaining(long start) {
		return System.currentTimeMillis() - start < OperaIntervals.WAIT_FOR_ELEMENT.getValue();
	}

	private final boolean isElementAvailable(String script) {
		return debugger.getObject(script) != null;
	}
	/*
	public String saveScreenshot(String fileName, int timeout, String... hashes) {
		return screenWatcher(fileName, timeout, true, hashes);
	}

	public ScreenShotReply saveScreenshot(Canvas canvas, long timeout, boolean includeImage, String... hashes) {

		 * FIXME: This _needs_ be cleaned up.  All things related to
		 * `timeout` should, ideally, be removed and handled elsewhere.


		//
		// * No reason to wait if we have idle control.  Builds without
		// * OperaIdle enabled will fail if timeout is 1 or less.
		//
		if(services.isOperaIdleAvailable()) {
			timeout = 1;
		} else if (!services.isOperaIdleAvailable() && timeout <= 1) {
			timeout = 10;
		}

        return exec.screenWatcher(canvas, timeout, includeImage, hashes);
	}

	private String screenWatcher(String fileName, int timeout, boolean saveFile, String... hashes) {
		Canvas canvas = new Canvas();
		canvas.setX(0);
		canvas.setY(0);

		String[] dimensions = debugger.executeJavascript("return (window.innerWidth + \",\" + window.innerHeight);").split(",");
		canvas.setH(Integer.valueOf(dimensions[1]));
		canvas.setW(Integer.valueOf(dimensions[0]));
		canvas.setViewPortRelative(true);

		ScreenShotReply screenshot = saveScreenshot(canvas, timeout, saveFile, hashes);

		if(saveFile && screenshot.getPng() != null) {
			FileOutputStream stream;

			try {
				stream = new FileOutputStream(fileName);
				stream.write(screenshot.getPng());
				stream.close();
			} catch (Exception e) {
				throw new WebDriverException("Failed to write file: " + e.getMessage());
			}
		}

		return screenshot.getMd5();
	}
	*/

	/**
	 * Takes a screenshot of the whole screen, including areas outside of the
	 * Opera browser window.
	 * @param timeout The number of milliseconds to wait before taking the
	 * screenshot
	 * @param hashes A previous screenshot MD5 hash. If it matches the hash
	 * of this screenshot then no image data is returned.
	 */
	public ScreenShotReply saveScreenshot(long timeout, String... hashes)
	{
		/*
		if(services.isOperaIdleAvailable()) {
			timeout = 1;
		} else if (!services.isOperaIdleAvailable() && timeout <= 1) {
			timeout = 10;
		}
		*/
		return operaRunner.saveScreenshot(timeout, hashes);
	}

	public boolean isOperaIdleAvailable()
	{
		return services.isOperaIdleAvailable();
	}

	public Object executeScript(String script, Object... args) {
            Object object = debugger.scriptExecutor(script, args);

            //we probably have an element OR list
            if(object instanceof ScriptResult) {
                ScriptResult result = (ScriptResult) object;
                Integer objectId = result.getObjectId();
                if(objectId == null)
                    return null;
                if(result.getClassName().endsWith("Element"))
                    return new OperaWebElement(this, objectId);
                if(result.getClassName().equals("NodeList"))
                    return processElements(objectId);
                if(result.getClassName().equals("Array") || result.getClassName().equals("Object"))
                    return debugger.examineScriptResult(objectId);
            }
            return object;
	}


	public boolean isJavascriptEnabled() {
		return (OperaIntervals.ENABLE_DEBUGGER.getValue() == 1);
	}

	public void executeActions(OperaAction action) {
            List<UserInteraction> actions = action.getActions();
            for (UserInteraction userInteraction : actions) {
                userInteraction.execute(this);
            }
            waitForLoadToComplete();
	}

	/**
	 * Presses and releases the given key. If the key is "enter" then OperaDriver
	 * waits for the page to finish loading.
	 * @param key A string containing the key to press. This can be a single
	 * character (e.g. "a") or a special key (e.g. "left"), and is matched
	 * case insensitively. For a list of keys see {@link OperaKeys}.
	 */
	public void key(String key) {
		keyDown(key);
		keyUp(key);

		if(key.equalsIgnoreCase("enter")) {
			sleep(OperaIntervals.EXEC_SLEEP.getValue());
			waitForLoadToComplete();
		}
	}

	/**
	 * Presses and holds the given key. You cannot press a key that is already
	 * down.
	 * @param key The key to press. See {@link #key(String)} for more information.
	 */
	public void keyDown(String key) {
            exec.key(key, false);
	}

	/**
   * Releases the given key.
   * @param key The key to release. See {@link #key(String)} for more
   * information.
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
	 * Types the given string as-is in to the browser window. To press special
	 * keys use {@link #key(String)}.
	 * @param using The string to type
	 */
	public void type(String using) {
            exec.type(using);
	}

	public void mouseEvent(int x, int y, int value) {
            exec.mouseAction(x, y, value, 1);
	}

	/**
	 * Returns the version number of driver.
	 * Replaced with the version during build
	 * 
	 */
    public String getVersion() {
    	return "{VERSION}";
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
	 * @param section The section the preference is in.
	 * @param key The key name of the preference to get.
	 * @return The value of the preference.
	 */
	public String getPref(String section, String key) {
		return services.getPrefs().getPref(section, key, Mode.CURRENT);
	}

	/**
	 * Gets the default value of the requested preference.
	 * @param section The section the preference is in.
	 * @param key The key name of the preference.
	 * @return The default string value of the preference.
	 */
	public String getDefaultPref(String section, String key) {
		return services.getPrefs().getPref(section, key, Mode.DEFAULT);
	}

	/**
	 * Returns a list of preferences in the requested section.
	 * @param sort Whether to alphabetically sort the preference keys.
	 * @param section The section to retreive the preferences from.
	 * @return A list of preferences.
	 */
	public List<Pref> listPrefs(boolean sort, String section) {
		return services.getPrefs().listPrefs(sort, section);
	}

	/**
	 * Set the {@link value} of the preference, {@link key}, in section
	 * {@link section}.
	 * @param section The section the preference is in.
	 * @param key The key name of the preference to set.
	 * @param value The value to set the preference to.
	 */
	public void setPref(String section, String key, String value) {
		services.getPrefs().setPrefs(section, key, value);
	}

	public Object executeAsyncScript(String script, Object... args) {
		throw new UnsupportedOperationException();
	}

}

