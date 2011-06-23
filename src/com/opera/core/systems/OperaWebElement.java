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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.FindsByClassName;
import org.openqa.selenium.internal.FindsByCssSelector;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByLinkText;
import org.openqa.selenium.internal.FindsByName;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.internal.WrapsDriver;

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

/**
 * Extends the default WebElement with Opera specific methods.
 *
 * @author Deniz Turkoglu
 *
 */
public class OperaWebElement implements WebElement, SearchContext, Locatable,
    FindsByTagName, FindsByLinkText, FindsByClassName, FindsByXPath,
    FindsByName, FindsById, FindsByCssSelector, WrapsDriver {

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
    parent.objectIds.add(objectId);

    debugger = parent.getScriptDebugger();
    execService = parent.getExecService();
    this.runtimeId = debugger.getRuntimeId();
  }

  /**
   * Calls the method and parses the result, the result must be a string
   *
   * @param using
   * @return response of ecmascript in string presentation
   */
  public final String callMethod(String using) {
    return debugger.callFunctionOnObject(using, objectId);
  }

  /**
   * Executes the given script with the element's object id doesn't parse the
   * response
   *
   * @param using
   */
  private final void executeMethod(String using) {
    debugger.callFunctionOnObject(using, objectId, false);
  }

  /**
   * Evals the given script with object id and parses the result and returns the
   * result object
   *
   * @param using
   * @return {@link ValueData}
   */
  private final Object evaluateMethod(String using) {
    return debugger.callFunctionOnObject(using, objectId, true);
  }

  public void clear() {
    executeMethod("locator.focus()");
    // FIXME testClearingAnElementShouldCauseTheOnChangeHandlerToFire
    // execService.action("Clear");
    execService.action("Select all");
    execService.action("Delete");
    executeMethod("locator.blur()");
  }

  /**
   * Right clicks on the element.
   */
  public void rightClick() {
    parent.actionHandler.rightClick(this);
  }

  /**
   * @deprecated Use {@link #mouseOver()}
   */
  @Deprecated
  public void hover() {
    mouseOver();
  }

  public void mouseOver() {
    Point point = this.getLocation();
    execService.mouseAction(point.x, point.y);
  }

  /**
   * Moves the mouse from the top left of the element to the top left of the
   * page, generating a mouseOut event.
   */
  public void mouseOut() {
    Point point = this.getLocation();
    execService.mouseAction(point.x, point.y);
    execService.mouseAction(0, 0);
  }

  /**
   * Presses the left mouse button down on the top left of the element.
   */
  public void mouseDown() {
    Point point = this.getLocation();
    execService.mouseAction(point.x, point.y, OperaMouseKeys.LEFT_DOWN);
  }

  // TODO add check if mouse not down?
  /**
   * Releases the left mouse button at the top left of the element.
   */
  public void mouseUp() {
    Point point = this.getLocation();
    execService.mouseAction(point.x, point.y, OperaMouseKeys.LEFT_DOWN);
  }

  /**
   * Click this element many times.
   *
   * @param times The number of times to click
   */
  public void click(int times) {
    Point point = this.getLocation();
    execService.mouseAction(point.x, point.y, OperaMouseKeys.LEFT.getValue(),
        times);
  }

  /**
   * Click the middle mouse button at the top left of the element.
   */
  public void middleClick() {
    Point point = this.getLocation();
    execService.mouseAction(point.x, point.y, OperaMouseKeys.MIDDLE);
  }

  public void click() {
    if (OperaFlags.ENABLE_CHECKS) {
      if (!isDisplayed()) throw new ElementNotVisibleException(
          "You can't click an element that is not displayed");
    }

		parent.getScopeServices().captureOperaIdle();

		// FIXME: temporary fix for setSelected deprecation
		if (this.getTagName().equals("OPTION")) {
		  setSelected();
		} else {
		  parent.actionHandler.click(this, "");
		}
    parent.waitForLoadToComplete();
  }

  /**
   * Click the element at the given x, y offset from the top left.
   *
   * @param x The distance from the left to click
   * @param y The distance from the top to click
   */
  public void click(int x, int y) {
		parent.getScopeServices().captureOperaIdle();
    parent.actionHandler.click(this, x, y);
    parent.waitForLoadToComplete();
  }

  public WebElement findElement(By by) {
    return by.findElement(this);
  }

  public List<WebElement> findElements(By by) {
    return by.findElements(this);
  }

  public String getAttribute(String attribute) {

    if (!parent.objectIds.contains(objectId)) throw new StaleElementReferenceException(
        "You cant interact with stale elements");

    String lcAttribute = attribute.toLowerCase();
    if (lcAttribute.equals("disabled")) {
      return String.valueOf(!isEnabled());
    } else if (lcAttribute.equals("checked") || lcAttribute.equals("selected")) {
      boolean selected = isSelected();
      if (!selected) return null;
      return String.valueOf(selected);
    } else if (lcAttribute.equals("index")) {
      return callMethod("locator.index");
    }

    if (lcAttribute.equals("value")) {
      return callMethod("if(/^input|select|textarea$/i.test(locator.nodeName)){"+
          "return locator.value;"+
           "}"+
           "return locator.textContent;");
    } else {
      return callMethod("locator.getAttribute('" + attribute + "')");
    }
  }

  /**
   * Get the tag name of this element.
   *
   * @return The tag name in upper-case
   */
  public String getElementName() {
    return callMethod("locator.nodeName");
  }

  public String getText() {
        return callMethod("function getVisibleContents(node, checkDisplay, trimmed, preformatted_parent) {\n"+
                "var s = '';\n"+
                "var trim = /^\\s+|\\s+$/g;\n"+
                "var forbidden = /^(head|script|title|style|select|textarea)$/ig;\n"+
                "\n"+
                "preformatted_parent = preformatted_parent || false;\n"+
                "\n"+
                "if (node.nodeType == Node.TEXT_NODE || node.nodeType == Node.CDATA_SECTION_NODE)\n"+
                    "return trimmed ? node.nodeValue.replace(trim, '') : node.nodeValue;\n"+
                    "\n"+
                "if (node.nodeType == Node.DOCUMENT_POSITION_DISCONNECTED)\n"+
                    "if (node.nodeName.match(forbidden) || (checkDisplay && getComputedStyle(node).display == 'none'))\n"+
                        "return s;\n"+
                        "\n"+
                "if (node.nodeType == Node.DOCUMENT_POSITION_DISCONNECTED\n"+
                    "|| node.nodeType == Node.DOCUMENT_NODE\n"+
                    "|| node.nodeType == Node.DOCUMENT_FRAGMENT_NODE)\n"+
                "{\n"+
                    "if (getComputedStyle(node).whiteSpace != 'normal')\n"+
                        "preformatted_parent = true;\n"+
                        "\n"+
                    "for (var i = 0, n; n = node.childNodes[i]; i++)\n"+
                    "{\n"+
                        "var n_s = getVisibleContents(n, checkDisplay, false, preformatted_parent);\n"+
                        "\n"+
                        // Parent element with non pre formatting. Remove all duplicated white-spaces
                        "if (n.nodeType == Node.TEXT_NODE && !preformatted_parent)\n"+
                            "n_s = n_s.replace(/\\s+/g, ' ');\n"+
                            "\n"+
        // Wrap with newlines if this is block element.
                        "if (n.nodeType == Node.ELEMENT_NODE && getComputedStyle(n).display == 'block')\n"+
                            "s += '\\n' + n_s + '\\n';\n"+
                        "else if (n.tagName == 'BR')\n"+
                            "s += '\\n';\n"+
                        "else if (s.substr(-1) == ' ' && n_s.substr(0, 1) == ' ')\n"+
                            "s += n_s.substr(1);\n"+
                        "else\n"+
                            "s += n_s;\n"+
                    "}\n"+
                "}\n"+
                "\n"+
                "return trimmed ? s.replace(trim, '') : s;\n"+
            "}\n"+
            "if(locator.tagName.toLowerCase() == 'title') document.title;\n"+
            "else getVisibleContents(locator, true, true).replace(/(\\r*\\n)+/g, '\\n');\n");
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

  /**
   * @deprecated Use {@link #isDisplayed()} instead
   */
  @Deprecated
  public boolean isVisible() {
    return isDisplayed();// (Boolean)
    // evaluateMethod("return (locator.offsetWidth > 0)");
  }

  public void sendKeys(CharSequence... keysToSend) {
    // A list of keys that should be held down, instead of pressed
    ArrayList<String> holdKeys = new ArrayList<String>();
    holdKeys.add(OperaKeys.SHIFT.getValue());
    holdKeys.add(OperaKeys.CONTROL.getValue());
    // Keys that have been held down, and need to be released
    ArrayList<String> heldKeys = new ArrayList<String>();

    if (OperaFlags.ENABLE_CHECKS) {

      long start = System.currentTimeMillis();
      boolean isDisplayed = false;

      do {
        isDisplayed = isDisplayed();

        if (!isDisplayed && parent.hasTimeRemaining(start)) sleep(OperaIntervals.EXEC_SLEEP.getValue());
        else break;

      } while (true);

      if (!isDisplayed()) throw new ElementNotVisibleException(
          "You can't type on an element that is not displayed");
      if (!isEnabled()) throw new InvalidElementStateException(
          "You can't type on an element that is disabled");
    }

    if (getTagName().equalsIgnoreCase("input")
        && (hasAttribute("type") && getAttribute("type").equals("file"))) {
      click();
    } else executeMethod("locator.focus()");

    // This code is a bit ugly. Because "special" keys can be sent either as
    // an individual argument, or in the middle of a string of "normal"
    // characters, we have to loop through the string and check each against
    // a list of special keys.

		parent.getScopeServices().captureOperaIdle();
    for (CharSequence seq : keysToSend) {
      if (seq instanceof Keys) {
        String key = OperaKeys.get(((Keys) seq).name());
        // Check if this is a key we hold down, and haven't already pressed,
        // and press, but don't release it. That's done at the end of this
        // method.
        if (holdKeys.contains(key) && !heldKeys.contains(key) && !execService.keyIsPressed(key)) {
          execService.key(key, false);
          heldKeys.add(key);
        } else if (key == "null") {
          for (String hkey : heldKeys) {
            execService.key(hkey, true);
          }
        } else {
          execService.key(key);
        }
      }
      else if (seq.toString().equals("\n")) {
        execService.key("enter");
      }
      else {
        // We need to check each character to see if it is a "special" key
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < seq.length(); i++) {
          Character c = seq.charAt(i);
          String keyName = charToKeyName(c);

          // Buffer normal keys for a single type() call
          if (keyName == null) {
            buffer.append(c.toString());
          }
          else {
            // This is a special key, so send all buffered normal keys
            if (buffer.length() > 0) {
              execService.type(buffer.toString());
              buffer.delete(0, buffer.length());
            }

            String key = OperaKeys.get(keyName);
            // FIXME: Code repeated from above.
            if (holdKeys.contains(key) && !heldKeys.contains(key) && !execService.keyIsPressed(key)) {
              execService.key(key, false);
              heldKeys.add(key);
            } else if (key == "null") {
              for (String hkey : heldKeys) {
                execService.key(hkey, true);
              }
            } else {
              execService.key(key);
            }
          }
        }

        // send any remaining buffered keys
        if (buffer.length() > 0) {
          execService.type(buffer.toString());
        }
      }
    }

    if (heldKeys.size() > 0) {
      for (String key : heldKeys) {
        execService.key(key, true);
      }
    }

    parent.waitForLoadToComplete();
    // executeMethod("locator.blur()");
  }

  private static final HashMap<Character, String> keysLookup = new HashMap<Character, String>();
  /**
   * Converts a character in the PUA to the name of the key, as given by
   * {@link org.openqa.selenium.Keys}. If the character doesn't appear in that
   * class then null is returned.
   * @param c The character that may be a special key.
   * @return A string containing the name of the "special" key or null.
   */
  private static String charToKeyName(char c) {
    if (keysLookup.isEmpty()) {
      for (Keys k : Keys.values()) {
        keysLookup.put(k.charAt(0), k.name());
      }
    }
    return keysLookup.get(c);
  }

  private boolean hasAttribute(String attr) {
    return getAttribute(attr) != null;
  }

  // FIXME isDisplayed is not working for select elements, revise
  /**
   * @deprecated Please use "click" instead
   */
  @Deprecated
  public void setSelected() {
    String tagName = getTagName();

    if (OperaFlags.ENABLE_CHECKS) {
      if (!isEnabled())
        throw new InvalidElementStateException("Cannot select disabled element");

      if (!isDisplayed())
        throw new ElementNotVisibleException("Cannot select an element that is not displayed");

      if (!tagName.equals("INPUT") && !tagName.equals("OPTION"))
        throw new InvalidElementStateException("Cannot select a "+tagName+" element");
    }

    if (tagName.equalsIgnoreCase("option")) {
      Integer id = debugger.executeScriptOnObject("return locator.parentNode",
          objectId);
      OperaWebElement parentNode = new OperaWebElement(this.parent,
          id.intValue());

			parent.getScopeServices().captureOperaIdle();
      if (parentNode.getAttribute("multiple") == null) {
        if (OperaFlags.ENABLE_CHECKS) {
          if (!parentNode.isDisplayed()) throw new UnsupportedOperationException(
              "You can't select an element that is not displayed");
        }

        // find all nodes
        Integer index = Integer.valueOf(getAttribute("index"));
        // reset prior to sending
        parentNode.sendKeys(Keys.HOME);
        List<WebElement> elements = parentNode.findElementsByTagName("option");
        for (int i = 0; i < index; i++) {
          // down only if enabled or target element
          if (elements.get(i).isEnabled() || elements.get(i).equals(this)) parentNode.sendKeys(Keys.DOWN);
        }
      } else {
        debugger.callFunctionOnObject("locator.selected = true;", objectId,
            false);
      }

      parent.waitForLoadToComplete();
    } else {// if(getTagName().equalsIgnoreCase("input")) {
      if (!isSelected()) click();
    }
  }

  public void submit() {
    OperaWebElement element = (OperaWebElement) parent.executeScript("element = arguments[0];" +
              "while (element.parentNode != null && element.tagName.toLowerCase() != \"form\") {" +
              "element = element.parentNode;" +
              "} return element;", this);
    ScriptResult result = (ScriptResult) debugger.scriptExecutor("return arguments[0];", element);

		parent.getScopeServices().captureOperaIdle();
    if(result.getClassName().equals("HTMLFormElement")) {
      debugger.callFunctionOnObject("if(locator.onsubmit == undefined || locator.onsubmit()) locator.submit()", element.getObjectId(), false);
    }
    else throw new WebDriverException("Element is not in a form, can't submit");

    parent.waitForLoadToComplete();

  }

  // FIXME revise with javascript guys
  /**
   * @deprecated To be removed. Determine the current state using {@link #isSelected()}
   */
  @Deprecated
  public boolean toggle() {
    String tagName = getTagName();
    if (!tagName.equals("INPUT") && !tagName.equals("OPTION"))
      throw new InvalidElementStateException("Cannot toggle a "+tagName+" element");

    if (tagName.equalsIgnoreCase("input") && getAttribute("type").equalsIgnoreCase("radio"))
      throw new InvalidElementStateException("You can't toggle an radio element");

    Integer id = debugger.executeScriptOnObject("return locator.parentNode",
        objectId);
    OperaWebElement parentNode = new OperaWebElement(this.parent, id);
    if (parentNode.getTagName().equalsIgnoreCase("SELECT")
        && parentNode.getAttribute("multiple") == null) {
      throw new InvalidElementStateException("You can't toggle on a regular select");
    }

    if (OperaFlags.ENABLE_CHECKS) {
      if (!isEnabled())
        throw new InvalidElementStateException("Cannot toggle disabled element");
      if (!isDisplayed())
        throw new ElementNotVisibleException("Cannot select an element that is not displayed");
    }

    if (tagName.equalsIgnoreCase("option")) return (Boolean) debugger.callFunctionOnObject(
        "locator.selected = !locator.selected; return locator.selected;",
        objectId, true);

    return (Boolean) debugger.callFunctionOnObject(
        "locator.checked = !locator.checked; return locator.checked;",
        objectId, true);
  }

  public static void sleep(long timeInMillis) {
    try {
      Thread.sleep(timeInMillis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * To be replaced by the advanced interactions API.
   * @deprecated
   */
  @Deprecated
  public void dragAndDropBy(int x, int y) {
    Point point = this.getLocation();
    execService.mouseAction(point.x, point.y, OperaMouseKeys.LEFT_DOWN);
    x = (x + point.x < 0) ? 0 : x + point.x;
    y = (y + point.y < 0) ? 0 : y + point.y;
    execService.mouseAction(x, y);
    execService.mouseAction(x, y, OperaMouseKeys.LEFT_UP);
  }

  /**
   * To be replaced by the advanced interactions API.
   * @deprecated
   */
  @Deprecated
  public void dragAndDropOn(WebElement element) {
    Point currentLocation = this.getLocation();
    Point dragPoint = element.getLocation();
    execService.mouseAction(currentLocation.x, currentLocation.y,
        OperaMouseKeys.LEFT_DOWN);
    execService.mouseAction(dragPoint.x, dragPoint.y);
    execService.mouseAction(dragPoint.x, dragPoint.y, OperaMouseKeys.LEFT_UP);
  }

  /**
   * Click top left, can be modified to click in the middle
   */
  public Point getLocation() {
    String coordinates = debugger.callFunctionOnObject(
        "locator.scrollIntoView();\n"
            + "var x = 0, y = 0;\n"
            + "if(window.frameElement) {\n"
            + "x = (window.screenLeft - window.top.screenLeft) + window.scrollX;\n"
            + "y = (window.screenTop - window.top.screenTop) + window.scrollY;\n"
            + "}\n"
            + "return (( x + locator.getBoundingClientRect().left) + ',' + ( y + locator.getBoundingClientRect().top));\n",
        objectId);
    String[] location = coordinates.split(",");
    return new Point(Integer.valueOf(location[0]), Integer.valueOf(location[1]));
  }

  public Dimension getSize() {
    if (!parent.objectIds.contains(objectId)) throw new StaleElementReferenceException(
        "You cant interact with stale elements");

    String widthAndHeight = debugger.callFunctionOnObject(
        "return (locator.getBoundingClientRect().width + ',' + locator.getBoundingClientRect().height);",
        objectId);
    String[] dimension = widthAndHeight.split(",");
    return new Dimension(Integer.valueOf(dimension[0]),
        Integer.valueOf(dimension[1]));
  }

  public boolean isDisplayed() {
    boolean isDisplayed = false;
    if (!parent.objectIds.contains(objectId)
        || callMethod("locator.parentNode") == null) throw new StaleElementReferenceException(
        "You cant interact with stale elements");
    try {
      isDisplayed = (Boolean) evaluateMethod("function d(locator) {"
          + "var el = locator;\n"
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
            // If this is a <option>, recurse up to its parent <select>
            + "return d(el.parentNode);\n"
            // Map is shown iff image that uses it is shown.
          + "} else if (el.tagName.toLowerCase() == 'map') {\n"
            + "if (!el.name) return false;\n"
            + "var image = el.ownerDocument.evaluate('//*[@usemap = \"#'+ el.name +'\"]', el.ownerDocument, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;\n"
            + "return true;"
            + "return !!image && d(image);\n"
          + "} else if (el.tagName.toLowerCase() == 'area') {\n"
            + "return d(el.parentNode);\n"
          + "}\n"
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
          + "return displayed != 'none' && visibility != 'hidden';"
          + "}"
          + "return d(locator);");
    } catch (WebDriverException ex) {
      throw new StaleElementReferenceException("This element is stale");
    }

    return isDisplayed;
  }

  /**
   * Takes a screenshot of the area this element's bounding-box covers and
   * returns the MD5 hash.
   *
   * @return A MD5 hash as a string.
   */
  public String getImageHash() {
    return getImageHash(10L);
  }

  /**
   * Takes a screenshot after {@link timeout} milliseconds of the area this
   * element's bounding-box covers and returns the MD5 hash.
   *
   * @param timeout The number of milliseconds to wait before taking the
   *          screenshot.
   * @param hashes
   * @return A MD5 hash as a string.
   */
  public String getImageHash(long timeout, String... hashes) {
    return saveScreenshot("", timeout, false, hashes);
  }

  /**
   * Take a screenshot of the area this element's bounding-box covers. Saves a
   * copy of the image to the given filename, and returns an MD5 hash of the
   * image.
   *
   * @param filename The location to save the screenshot
   * @return The MD5 hash of the screenshot
   */
  public String saveScreenshot(String filename) {
    return this.saveScreenshot(filename, 10L, true);
  }

  /**
   * Take a screenshot of the area this element covers. Saves a copy of the
   * image to the given filename.
   *
   * @param filename The location to save the screenshot
   * @param timeout The number of milliseconds to wait before taking the
   *          screenshot
   * @return The MD5 hash of the screenshot
   */
  public String saveScreenshot(String filename, long timeout) {
    return this.saveScreenshot(filename, timeout, true);
  }

  public String saveScreenshot(String filename, long timeout,
      boolean includeImage, String... hashes) {
    Canvas canvas = buildCanvas();
    ScreenShotReply reply = execService.screenWatcher(canvas, timeout,
        includeImage, hashes);
    if (includeImage && reply.getPng() != null) {
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

  /**
   * Take a screenshot of the area this element's bounding-box covers.
   * @param timeout The number of milliseconds to wait before taking the
   * screenshot
   * @param hashes A previous screenshot MD5 hash. If it matches the hash
   * of this screenshot then no image data is returned.
   */
  public ScreenShotReply saveScreenshot(long timeout, String... hashes) {
    Canvas canvas = buildCanvas();
    return execService.screenWatcher(canvas, timeout, true, hashes);
  }

  public boolean containsColor(OperaColors... colors) {
    // List<String> keys = Arrays.asList(hashes);

    Canvas canvas = buildCanvas();
    ScreenShotReply reply = execService.containsColor(canvas, 100L, colors);
    List<ColorResult> results = reply.getColorResult();
    for (ColorResult result : results) {
      if (result.getCount() > 0) return true;
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
    // Avoid internal error by making sure we have some width and height
    int w = Math.max(dimension.width, 1);
    int h = Math.max(dimension.height, 1);
    canvas.setX(x);
    canvas.setY(y);
    canvas.setHeight(h);
    canvas.setWidth(w);
    canvas.setViewPortRelative(true);
    return canvas;
  }

  public String getTagName() {
    return callMethod("return (locator.tagName);");
  }

  // TODO we only return location on screen when scrolled?
  // isnt this a duplicate method?
  public Point getLocationOnScreenOnceScrolledIntoView() {
    if (isVisible()) return getLocation();
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof OperaWebElement) {
      OperaWebElement ref = (OperaWebElement) obj;
      return (ref.getObjectId() == this.getObjectId() && ref.getRuntimeId() == this.getRuntimeId());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 42;
    result = 31 * result + objectId;
    result = 31 * result + runtimeId;
    return result;
  }

  @Override
  public String toString() {
    return "element" + hashCode();
  }

  private WebElement findSingleElement(String using, String type) {
    Integer id = debugger.executeScriptOnObject(using, objectId);
    if (id != null) {
      return new OperaWebElement(parent, id);
    }

    throw new NoSuchElementException("Cannot find element with " + type);
  }

  private final List<WebElement> findMultipleElements(String using, String type) {
    Integer id = debugger.executeScriptOnObject(using, objectId);
    if (id == null) throw new NoSuchElementException(
        "Cannot find element(s) with " + type);

    List<WebElement> elements = parent.processElements(id);

    return elements;
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
    if (using.contains(":")) {
      // has prefix
      String[] tagInfo = using.split(":");
      return findSingleElement("(function(obj) { var elements = obj.getElementsByTagName('" + tagInfo[1] + "'), element = null;" +
          "for( var i = 0; i < elements.length; i++ ) {" +
          "if( elements[i].prefix == '" + tagInfo[0] + "' ) {" +
          "element = elements[i];" +
          "break;"+
          "}" +
          "}" +
          "return element; })(locator)", "tag name");
    }
    return findSingleElement("locator.getElementsByTagName('" + using +"')[0]", "tag name");
  }

  public List<WebElement> findElementsByTagName(String using) {
    if (using.contains(":")) {// has prefix
      String[] tagInfo = using.split(":");
      return findMultipleElements("(function(obj) { var elements = obj.getElementsByTagName('" + tagInfo[1] + "'), output = [];" +
          "for( var i = 0; i < elements.length; i++ ) {" +
          "if( elements[i].prefix == '" + tagInfo[0] + "' ) {" +
          "output.push(elements[i]);" +
          "}" +
          "}" +
          "return output; })(locator)", "tag name");
    }
    return findMultipleElements("locator.getElementsByTagName('" + using +"')", "tag name");
  }


  public WebElement findElementByClassName(String using) {
    return findSingleElement("locator.getElementsByClassName('" + using
        + "')[0]", "class name");
  }

  public List<WebElement> findElementsByClassName(String using) {
    return findMultipleElements("locator.getElementsByClassName('" + using
        + "')", "class name");
  }

  public WebElement findElementByXPath(String using) {
    return findSingleElement(
        "document.evaluate('"
            + using
            + "', locator, null, XPathResult.FIRST_ORDERED_NODE_TYPE,  null).singleNodeValue;\n",
        "XPath");
  }

  public List<WebElement> findElementsByXPath(String using) {
    return findMultipleElements(
        "var result = document.evaluate(\""
            + using
            + "\", locator, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n"
            + "var elements = new Array();\n"
            + "var element = result.iterateNext();\n" + "while (element) {\n"
            + "  elements.push(element);\n"
            + "  element = result.iterateNext();\n" + "}\n" + "return elements",
        "XPath");
  }

  public WebElement findElementByName(String using) {
    return findSingleElement(
        "document.evaluate(\"descendant-or-self::*[@name='"
            + using
            + "']\",locator,null,XPathResult.ORDERED_NODE_ITERATOR_TYPE,null).iterateNext()",
        "name");
  }

  public List<WebElement> findElementsByName(String using) {
    return findMultipleElements("var result = document.evaluate"
        + "(\"descendant-or-self::*[@name='" + using + "']\""
        + ", locator, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n"
        + "var elements = new Array();\n"
        + "var element = result.iterateNext();\n" + "while (element) {\n"
        + "  elements.push(element);\n" + "  element = result.iterateNext();\n"
        + "}\n" + "return elements", "XPath");
  }

  public WebElement findElementById(String using) {
    return findSingleElement(
        "document.evaluate(\"descendant-or-self::*[@id='"
            + using
            + "']\",locator,null,XPathResult.ORDERED_NODE_ITERATOR_TYPE,null).iterateNext()",
        "id");
  }

  public List<WebElement> findElementsById(String using) {
    return findMultipleElements("var result = document.evaluate"
        + "(\"descendant-or-self::*[@id='" + using + "']\""
        + ", locator, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n"
        + "var elements = new Array();\n"
        + "var element = result.iterateNext();\n" + "while (element) {\n"
        + "  elements.push(element);\n" + "  element = result.iterateNext();\n"
        + "}\n" + "return elements", "XPath");
  }

  public WebElement findElementByCssSelector(String using) {
    return findSingleElement("locator.querySelector('" + using + "');",
        "selector");
  }

  public List<WebElement> findElementsByCssSelector(String using) {
    return findMultipleElements(
        "var results = locator.querySelectorAll('"
            + using
            + "'), returnValue = [], i=0;for(;returnValue[i]=results[i];i++); return returnValue;",
        "selector");
  }

  @Override
  protected void finalize() throws Throwable {
    debugger.releaseObject(objectId);
    super.finalize();
  }

  public Coordinates getCoordinates() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getCssValue(String property) {
    return callMethod("return getComputedStyle(locator,null).getPropertyValue('"
        + property + "');");
  }

  public WebDriver getWrappedDriver() {
    return parent;
  }
}
