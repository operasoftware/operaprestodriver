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

import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ColorResult;
import com.opera.core.systems.model.OperaColor;
import com.opera.core.systems.model.ScreenShotReply;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.scope.internal.OperaColors;
import com.opera.core.systems.scope.internal.OperaFlags;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.internal.OperaKeys;
import com.opera.core.systems.scope.internal.OperaMouseKeys;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IOperaExec;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Extends the default WebElement with Opera specific methods.
 *
 * @author Deniz Turkoglu <dturkoglu@opera.com>
 */
public class OperaWebElement extends RemoteWebElement {

  private final int objectId;
  private final IEcmaScriptDebugger debugger;
  private final OperaDriver parent;
  private final int runtimeId;

  private final IOperaExec execService;

  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  public int getObjectId() {
    return objectId;
  }

  public int getRuntimeId() {
    return runtimeId;
  }

  /**
   * @param parent   driver that this element belongs to
   * @param objectId the Ecmascript object id of this element
   */
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
   * @param method the method to call
   * @return response of EcmaScript in string presentation
   */
  public final String callMethod(String method) {
    return debugger.callFunctionOnObject(method, objectId);
  }

  /**
   * Executes the given script with the element's object ID doesn't parse the response
   */
  private void executeMethod(String script) {
    debugger.callFunctionOnObject(script, objectId, false);
  }

  /**
   * Evaluates the given script with object ID and parses the result and returns the result object.
   */
  private Object evaluateMethod(String script) {
    return debugger.callFunctionOnObject(script, objectId, true);
  }

  /**
   * Right clicks on the element.
   */
  public void rightClick() {
    parent.actionHandler.rightClick(this);
  }

  /**
   * Click this element many times in the top left corner of the element.
   *
   * @param times the number of times to click
   */
  public void click(int times) {
    Point point = coordinates.getLocationInViewPort();
    execService.mouseAction(point.x, point.y, times, OperaMouseKeys.LEFT);
  }

  /**
   * Click the middle mouse button at the top left corner of the element.
   */
  public void middleClick() {
    Point point = coordinates.getLocationInViewPort();
    execService.mouseAction(point.x, point.y, OperaMouseKeys.MIDDLE);
  }

  public void click() {
    if (OperaFlags.ENABLE_CHECKS) {
      if (!isDisplayed()) {
        throw new ElementNotVisibleException("You cannot click an element that is not displayed");
      }
    }

    parent.getScopeServices().captureOperaIdle();

    // FIXME: temporary fix for toggle and setSelected deprecation
    Integer id = debugger.executeScriptOnObject("return locator.parentNode", objectId);
    OperaWebElement parentNode = new OperaWebElement(this.parent, id);

    String multiple = parentNode.getAttribute("multiple");
    if (parentNode.getTagName().equalsIgnoreCase("SELECT") &&
        multiple != null && !multiple.equals("false")) {
      toggle();
    } else if (this.getTagName().equals("OPTION")) {
      setSelected();
    } else {
      parent.actionHandler.click(this, "");
    }

    try {
      parent.waitForLoadToComplete();
    } catch (ResponseNotReceivedException e) {
      // This might be expected
      logger.fine("Response not received, returning control to user");
    }
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

    try {
      parent.waitForLoadToComplete();
    } catch (ResponseNotReceivedException e) {
      // This might be expected
      logger.fine("Response not received, returning control to user");
    }
  }

  public WebElement findElement(By by) {
    return by.findElement(this);
  }

  public List<WebElement> findElements(By by) {
    return by.findElements(this);
  }

  public String getAttribute(String attribute) {
    throwIfStale();

    if (attribute.toLowerCase().equals("value")) {
      return callMethod("if(/^input|select|option|textarea$/i.test(locator.nodeName)){" +
                        "return locator.value;" +
                        "}" +
                        "return locator.textContent;");
    } else {
      return callMethod(
          "return " + OperaAtoms.GET_ATTRIBUTE.getValue() + "(locator, '" + attribute + "')");
    }
  }

  /**
   * Get the tag name of this element.
   *
   * @return the tag name in upper-case
   */
  public String getElementName() {
    return callMethod("locator.nodeName");
  }

  public String getText() {
    return callMethod("return " + OperaAtoms.GET_TEXT.getValue() + "(locator)");
  }

  @Deprecated
  /**
   * @deprecated Use {@link #getAttribute('value')} instead
   */
  public String getValue() {
    return callMethod("if(/^input|select|option|textarea$/i.test(locator.nodeName)){" +
                      "return locator.value;" +
                      "}" +
                      "return locator.textContent;");
  }

  public boolean isDisplayed() {
    throwIfStale();
    return (Boolean) evaluateMethod("return " + OperaAtoms.IS_DISPLAYED.getValue() + "(locator)");
  }

  public boolean isEnabled() {
    throwIfStale();
    return (Boolean) evaluateMethod("return " + OperaAtoms.IS_ENABLED.getValue() + "(locator)");
  }

  public boolean isSelected() {
    throwIfStale();
    return (Boolean) evaluateMethod("return " + OperaAtoms.IS_SELECTED.getValue() + "(locator)");
  }

  /**
   * @deprecated Use {@link #isDisplayed()} instead
   */
  @Deprecated
  public boolean isVisible() {
    return isDisplayed();
  }

  public void clear() {
    if (isEnabled()) {
      if (!Boolean.valueOf(getAttribute("readonly"))) {
        executeMethod("return " + OperaAtoms.CLEAR.getValue() + "(locator)");
      }
    }
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
      boolean isDisplayed;

      while (true) {
        isDisplayed = isDisplayed();

        if (!isDisplayed && parent.hasTimeRemaining(start)) {
          sleep(OperaIntervals.EXEC_SLEEP.getValue());
        } else {
          break;
        }
      }

      if (!isDisplayed()) {
        throw new ElementNotVisibleException("You can't type on an element that is not displayed");
      }
      if (!isEnabled()) {
        throw new InvalidElementStateException("You can't type on an element that is disabled");
      }
    }

    if (getTagName().equalsIgnoreCase("input")
        && (hasAttribute("type") && getAttribute("type").equals("file"))) {
      click();
    } else {
      executeMethod("locator.focus()");
      // When focused textareas return the cursor to the last position it was at.  Inputs place the
      // cursor at the beginning, and so we need to move it to the end. We do this by pre-pending an
      // "End" key to the keys to send (in a round-about way).
      if (getTagName().equalsIgnoreCase("input")) {
        // Javascript from webdriver_session.cc in ChromeDriver
        executeMethod(
            "function(elem) {"
            + "  var doc = elem.ownerDocument || elem;"
            + "  var prevActiveElem = doc.activeElement;"
            + "  if (elem != prevActiveElem && prevActiveElem)"
            + "    prevActiveElem.blur();"
            + "  elem.focus();"
            + "  if (elem != prevActiveElem && elem.value && elem.value.length &&"
            + "      elem.setSelectionRange) {"
            + "    elem.setSelectionRange(elem.value.length, elem.value.length);"
            + "  }"
            + "  if (elem != doc.activeElement)"
            + "    throw new Error('Failed to send keys because cannot focus element');"
            + "}(locator)"
        );
      }
    }

    // This code is a bit ugly.  Because "special" keys can be sent either as an individual
    // argument, or in the middle of a string of "normal" characters, we have to loop through the
    // string and check each against a list of special keys.

    parent.getScopeServices().captureOperaIdle();
    for (CharSequence seq : keysToSend) {
      if (seq instanceof Keys) {
        String key = OperaKeys.get(((Keys) seq).name());
        // Check if this is a key we hold down, and haven't already pressed, and press, but don't
        // release it. That's done at the end of this method.
        if (holdKeys.contains(key) && !heldKeys.contains(key) && !execService.keyIsPressed(key)) {
          execService.key(key, false);
          heldKeys.add(key);
        } else if (key.equals("null")) {
          for (String hkey : heldKeys) {
            execService.key(hkey, true);
          }
        } else {
          execService.key(key);
        }
      } else if (seq.toString().equals("\n")) {
        execService.key("enter");
      } else {
        // We need to check each character to see if it is a "special" key
        for (int i = 0; i < seq.length(); i++) {
          Character c = seq.charAt(i);
          String keyName = charToKeyName(c);

          // Buffer normal keys for a single type() call
          if (keyName == null) {
            execService.type(c.toString());
          } else {
            String key = OperaKeys.get(keyName);
            // TODO: Code repeated from above.
            if (holdKeys.contains(key) && !heldKeys.contains(key) && !execService
                .keyIsPressed(key)) {
              execService.key(key, false);
              heldKeys.add(key);
            } else if (key.equals("null")) {
              for (String hkey : heldKeys) {
                execService.key(hkey, true);
              }
            } else {
              execService.key(key);
            }
          }
        }
      }
    }

    if (heldKeys.size() > 0) {
      for (String key : heldKeys) {
        execService.key(key, true);
      }
    }

    try {
      parent.waitForLoadToComplete();
    } catch (ResponseNotReceivedException e) {
      // This might be expected
      logger.fine("Response not received, returning control to user");
    }

    // executeMethod("locator.blur()");
  }

  /**
   * Stores a map of special character codes to the string representation.  For example "\uE00E"
   * maps to "page_up".
   */
  private static final HashMap<Character, String> keysLookup = new HashMap<Character, String>();

  /**
   * Converts a character in the PUA to the name of the key, as given by {@link
   * org.openqa.selenium.Keys}. If the character doesn't appear in that class then null is
   * returned.
   *
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

  /**
   * @deprecated Please use "click" instead
   */
  @Deprecated
  public void setSelected() {
    String tagName = getTagName();

    if (OperaFlags.ENABLE_CHECKS) {
      if (!isEnabled()) {
        throw new InvalidElementStateException("Cannot select disabled element");
      }

      if (!isDisplayed()) {
        throw new ElementNotVisibleException("Cannot select an element that is not displayed");
      }

      if (!tagName.equals("INPUT") && !tagName.equals("OPTION")) {
        throw new InvalidElementStateException("Cannot select a " + tagName + " element");
      }
    }

    evaluateMethod("return " + OperaAtoms.SET_SELECTED.getValue() + "(locator, true)");
  }

  public void submit() {
    parent.getScopeServices().captureOperaIdle();

    evaluateMethod("return " + OperaAtoms.SUBMIT.getValue() + "(locator)");

    try {
      parent.waitForLoadToComplete();
    } catch (ResponseNotReceivedException e) {
      // This might be expected
      logger.fine("Response not received, returning control to user");
    }
  }

  // FIXME revise with javascript guys

  /**
   * @deprecated To be removed. Determine the current state using {@link #isSelected()}
   */
  @Deprecated
  public boolean toggle() {
    String tagName = getTagName();
    if (!tagName.equals("INPUT") && !tagName.equals("OPTION")) {
      throw new InvalidElementStateException("Cannot toggle a " + tagName + " element");
    }

    if (tagName.equalsIgnoreCase("input") && getAttribute("type").equalsIgnoreCase("radio")) {
      throw new InvalidElementStateException("You can't toggle an radio element");
    }

    Integer id = debugger.executeScriptOnObject("return locator.parentNode",
                                                objectId);
    OperaWebElement parentNode = new OperaWebElement(this.parent, id);
    if (parentNode.getTagName().equalsIgnoreCase("SELECT")
        && parentNode.getAttribute("multiple") == null) {
      throw new InvalidElementStateException("You can't toggle on a regular select");
    }

    if (OperaFlags.ENABLE_CHECKS) {
      if (!isEnabled()) {
        throw new InvalidElementStateException("Cannot toggle disabled element");
      }
      if (!isDisplayed()) {
        throw new ElementNotVisibleException("Cannot select an element that is not displayed");
      }
    }

    return (Boolean) debugger.callFunctionOnObject(
        "return " + OperaAtoms.TOGGLE.getValue() + "(locator)", objectId, true);
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * To be replaced by the advanced interactions API.
   *
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
   *
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
        "var coords = " + OperaAtoms.GET_LOCATION.getValue() +
        "(locator); return coords.x + ',' + coords.y;", objectId
    );

    // FIXME: the goog.dom.getDocumentScrollElement_() function the Google
    // closure library doesn't return the document for SVG documents. This
    // is used by the above atom. In this case the coordinates string will be
    // empty, so we use this fallback to get the coordinates. Hopefully a fix
    // will be forthcoming in the closure library.
    if (coordinates.isEmpty()) {
      logger.warning("Falling back to non-atom positioning code in getLocation");
      coordinates = debugger.callFunctionOnObject(
          "var coords = locator.getBoundingClientRect();" +
          "return (coords.left-window.pageXOffset)+','+(coords.top-window.pageYOffset)",
          objectId
      );
    }

    String[] location = coordinates.split(",");
    return new Point(Integer.valueOf(location[0]), Integer.valueOf(location[1]));
  }

  public Dimension getSize() {
    if (!parent.objectIds.contains(objectId)) {
      throw new StaleElementReferenceException(
          "You cant interact with stale elements");
    }

    String widthAndHeight = debugger.callFunctionOnObject(
        "var s=" + OperaAtoms.GET_SIZE.getValue() + "(locator);return s.width+','+s.height;",
        objectId
    );
    String[] dimension = widthAndHeight.split(",");
    return new Dimension(Integer.valueOf(dimension[0]),
                         Integer.valueOf(dimension[1]));
  }

  /**
   * Takes a screenshot of the area this element's bounding-box covers and returns the MD5 hash.
   *
   * @return an MD5 hash as a string
   */
  public String getImageHash() {
    return getImageHash(10L);
  }

  /**
   * Takes a screenshot after timeout milliseconds of the area this element's bounding-box covers
   * and returns the MD5 hash.
   *
   * @param timeout the number of milliseconds to wait before taking the screenshot
   * @param hashes  optional hashes to compare the hashes with
   * @return an MD5 hash as a string
   */
  public String getImageHash(long timeout, String... hashes) {
    return saveScreenshot("", timeout, false, hashes);
  }

  /**
   * Take a screenshot of the area this element's bounding-box covers. Saves a copy of the image to
   * the given filename, and returns an MD5 hash of the image.
   *
   * @param filename The location to save the screenshot
   * @return The MD5 hash of the screenshot
   */
  public String saveScreenshot(String filename) {
    return this.saveScreenshot(filename, 10L, true);
  }

  /**
   * Take a screenshot of the area this element covers. Saves a copy of the image to the given
   * filename.
   *
   * @param filename The location to save the screenshot
   * @param timeout  The number of milliseconds to wait before taking the screenshot
   * @return The MD5 hash of the screenshot
   */
  public String saveScreenshot(String filename, long timeout) {
    return this.saveScreenshot(filename, timeout, true);
  }

  /**
   * Take a screenshot of the area this element covers. If the hash of the image matches any of the
   * given hashes then no image is saved, otherwise it saves a copy of the image to the given
   * filename.
   *
   * @param filename     The location to save the screenshot.
   * @param timeout      The number of milliseconds to wait before taking the screenshot.
   * @param includeImage Whether to get the image data. Disable if you just need the MD5 hash.
   * @param hashes       Known image hashes.
   * @return The MD5 hash of the screenshot.
   */
  public String saveScreenshot(String filename, long timeout,
                               boolean includeImage, String... hashes) {
    Canvas canvas = buildCanvas();
    ScreenShotReply reply = execService.screenWatcher(canvas, timeout,
                                                      includeImage, hashes);
    if (includeImage && reply.getPng() != null) {
      FileChannel stream;
      try {
        stream = new FileOutputStream(filename).getChannel();
        stream.write(ByteBuffer.wrap(reply.getPng()));
        stream.close();
      } catch (Exception e) {
        throw new WebDriverException("Failed to write file: " + e.getMessage());
      }
    }
    return reply.getMd5();
  }

  /**
   * Take a screenshot of the area this element's bounding-box covers.
   *
   * @param timeout The number of milliseconds to wait before taking the screenshot
   * @param hashes  A previous screenshot MD5 hash. If it matches the hash of this screenshot then
   *                no image data is returned.
   */
  public ScreenShotReply saveScreenshot(long timeout, String... hashes) {
    Canvas canvas = buildCanvas();
    return execService.screenWatcher(canvas, timeout, true, hashes);
  }

  /**
   * Check if the current webpage contains any of the given colors. Used on tests that use red to
   * show a failure.
   *
   * @param colors list of colors to check for.
   * @return true if the page contains any of the given colors, false otherwise.
   */
  public boolean containsColor(OperaColors... colors) {
    // List<String> keys = Arrays.asList(hashes);

    Canvas canvas = buildCanvas();
    ScreenShotReply reply = execService.containsColor(canvas, 100L, colors);
    List<ColorResult> results = reply.getColorResult();
    for (ColorResult result : results) {
      if (result.getCount() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Create a "canvas", which is an object that specifies a rectangle to take a screenshot of.
   *
   * @return a canvas representing the size and position of this element.
   */
  private Canvas buildCanvas() {
    Canvas canvas = new Canvas();
    Dimension dimension = getSize();
    Point point = coordinates.getLocationInViewPort();
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
    if (isDisplayed()) {
      return getLocation();
    }
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

  protected WebElement findElement(String by, String using) {
    return parent.findElement(by, using, this);
  }

  protected List<WebElement> findElements(String by, String using) {
    return parent.findElements(by, using, this);
  }

  private List<WebElement> findMultipleElements(String using, String type) {
    Integer id = debugger.executeScriptOnObject(using, objectId);

    if (id == null) {
      throw new NoSuchElementException("Cannot find element(s) with " + type);
    }

    return parent.processElements(id);
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
                                + "  elements.push(element);\n"
                                + "  element = result.iterateNext();\n"
                                + "}\n" + "return elements", "XPath");
  }

  @Override
  protected void finalize() throws Throwable {
    debugger.releaseObject(objectId);
    super.finalize();
  }

  public Coordinates getCoordinates() {
    return coordinates;
  }

  private Coordinates coordinates = new Coordinates() {
    public Point getLocationOnScreen() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public Point getLocationInViewPort() {
      String coordinates = debugger.callFunctionOnObject(
          "locator.scrollIntoView();\n"
          + "var x = 0, y = 0;\n"
          + "if(window.top !== window.self) {\n"
          + "x = (window.screenLeft - window.top.screenLeft) + window.scrollX;\n"
          + "y = (window.screenTop - window.top.screenTop) + window.scrollY;\n"
          + "}\n"
          + "return (( x + locator.getBoundingClientRect().left) + ',' + ( y + locator.getBoundingClientRect().top));\n",
          objectId);
      String[] location = coordinates.split(",");
      return new Point(Integer.valueOf(location[0]), Integer.valueOf(location[1]));
    }

    public Point getLocationInDOM() {
      return getLocation();
    }

    public Object getAuxiliry() {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  };

  public String getCssValue(String property) {
    String value = callMethod(
        "return " + OperaAtoms.GET_EFFECTIVE_STYLE.getValue() + "(locator, '" + property + "')");

    // Opera returns a colour in RGB format.  WebDriver specifies that the output from getCssValue()
    // must be in HEX format.
    if (property.contains("color")) {
      return new OperaColor(value).getHex();
    }

    return value;
  }

  public WebDriver getWrappedDriver() {
    return parent;
  }

  private void throwIfStale() {
    if (!parent.objectIds.contains(objectId) ||
        Boolean
            .valueOf(debugger.callFunctionOnObject("locator.parentNode == undefined", objectId))) {
      throw new StaleElementReferenceException("You cant interact with stale elements");
    }
  }

}
