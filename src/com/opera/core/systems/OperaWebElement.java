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
import java.awt.Point;
import java.io.FileOutputStream;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.internal.Locatable;

import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ColorResult;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.model.ScriptResult;
import com.opera.core.systems.scope.internal.OperaColors;
import com.opera.core.systems.scope.internal.OperaFlags;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.OperaKeys;
import com.opera.core.systems.scope.internal.OperaMouseKeys;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;

public class OperaWebElement implements RenderedWebElement, SearchContext, Locatable, FindsByTagName, FindsByLinkText, FindsByClassName, FindsByXPath, FindsByName, FindsById, FindsByCssSelector {

	
	private final int objectId;
    private final IEcmaScriptDebugger debugger;
	private final OperaDriver parent;
	private final int runtimeId;

	private final IOperaExec execService;
	
	public int getObjectId() {
		return objectId;
	}
	
	public int getRuntimeId() {
		return runtimeId;
	}
	
    public OperaWebElement(OperaDriver parent, int objectId) {
        this.parent = parent;
        this.objectId = objectId;
        
        debugger = parent.getScriptDebugger();
        execService = parent.getExecService();
        this.runtimeId = debugger.getRuntimeId();
    }
    
    /**
     * Calls the method and parses the result, the result
     * must be a string
     * @param using
     * @return response of ecmascript in string presentation
     */
    private final String callMethod(String using) {
    	return debugger.callFunctionOnObject(using, objectId);
    }
    
    /**
     * Executes the given script with the element's object id
     * doesn't parse the response
     * @param using
     */
    private final void executeMethod(String using) {
    	debugger.callFunctionOnObject(using, objectId, false);
    }
    
    /**
     * Evals the given script with object id and parses the
     * result and returns the result object
     * @param using
     * @return {@link ValueData}
     */
    private final Object evaluateMethod(String using) {
    	return debugger.callFunctionOnObject(using, objectId, true);
    }
    
	public void clear() {
    	executeMethod("locator.focus()");
    	//FIXME testClearingAnElementShouldCauseTheOnChangeHandlerToFire
    	//execService.action("Clear");
    	execService.action("Select all");
    	execService.action("Delete");
    	executeMethod("locator.blur()");
	}
	
	public void rightClick() {
		parent.actionHandler.rightClick(this);
	}
	
	//FIXME these methods are not available in public builds
	public void hover() {
		Point point = this.getLocation();
		execService.mouseAction(point.x, point.y);
	}
	
	public void mouseOut() {
		Point point = this.getLocation();
		execService.mouseAction(point.x, point.y);
		execService.mouseAction(0, 0);
	}
	
	public void mouseDown() {
		Point point = this.getLocation();
		execService.mouseAction(point.x, point.y, OperaMouseKeys.LEFT_DOWN);
	}
	
	//TODO add check if mouse not down?
	public void mouseUp() {
		Point point = this.getLocation();
		execService.mouseAction(point.x, point.y, OperaMouseKeys.LEFT_DOWN);
	}
	
	public void click(int times) {
            Point point = this.getLocation();
            execService.mouseAction(point.x, point.y, OperaMouseKeys.LEFT.getValue(), times);
	}
	
	public void middleClick() {
		Point point = this.getLocation();
		execService.mouseAction(point.x, point.y, OperaMouseKeys.MIDDLE);
	}
	
	public void click() {
		if(OperaFlags.ENABLE_CHECKS) {
			if(!isDisplayed())
				throw new ElementNotVisibleException("You can't click an element that is not displayed");
		}
		parent.actionHandler.click(this, "");
		//workaround for click synchronization problems
		sleep(OperaIntervals.EXEC_SLEEP.getValue());
		parent.waitForLoadToComplete();
		//TODO this one must be tested throughly
		//parent.gc();
	}
	
	public void click(int x, int y) {
		parent.actionHandler.click(this, x, y);
		parent.waitForLoadToComplete();
	}

	public WebElement findElement(By by) {
		return by.findElement(this);
	}

	public List<WebElement> findElements(By by) {
		return by.findElements(this);
	}

	public String getAttribute(String name) {
		String attribute = name.toLowerCase();
		if(attribute.equals("disabled")) {
			return String.valueOf(!isEnabled());
		} else if(attribute.equals("checked") || attribute.equals("selected")) {
			return String.valueOf(isSelected());
		} else if(attribute.equals("index")) {
			return callMethod("locator.index");
		}
		
		return callMethod("locator.getAttribute('" + attribute + "')");
	}

	public String getElementName() {
		return callMethod("locator.nodeName");
	}

	public String getText() {
		return callMethod("return locator.textContent");
	}

	
	public String getValue(){
		return callMethod("if(/^input|select|textarea$/i.test(locator.nodeName)){"+
												"return locator.value;"+
	 											"}"+ 
	 											"return locator.textContent;");
	}
	
	
	public boolean isEnabled() {
		return (Boolean) evaluateMethod("return !locator.disabled");
	}

	
	public boolean isSelected() {
		return (Boolean) evaluateMethod("return locator.checked != null ? locator.checked : locator.selected != null ? locator.selected : false;");
	}
	
	public boolean isVisible(){
		return isDisplayed();//(Boolean) evaluateMethod("return (locator.offsetWidth > 0)"); 
	}
	
	public void sendKeys(CharSequence... keysToSend) {
		if(OperaFlags.ENABLE_CHECKS) {
			if(!isDisplayed())
				throw new ElementNotVisibleException("You can't type on an element that is not displayed");
		}
		
		executeMethod("locator.focus()");
        for (CharSequence seq : keysToSend) {
        	if(seq instanceof Keys)
        		execService.key(OperaKeys.get(((Keys)seq).name()));
        	else
        		execService.type(seq.toString());
        }
        
        	executeMethod("locator.blur()");
	}

	//FIXME isDisplayed is not working for select elements, revise
	public void setSelected() {
		if (OperaFlags.ENABLE_CHECKS) {
			if (!isEnabled())
				throw new UnsupportedOperationException("Can't selected disabled elements");
		}

		if (getTagName().equalsIgnoreCase("option")) {
			Integer id = debugger.executeScriptOnObject("return locator.parentNode", objectId);
			OperaWebElement parentNode = new OperaWebElement(this.parent, id.intValue());
			if (parentNode.getAttribute("multiple") == null) {
				if (OperaFlags.ENABLE_CHECKS) {
					if (!parentNode.isDisplayed())
						throw new ElementNotVisibleException("You can't select an element that is not displayed");
				}

				// find all nodes
				Integer index = Integer.valueOf(getAttribute("index"));
				// reset prior to sending
				parentNode.sendKeys(Keys.HOME);
				List<WebElement> elements = parentNode.findElementsByTagName("option");
				for (int i = 0; i < index; i++) {
					// down only if enabled or target element
					if (elements.get(i).isEnabled() || elements.get(i).equals(this))
						parentNode.sendKeys(Keys.DOWN);
				}
			} else {
				debugger.callFunctionOnObject("locator.selected = true;", objectId, false);
			}
		} else {// if(getTagName().equalsIgnoreCase("input")) {
			click();
		}
	}

	public void submit() {
		OperaWebElement element = (OperaWebElement) parent.executeScript("element = arguments[0];" + 
							"while (element.parentNode != null && element.tagName.toLowerCase() != \"form\") {" + 
							"element = element.parentNode;" +
							"} return element;", this);
		ScriptResult result = (ScriptResult) debugger.scriptExecutor("return arguments[0];", element);
		
		if(result.getClassName().equals("HTMLFormElement"))
			debugger.callFunctionOnObject("locator.submit()", element.getObjectId(), false);
		else throw new WebDriverException("Element is not in a form, can't submit");
		
		parent.waitForLoadToComplete();
		
	}

	//FIXME  revise with javascript guys
	public boolean toggle() {
		if(OperaFlags.ENABLE_CHECKS) {
			if(!isDisplayed())
				throw new ElementNotVisibleException("You can't toggle an element that is not displayed");
		}
		Integer id = debugger.executeScriptOnObject("return locator.parentNode", objectId);
		OperaWebElement parentNode = new OperaWebElement(this.parent, id);
		if(parentNode.getTagName().equalsIgnoreCase("SELECT") && parentNode.getAttribute("multiple") == null) {
			throw new UnsupportedOperationException("You can't toggle on a regular select");
		}
		if(getTagName().equalsIgnoreCase("option"))
			return (Boolean) debugger.callFunctionOnObject("locator.selected = !locator.selected; return locator.selected;", objectId, true);
		
		return (Boolean) debugger.callFunctionOnObject("locator.checked = !locator.checked; return locator.checked;", objectId, true);
	}
	
	public static void sleep(long timeInMillis) {
		try {
			Thread.sleep(timeInMillis);
		} catch (InterruptedException e) {
			//ignore
		}
	}

	public void dragAndDropBy(int x, int y) {
		Point point = this.getLocation();
		execService.mouseAction(point.x, point.y, OperaMouseKeys.LEFT_DOWN);
		x = (x + point.x < 0) ? 0 : x + point.x;
		y = (y + point.y < 0) ? 0 : y + point.y;
		execService.mouseAction(x, y);
		execService.mouseAction(x, y, OperaMouseKeys.LEFT_UP);
	}

	public void dragAndDropOn(RenderedWebElement element) {
		Point currentLocation = this.getLocation();
		Point dragPoint = element.getLocation();
		execService.mouseAction(currentLocation.x, currentLocation.y, OperaMouseKeys.LEFT_DOWN);
		execService.mouseAction(dragPoint.x, dragPoint.y);
		execService.mouseAction(dragPoint.x, dragPoint.y, OperaMouseKeys.LEFT_UP);
	}
	
	/**
	 * Click top left, can be modified to click in the middle
	 */
	public Point getLocation() {
		String coordinates = debugger.callFunctionOnObject("locator.scrollIntoView();\n" +
				"var x = 0, y = 0, curr = window.frameElement;\n" + 
				"while(curr) {\n" +
				"x+= curr.getBoundingClientRect().left + curr.clientLeft;\n" +
				"y+= curr.getBoundingClientRect().top + curr.clientTop;\n" + 
				"curr = window.parent.frameElement;\n"+
				"}\n" +
				"return ((x + window.scrollX + locator.getBoundingClientRect().left) + ',' + (y+ window.scrollY + locator.getBoundingClientRect().top));\n", objectId);
		String[] location = coordinates.split(",");
		return new Point(Integer.valueOf(location[0]), Integer.valueOf(location[1]));
	}

	public Dimension getSize() {
		String widthAndHeight = debugger.callFunctionOnObject("return (locator.getBoundingClientRect().width + ',' + locator.getBoundingClientRect().height);", objectId);
		String[] dimension = widthAndHeight.split(",");
		return new Dimension(Integer.valueOf(dimension[0]), Integer.valueOf(dimension[1]));
	}

	public String getValueOfCssProperty(String property) {
		return callMethod("return getComputedStyle(locator,null)." + property + ";");
	}

	public boolean isDisplayed() {
		boolean isDisplayed = false;
		try {
		isDisplayed = (Boolean) evaluateMethod("var el = locator;\n"
				+ "while (el.nodeType != 1 && !(el.nodeType >= 9 && el.nodeType <= 11)) {\n"
				+ "el = el.parentNode;\n"
				+ "}\n"
				+ "\n"
				+ "if (!el) {\n"
				+ "return false;\n"
				+ "}\n"
				+ "\n"
				+ "if (el.tagName.toLowerCase() == 'input' && el.type == 'hidden') {\n"
				+ " return false; \n"
				+ " }\n"
				+ "if(el.tagName.toLowerCase() == 'option') {\n"
				+ "return (el.parentNode == document.activeElement);\n"
				+"}\n"
				+ "\n"
				+ "el.scrollIntoView();\n" 
				+ "var box = el.getBoundingClientRect();\n"
				+ "if (box.width == 0 || box.height == 0) {\n"
				+ "return false;\n"
				+ "}\n"
				+ "var visibility = el.ownerDocument.defaultView.getComputedStyle(el, null).getPropertyValue('visibility');\n"
				+ "\n"
				+ "var _isDisplayed = function(e) {\n"
				+ " var display = e.ownerDocument.defaultView.getComputedStyle(e, null).getPropertyValue('display');\n"
				+ "if (display == 'none') return display;\n"
				+ " if (e && e.parentNode && e.parentNode.style) {\n"
				+ " return _isDisplayed(e.parentNode);\n" + "}\n"
				+ "return undefined;\n" + "};\n" + "\n"
				+ "var displayed = _isDisplayed(el);\n" + "\n"
				+ "return displayed != 'none' && visibility != 'hidden';");
		} catch (WebDriverException ex) {
			throw new StaleElementReferenceException("This element is stale");
		}
		
		return isDisplayed;
	}

	public String getImageHash() {
		return getImageHash(10l);
	}
	
	public String getImageHash(long timeout, String... hashes) {
		return saveScreenshot("", timeout, false, hashes);
	}
	
	public String saveScreenshot(String filename){
		return this.saveScreenshot(filename, 10l, true);
	}
	
	public String saveScreenshot(String filename,long timeout){
		return this.saveScreenshot(filename, timeout, true);
	}
	
	public String saveScreenshot(String filename, long timeout, boolean includeImage, String... hashes){
		Canvas canvas = buildCanvas();
        ScreenShotReply reply = execService.screenWatcher(canvas, timeout, includeImage, hashes);
        if(includeImage && reply.getPng() != null) {
			FileOutputStream stream;
			try {
				stream = new FileOutputStream(filename);
				stream.write(reply.getPng());
				stream.close();
			} catch (Exception e) {
				throw new WebDriverException("Failed to write file: " + e.getMessage());
			}
        }
		return reply.getMd5();
	}

	
	public boolean containsColor(OperaColors... colors){
		//List<String> keys = Arrays.asList(hashes);
		
		Canvas canvas = buildCanvas();
        ScreenShotReply reply = execService.containsColor(canvas, 100l, colors);
        List<ColorResult> results = reply.getColorResult();
        for (ColorResult result : results) {
			if(result.getCount() > 0)
				return true;
		}
        return false;
	}

	
	private Canvas buildCanvas() {
		Canvas canvas = new Canvas();
		Dimension dimension = getSize();
		Point point = getLocation();
		/*
		String[] areaCoordinates = callMethod("var oElement = locator;\n" +
			    "var posX = 0, posY = 0;\n" +
			    "do{\n" +
			    "posX += oElement.offsetLeft;\n" +
			    "posY += oElement.offsetTop;\n" +
			    "} while(oElement = oElement.offsetParent );\n" +
			    "return (posX + ',' + posY + ',' + locator.offsetWidth + ',' + locator.offsetHeight);\n").split(",");
			    */
		int x = point.x;
		int y = point.y;
		int w = dimension.width;
		int h = dimension.height;
		canvas.setX(x);
		canvas.setY(y);
		canvas.setH(h);
		canvas.setW(w);
		canvas.setViewPortRelative(true);
		return canvas;
	}
	
	public String getTagName() {
		return callMethod("return (locator.tagName);");
	}

	//TODO we only return location on screen when scrolled?
	//isnt this a duplicate method?
	public Point getLocationOnScreenOnceScrolledIntoView() {
		if(isVisible())
			return getLocation();
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof OperaWebElement) {
			OperaWebElement ref = (OperaWebElement) obj;
			return (ref.getObjectId() == this.getObjectId() && ref.getRuntimeId() == this.getRuntimeId());
		}
		return false;
	}
	
	//FIXME can be more than one
	@Override
	public int hashCode() {
		return (objectId ^ runtimeId);
	}
	
	@Override
	public String toString() {
		return "element" + hashCode();
	}


	private WebElement findSingleElement(String using, String type) {
		Integer id = debugger.executeScriptOnObject(using, objectId);
		if(id != null)
			return new OperaWebElement(parent, id);
		
		throw new NoSuchElementException("Cannot find element with " + type);
	}
	
	private final List<WebElement> findMultipleElements(String using, String type) {
		Integer id = debugger.executeScriptOnObject(using, objectId);
		if(id == null)
			throw new NoSuchElementException("Cannot find element(s) with " + type);
		
		return parent.processElements(id);
	}
	
	public WebElement findElementByLinkText(String using) {
		return findSingleElement("var elements = locator.getElementsByTagName('a'), element = null, i = 0;\n" +
				"for(;element = elements[i]; i++) {\n" +
				" if(element.textContent == '"+ using + "'){\n"+
				" return element; }\n"+
				"}", "link text");
	}

	public WebElement findElementByPartialLinkText(String using) {
		return findSingleElement("var elements = locator.getElementsByTagName('a'), element = null, i = 0;\n" +
				"for(;element = elements[i]; i++) {\n" +
				" if(element.textContent.indexOf('" + using + "') > -1){\n"+
				" return element; }\n"+
				"}", "partial link text");
	}

	public List<WebElement> findElementsByLinkText(String using) {
		return findMultipleElements("var links = locator.getElementsByTagName('a'), link = null, i = 0, ret = [];"+
			    "for( ; link = links[i]; i++)"+
			    "{"+
			      "if(link.textContent == '" + using + "')"+
			      "{"+
			        "ret.push(link);"+
			      "}"+
			    "}"+
			    "return ret;", "link text");	
	}

	public List<WebElement> findElementsByPartialLinkText(String using) {
		return findMultipleElements("var links = locator.getElementsByTagName('a'), link = null, i = 0, ret = [];"+
			    "for( ; link = links[i]; i++)"+
			    "{"+
			      "if(link.textContent.indexOf('" + using + "') > -1 )"+
			      "{"+
			        "ret.push(link);"+
			      "}"+
			    "}"+
			    "return ret;", "link text");	
	}
	
	public WebElement findElementByTagName(String using) {
		return findSingleElement("locator.getElementsByTagName('" + using +"')[0]", "tag name");
	}

	public List<WebElement> findElementsByTagName(String using) {
		return findMultipleElements("locator.getElementsByTagName('" + using +"')", "tag name");
	}


	public WebElement findElementByClassName(String using) {
		return findSingleElement("locator.getElementsByClassName('" + using +"')[0]", "class name");
	}

	public List<WebElement> findElementsByClassName(String using) {
		return findMultipleElements("locator.getElementsByClassName('" + using +"')", "class name");
	}

	public WebElement findElementByXPath(String using) {
		return findSingleElement("document.evaluate('" + using +"', locator, null, XPathResult.FIRST_ORDERED_NODE_TYPE,  null).singleNodeValue;\n", "XPath");
	}

	public List<WebElement> findElementsByXPath(String using) {
		return findMultipleElements("var result = document.evaluate(\""
						+ using
						+ "\", locator, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n"
						+ "var elements = new Array();\n"
						+ "var element = result.iterateNext();\n"
						+ "while (element) {\n" + "  elements.push(element);\n"
						+ "  element = result.iterateNext();\n" + "}\n"
						+ "return elements", "XPath");
	}

	public WebElement findElementByName(String using) {
		return findSingleElement("document.evaluate(\"descendant-or-self::*[@name='" + using + "']\",locator,null,XPathResult.ORDERED_NODE_ITERATOR_TYPE,null).iterateNext()", "name");
	}

	public List<WebElement> findElementsByName(String using) {
		return findMultipleElements("var result = document.evaluate"
				+"(\"descendant-or-self::*[@name='" + using + "']\"" 
				+", locator, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n"
				+ "var elements = new Array();\n"
				+ "var element = result.iterateNext();\n"
				+ "while (element) {\n" + "  elements.push(element);\n"
				+ "  element = result.iterateNext();\n" + "}\n"
				+ "return elements", "XPath");
	}

	public WebElement findElementById(String using) {
		return findSingleElement("document.evaluate(\"descendant-or-self::*[@id='" + using + "']\",locator,null,XPathResult.ORDERED_NODE_ITERATOR_TYPE,null).iterateNext()", "id");
	}

	public List<WebElement> findElementsById(String using) {
		return findMultipleElements("var result = document.evaluate"
				+"(\"descendant-or-self::*[@id='" + using + "']\"" 
				+", locator, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n"
				+ "var elements = new Array();\n"
				+ "var element = result.iterateNext();\n"
				+ "while (element) {\n" + "  elements.push(element);\n"
				+ "  element = result.iterateNext();\n" + "}\n"
				+ "return elements", "XPath");
	}

	public WebElement findElementByCssSelector(String using) {
		return findSingleElement("locator.querySelector('" + using +"');", "selector");
	}

	public List<WebElement> findElementsByCssSelector(String using) {
		return findMultipleElements("var results = locator.querySelectorAll('"+ using + "'), returnValue = [], i=0;for(;returnValue[i]=results[i];i++); return returnValue;", "selector");
	}
	
	@Override
	protected void finalize() throws Throwable {
		debugger.releaseObject(objectId);
		super.finalize();
	}
}
