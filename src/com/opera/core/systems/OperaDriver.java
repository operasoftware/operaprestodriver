/*
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.
Copyright 2009 Opera Software ASA.

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

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.Speed;
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
import org.openqa.selenium.internal.ReturnedCookie;

import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.OperaAction;
import com.opera.core.systems.model.ScopeActions;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.model.UserInteraction;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.handlers.PbActionHandler;
import com.opera.core.systems.scope.internal.OperaBinary;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;
import com.opera.core.systems.scope.services.IWindowManager;

public class OperaDriver implements WebDriver, FindsByLinkText, FindsById,FindsByXPath, FindsByName, FindsByTagName, FindsByClassName,
		FindsByCssSelector, SearchContext, JavascriptExecutor {
	
	private IEcmaScriptDebugger debugger;
	private IOperaExec exec;
	private IWindowManager windowManager;
	private ScopeServices services;
	protected ScopeActions actionHandler;
	
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

	// TODO
	// Profiling
	public OperaDriver() {
		this(null);
	}
	
	public OperaDriver(String executableLocation, String... arguments){
		String operaPath = System.getenv().get("OPERA_PATH");
		String operaArgs = System.getenv().get("OPERA_ARGS");
		
		if(operaPath != null && operaPath.length() > 0) {
			arguments = (operaArgs != null && operaArgs.length() > 0) ? operaArgs.split(",") : null;
			new OperaBinary(operaPath, arguments);
		} else if(executableLocation != null) {
			new OperaBinary(executableLocation, arguments);
		}
		init();
	}

	/**
	 * For testing override this method.
	 */
	protected void init() {
		createScopeServices();
		services.init();
		debugger = services.getDebugger();
		windowManager = services.getWindowManager();
		exec = services.getExec();
		actionHandler = new PbActionHandler(services);
	}

	private void createScopeServices() {
		try {
			Map<String, String> versions = new HashMap<String, String>();
			versions.put("ecmascript-debugger", "5.0");
			versions.put("window-manager", "2.0");
			versions.put("exec", "2.0");
			services = new ScopeServices(versions);
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
			throw new CommunicationException("Unable to open URL because Opera is not connected.");
		
		int activeWindowId = windowManager.getActiveWindowId();
		
		actionHandler.get(url);
		services.waitForWindowLoaded(activeWindowId, timeout);
		switchTo().defaultContent();
		
		return windowManager.getLastHttpResponseCode().getAndSet(0);
	}
	
    // FIXME: Using sleep!
	public void waitForPageLoad(int oldId, long timeout){
            long end = System.currentTimeMillis() + timeout;
            while(debugger.getRuntimeId() == oldId) {
                sleep(OperaIntervals.POLL_INVERVAL.getValue());
                if(System.currentTimeMillis() >= end)
                    break;
            }
            waitForLoadToComplete();
	}

	public String getCurrentUrl() {
            return debugger.executeJavascript("return document.location.href");
	}
	
	public void gc() {
            debugger.releaseObjects();
	}

	public Dimension getDimensions() {
		String[] dimensions = (debugger.executeJavascript("return (window.innerWidth + \",\" + window.innerHeight")).split(",");
		return new Dimension(Integer.valueOf(dimensions[0]), Integer.valueOf(dimensions[1]));
	}

	 //Chris' way
    public String getText(){
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

	public void closeAll() {
		windowManager.closeAllWindows();
		windowManager.filterActiveWindow();
	}

	private void closeWindow() {
		windowManager.closeActiveWindow();
	}

	public void stop() {
		exec.action("Stop");
	}

	public WebElement findElement(By by) {
		return by.findElement((SearchContext) this);
	}

	public List<WebElement> findElements(By by) {
		return by.findElements((SearchContext) this);
	}

	public String getPageSource() {
		return debugger.executeJavascript("return document.documentElement.outerHTML");
	}

	public String getTitle() {
		return debugger.executeJavascript("return document.title;");
	}

	public String getWindowHandle() {
		return String.valueOf(windowManager.getActiveWindowId());
	}

	public Set<String> getWindowHandles() {
		return windowManager.getWindowHandles();
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
			while(findElementsByTagName("frameset").size() > 0) {
				switchTo().frame(0);
			}
			//waitForLoadToComplete();
			return OperaDriver.this;
		}

		public WebDriver frame(int frameIndex) {
			//make sure we execute this one on "_top"
			debugger.resetFramePath();
			int framesLength = Integer.valueOf(debugger.executeJavascript("return document.frames.length"));
			
			//index to id matching
			if(frameIndex < 0 || frameIndex >= framesLength)
				throw new NoSuchFrameException("Invalid frame index : " + frameIndex);
			
			Integer operaId = debugger.getObject("document.frames[" + frameIndex +"].opera");
			
			debugger.changeRuntime(operaId);
			
			//debugger.releaseObject(operaId);
			
			return OperaDriver.this;
		}

		public WebDriver frame(String frameName) {
			Integer id = debugger.getObject("return document.frames['" +  frameName+"']");
			
			if(id == null)
				throw new NoSuchFrameException("Invalid frame name : " + frameName);
			
			debugger.changeRuntime(id);
			return OperaDriver.this;
		}

		public WebDriver window(String windowName) {
			windowManager.setActiveWindow(windowName); //find by title
			defaultContent(); //set runtime to _top
			debugger.executeJavascript("window.focus()", false); //steal focus
			return OperaDriver.this;
		}
		
	}
	
	/**
	 * TODO: Add to official API?
	 * @return list of frames available for chosing
	 */
	public List<String> listFrames(){
		return debugger.listFramePaths();
	}
	
	public WebElement findActiveElement() {
		return findSingleElement("document.activeElement;", "active element");
	}
	

	// TODO Benchmark, XPath is supposed to be faster?
	public WebElement findElementByLinkText(String using) {	
		return findSingleElement("var elements = document.getElementsByTagName('a');\n" +
				"var element = null;\n" +
				"var i = 0;\n" +
				"for(;element = elements[i]; i++) {\n" +
				" if(element.textContent == '"+ using + "'){\n"+
				" return element; }\n"+
				"}", "link text");
	}

	public WebElement findElementByPartialLinkText(String using) {
		return findSingleElement("var elements = document.getElementsByTagName('a');\n" +
				"var element = null;\n" +
				"var i = 0;\n" +
				"for(;element = elements[i]; i++) {\n" +
				"if( element.textContent.indexOf('"+ using + "') > -1 ){\n"+
				"return element; }\n"+
				"} return \"No element found\";", "partial link text");
		
		
	}

	public List<WebElement> findElementsByLinkText(String using) {
		return findMultipleElements("var links = document.links, link = null, i = 0, elements = [];\n"+
				"for( ; link = links[i]; i++)\n"+
				"{\n"+
				"if(link.textContent == '" + using +"')\n"+
				"{\n"+
				"elements.push(link);\n"+
					"}\n"+
				"}\n" +
				"return elements;", "link text");
	}
	
	protected List<WebElement> processElements(Integer id){		
		List<Integer> ids = debugger.examineObjects(id);
		List<WebElement> toReturn = new ArrayList<WebElement>();
		for (Integer objectId : ids)
			toReturn.add(new OperaWebElement(this, objectId));
		return toReturn;
	}
	
	public List<WebElement> findElementsByPartialLinkText(String using) {
		return findMultipleElements("var links = document.links, link = null, i = 0, elements = [];\n" +
				"for( ; link = links[i]; i++)\n" +
				"{\n" +
				"if(link.textContent.indexOf('" + using +"') > -1)\n" +
				"{\n" +
				"elements.push(link);\n" +
					"}\n" +
				"}\n" +
				"return elements;", "partial link text");
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
		long endTime = System.currentTimeMillis() + OperaIntervals.PAGE_LOAD_TIMEOUT.getValue();
		while (!"complete".equals(debugger.executeJavascript("return document.readyState"))) {
			if(System.currentTimeMillis() < endTime) 
				sleep(OperaIntervals.POLL_INVERVAL.getValue());
			else
				throw new WebDriverException("Timeout while loading page");
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
				cookie = new ReturnedCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), new Date(new Date().getTime() + (10 * 365 * 24 * 60 * 60 * 1000)), false);
			debugger.executeJavascript("document.cookie='" + cookie.toString() + "'", false);
		}

		public void deleteCookieNamed(String name) {
			deleteCookie(new ReturnedCookie(name, "", getCurrentHost(), "", null, false));
		}

		public void deleteCookie(Cookie cookie) {
			Date dateInPast = new Date(0);
			Cookie toDelete = new ReturnedCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), dateInPast, false);
			addCookie(toDelete);
		}

		public void deleteAllCookies() {
			Set<Cookie> cookies = getCookies();
			for (Cookie cookie : cookies) {
				deleteCookie(cookie);
			}
		}

		public Set<Cookie> getCookies() {
			String currentUrl = getCurrentHost();

			Set<Cookie> toReturn = new HashSet<Cookie>();
			String allDomainCookies = debugger.executeJavascript("return document.cookie");

			String[] cookies = allDomainCookies.split(";");
			for (String cookie : cookies) {
				String[] parts = cookie.split("=");
				if (parts.length != 2) {
					continue;
				}

				toReturn.add(new ReturnedCookie(parts[0].trim(), parts[1].trim(), currentUrl,"", null, false));
			}

			return toReturn;
		}

		public Speed getSpeed() {
			throw new UnsupportedOperationException("getMouseSpeed");
		}

		public void setSpeed(Speed speed) {
			throw new UnsupportedOperationException("setMouseSpeed");
		}

		private String getCurrentHost() {
			try {
				URL url = new URL(getCurrentUrl());
				return url.getHost();
			} catch (MalformedURLException e) {
				return "";
			}
		}

		public Cookie getCookieNamed(String name) {
			String value = debugger.executeJavascript("var getCookieNamed = function(key)\n"+
														  "{"+
														  "var value = new RegExp(key + \"=([^;]*)\").exec(document.cookie);"+
														  "return value && decodeURIComponent(value[1]);"+
														  "}\n"+
														  "return getCookieNamed('" + name + "')");
			return (value == null) ? null : new Cookie(name, value);
		}
	}
	
	public void operaAction(String using, String... params) {
            
		exec.action(using, params);
	}
	
	public Set<String> getOperaActionList() {
		return exec.getActionList();
	}
	
	/**
	 * @deprecated Don't use sleep!
	 */
	private static void sleep(long timeInMillis) {
		try {
			Thread.sleep(timeInMillis);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public WebElement findElementByTagName(String using) {
            return findSingleElement("document.getElementsByTagName('" + using +"')[0];", "tag name");
	}


	public List<WebElement> findElementsByTagName(String using) {
            return findMultipleElements("document.getElementsByTagName('"+ using + "');\n", "name");
	}

	public WebElement findElementByCssSelector(String using) {
            return findSingleElement("document.querySelector('" + using +"');", "selector");
	}

	public List<WebElement> findElementsByCssSelector(String using) {
            return findMultipleElements("document.querySelectorAll('"+ using + "'), returnValue = [], i=0;for(;returnValue[i]=results[i];i++); return returnValue;", "selector");
	}

	private final List<WebElement> findMultipleElements(String script, String type) {
		Integer id = debugger.getObject(script);

		if (id == null) {
			throw new NoSuchElementException("Cannot find element(s) with " + type);
		}
		return processElements(id);
	}
	
	private final WebElement findSingleElement(String script, String type) {
            Integer id = debugger.getObject(script);

            if (id != null) {
                    return new OperaWebElement(this, id);
            }

            throw new NoSuchElementException("Cannot find element with " + type);
	}


	public void saveScreenshot(File pngFile) {
            actionHandler.saveScreenshot(pngFile);
	}

	public String saveScreenshot(String fileName, int timeout, String... hashes) {		
            return screenWatcher(fileName, timeout, true, hashes);
	}
	
	public ScreenShotReply saveScreenShot(Canvas canvas, long timeout, boolean includeImage, String... hashes) {		
            return exec.screenWatcher(canvas, timeout, includeImage, hashes);
	}
	
	
	private String screenWatcher(String fileName, int timeout, boolean saveFile, String... hashes){
            Canvas canvas = new Canvas();
            canvas.setX(0);
            canvas.setY(0);

            String[] dimensions = debugger.executeJavascript("return (window.innerWidth + \",\" + window.innerHeight);").split(",");
            canvas.setH(Integer.valueOf(dimensions[1]));
            canvas.setW(Integer.valueOf(dimensions[0]));
            canvas.setViewPortRelative(true);

            ScreenShotReply screenshot = exec.screenWatcher(canvas, timeout, saveFile, hashes);
            if(saveFile && screenshot.getPng() != null){
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
                if(result.getClassName().equals("Array"))
                    return processObjects(objectId);
            }
            return object;
	}

	protected List<Object> processObjects(Integer id) {
            List<Integer> ids = debugger.examineObjects(id);
            List<Object> toReturn = new ArrayList<Object>();
            for (Integer objectId : ids)
                toReturn.add(debugger.callFunctionOnObject("locator", objectId, true));
            return toReturn;
	}
	
	public boolean isJavascriptEnabled() {
            // FIXME we always assume it is true
            // TODO it should not be possible to register esdbg if js is disabled?
            return true;
	}

	@Deprecated
	public void cleanUp() {
		services.close();
	}

	public void executeActions(OperaAction action) {
            List<UserInteraction> actions = action.getActions();
            for (UserInteraction userInteraction : actions) {
                userInteraction.execute(this);
            }
            waitForLoadToComplete();
	}
	
	/**
	 * @deprecated This should not be used!
	 */
	@Deprecated
	public boolean isConnected() {
		return services.isConnected();
	}
	
	public void key(String key) {
		keyDown(key);
		keyUp(key);
		
		if(key.equalsIgnoreCase("enter")) {
			sleep(OperaIntervals.EXEC_SLEEP.getValue());
			waitForLoadToComplete();
		}
	}

	public void keyDown(String key) {
            exec.key(key, false);
	}

	public void keyUp(String key) {
            exec.key(key, true);
	}

	public void releaseKeys() {
            exec.releaseKeys();
	}

	public void type(String using) {
            exec.type(using);
	}
	
	public void mouseEvent(int x, int y, int value) {
            exec.mouseAction(x, y, value, 1);
	}
	
	public void addConsoleListener(IConsoleListener listener) {
            services.addConsoleListener(listener);
	}

    public void binaryStopped(int code) {
        services.onBinaryStopped(code);
    }

}
