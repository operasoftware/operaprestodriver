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
 * Implements WebDriver's {@link WebElement}, but also extends it with Opera specific methods.
 */
public class OperaWebElement extends RemoteWebElement {

  protected final Logger logger = Logger.getLogger(this.getClass().getName());

  private final int objectId;
  private final int runtimeId;
  private final OperaDriver parent;
  private final IOperaExec execService;
  private final IEcmaScriptDebugger debugger;

  /**
   * Stores a map of special character codes to the string representation. For example "\uE00E" maps
   * to "page_up".
   *
   * TODO(andreastt): Move this to OperaKeyboard?
   */
  private static final HashMap<Character, String> keysLookup = new HashMap<Character, String>();

  /**
   * @param parent   driver that this element belongs to
   * @param objectId the EcmaScript object ID of this element
   */
  public OperaWebElement(OperaDriver parent, int objectId) {
    this.parent = parent;
    this.objectId = objectId;
    parent.objectIds.add(objectId);
    debugger = parent.getScriptDebugger();
    execService = parent.getExecService();
    runtimeId = debugger.getRuntimeId();
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
   * Executes the given script with the element's object ID, but does not parse the response.
   *
   * @param script the script to execute
   */
  private void executeMethod(String script) {
    debugger.callFunctionOnObject(script, objectId, false);
  }

  /**
   * Evaluates the given script with object ID, parses the result and returns the result object.
   *
   * @param script the script to execute
   * @return a parsed result object from the executor
   */
  private Object evaluateMethod(String script) {
    return debugger.callFunctionOnObject(script, objectId, true);
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

  public void click() {
    assertElementDisplayed("Cannot click an element that is not displayed");

    parent.getScopeServices().captureOperaIdle();

    if (getTagName().equals("OPTION")) {
      assertElementEnabled("Cannot select disabled element");
      callMethod("return " + OperaAtoms.CLICK + "(locator)");
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
   * Click the element at the given X,Y offset from the top left.
   *
   * @param x the distance from the left border of the element to click
   * @param y the distance from the top border of the element to click
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

  /**
   * Click the middle mouse button at the top left corner of the element.
   */
  public void middleClick() {
    Point point = coordinates.getLocationInViewPort();
    execService.mouseAction(point.x, point.y, OperaMouseKeys.MIDDLE);
  }

  public WebElement findElement(By by) {
    return by.findElement(this);
  }

  public List<WebElement> findElements(By by) {
    return by.findElements(this);
  }

  public String getAttribute(String attribute) {
    assertElementNotStale();

    if (attribute.toLowerCase().equals("value")) {
      return callMethod("if(/^input|select|option|textarea$/i.test(locator.nodeName)){"
                        + "return locator.value;" + "}" + "return locator.textContent;");
    } else {
      return callMethod("return " + OperaAtoms.GET_ATTRIBUTE + "(locator, '" + attribute
                        + "')");
    }
  }

  private boolean hasAttribute(String attr) {
    return getAttribute(attr) != null;
  }

  public String getText() {
    assertElementNotStale();
    return callMethod("return " + OperaAtoms.GET_TEXT + "(locator)");
  }

  public boolean isDisplayed() {
    assertElementNotStale();
    return (Boolean) evaluateMethod("return " + OperaAtoms.IS_DISPLAYED + "(locator)");
  }

  public boolean isEnabled() {
    assertElementNotStale();
    return (Boolean) evaluateMethod("return " + OperaAtoms.IS_ENABLED + "(locator)");
  }

  public boolean isSelected() {
    assertElementNotStale();
    return (Boolean) evaluateMethod("return " + OperaAtoms.IS_SELECTED + "(locator)");
  }

  public void clear() {
    assertElementNotStale();

    // TODO(andreastt): Throw InvalidElementStateException

    if (isEnabled()) {
      if (!Boolean.valueOf(getAttribute("readonly"))) {
        executeMethod("return " + OperaAtoms.CLEAR + "(locator)");
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

      assertElementDisplayed("Cannot type on an element that is not displayed");
      assertElementEnabled("Cannot type on an element that is disabled");
    }

    if (getTagName().equalsIgnoreCase("input")
        && (hasAttribute("type") && getAttribute("type").equals("file"))) {
      click();
    } else {
      executeMethod("locator.focus()");
      // When focused textareas return the cursor to the last position it was at. Inputs place the
      // cursor at the beginning, and so we need to move it to the end. We do this by pre-pending an
      // "End" key to the keys to send (in a round-about way).
      if (getTagName().equalsIgnoreCase("input")) {
        // Javascript from webdriver_session.cc in ChromeDriver
        executeMethod("function(elem) {" + "  var doc = elem.ownerDocument || elem;"
                      + "  var prevActiveElem = doc.activeElement;"
                      + "  if (elem != prevActiveElem && prevActiveElem)"
                      + "    prevActiveElem.blur();"
                      + "  elem.focus();"
                      + "  if (elem != prevActiveElem && elem.value && elem.value.length &&"
                      + "      elem.setSelectionRange) {"
                      + "    elem.setSelectionRange(elem.value.length, elem.value.length);" + "  }"
                      + "  if (elem != doc.activeElement)"
                      + "    throw new Error('Failed to send keys because cannot focus element');"
                      + "}(locator)");
      }
    }

    // This code is a bit ugly. Because "special" keys can be sent either as an individual
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
            // TODO: Code repeated from above
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
   * Converts a character in the PUA to the name of the key, as given by {@link
   * org.openqa.selenium.Keys}. If the character doesn't appear in that class then null is
   * returned.
   *
   * @param c the character that may be a special key
   * @return a string containing the name of the "special" key or null
   */
  private static String charToKeyName(char c) { // TODO(andreastt): Move this to OperaKeyboard?
    if (keysLookup.isEmpty()) {
      for (Keys k : Keys.values()) {
        keysLookup.put(k.charAt(0), k.name());
      }
    }
    return keysLookup.get(c);
  }

  public void submit() {
    parent.getScopeServices().captureOperaIdle();

    evaluateMethod("return " + OperaAtoms.SUBMIT + "(locator)");

    try {
      parent.waitForLoadToComplete();
    } catch (ResponseNotReceivedException e) {
      // This might be expected
      logger.fine("Response not received, returning control to user");
    }
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Click top left, can be modified to click in the middle
   */
  public Point getLocation() {
    assertElementNotStale();

    String coordinates =
        debugger.callFunctionOnObject("var coords = " + OperaAtoms.GET_LOCATION
                                      + "(locator); return coords.x + ',' + coords.y;", objectId);

    // TODO: The goog.dom.getDocumentScrollElement_() function the Google closure library doesn't
    // return the document for SVG documents. This is used by the above atom. In this case the
    // coordinates string will be empty, so we use this fallback to get the coordinates. Hopefully
    // a fix will be forthcoming in the closure library.
    if (coordinates.isEmpty()) {
      logger.warning("Falling back to non-atom positioning code in getLocation");
      coordinates =
          debugger.callFunctionOnObject("var coords = locator.getBoundingClientRect();"
                                        + "return (coords.left-window.pageXOffset)+','+(coords.top-window.pageYOffset)",
                                        objectId);
    }

    String[] location = coordinates.split(",");
    return new Point(Integer.valueOf(location[0]), Integer.valueOf(location[1]));
  }

  public Dimension getSize() {
    assertElementNotStale();

    String widthAndHeight =
        debugger.callFunctionOnObject("var s=" + OperaAtoms.GET_SIZE
                                      + "(locator);return s.width+','+s.height;", objectId);

    String[] dimension = widthAndHeight.split(",");
    return new Dimension(Integer.valueOf(dimension[0]), Integer.valueOf(dimension[1]));
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
  public String saveScreenshot(String filename, long timeout, boolean includeImage,
                               String... hashes) {
    Canvas canvas = buildCanvas();
    ScreenShotReply reply = execService.screenWatcher(canvas, timeout, includeImage, hashes);
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
  @SuppressWarnings("unused")
  public boolean containsColor(OperaColors... colors) {
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
    assertElementNotStale();
    return callMethod("return (locator.tagName);");
  }

  // TODO: We only return location on screen when scrolled? Isn't this a duplicate method?
  public Point getLocationOnScreenOnceScrolledIntoView() {
    if (isDisplayed()) {
      return getLocation();
    }
    return null;
  }

  public int getObjectId() {
    return objectId;
  }

  public int getRuntimeId() {
    return runtimeId;
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
    return findSingleElement("document.evaluate(\"descendant-or-self::*[@name='" + using
                             + "']\",locator,null,XPathResult.ORDERED_NODE_ITERATOR_TYPE,null).iterateNext()",
                             "name");
  }

  public List<WebElement> findElementsByName(String using) {
    return findMultipleElements("var result = document.evaluate"
                                + "(\"descendant-or-self::*[@name='" + using + "']\""
                                + ", locator, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE,  null);\n"
                                + "var elements = new Array();\n"
                                + "var element = result.iterateNext();\n"
                                + "while (element) {\n" + "  elements.push(element);\n"
                                + "  element = result.iterateNext();\n" + "}\n" + "return elements",
                                "XPath");
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
      String coordinates =
          debugger
              .callFunctionOnObject(
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
      throw new UnsupportedOperationException("Not supported yet");
    }
  };

  public String getCssValue(String property) {
    String value =
        callMethod("return " + OperaAtoms.GET_EFFECTIVE_STYLE + "(locator, '" + property + "')");

    // Opera returns a colour in RGB format. WebDriver specifies that the output from getCssValue()
    // must be in HEX format.
    if (property.contains("color")) {
      return new OperaColor(value).getHex();
    }

    return value;
  }

  public WebDriver getWrappedDriver() {
    return parent;
  }

  private OperaWebElement getParent() {
    return new OperaWebElement(this.parent, debugger
        .executeScriptOnObject("return locator.parentNode", objectId));
  }

  private void assertElementDisplayed() {
    assertElementDisplayed("Cannot interact with an element that is not displayed");
  }

  private void assertElementDisplayed(String message) {
    if (OperaFlags.ENABLE_CHECKS) {
      if (!isDisplayed()) {
        throw new ElementNotVisibleException(message);
      }
    }
  }

  private void assertElementEnabled() {
    assertElementEnabled("Cannot interact with a disabled element");
  }

  private void assertElementEnabled(String message) {
    if (OperaFlags.ENABLE_CHECKS) {
      if (!isEnabled()) {
        throw new InvalidElementStateException(message);
      }
    }
  }

  private void assertElementNotStale() {
    // Has the user navigated away from the page this object belongs to?
    if (!parent.objectIds.contains(objectId)) {
      throw new StaleElementReferenceException(
          "Element appears to be stale.  Did you navigate away from the page that contained it?  "
          + "And is the current window focussed the same as the one holding this element?");
    }

    // Check if current document contains this element
    if (Boolean.valueOf(callMethod("locator.parentNode == undefined"))) {
      throw new StaleElementReferenceException(
          "The element seems to be disconnected from the DOM.  This means that the user cannot "
          + "interact with it.");
    }
  }

}