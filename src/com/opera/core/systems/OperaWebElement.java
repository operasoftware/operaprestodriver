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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import com.opera.core.systems.model.Canvas;
import com.opera.core.systems.model.ColorResult;
import com.opera.core.systems.model.ScreenCaptureReply;
import com.opera.core.systems.scope.exceptions.ResponseNotReceivedException;
import com.opera.core.systems.internal.OperaColors;
import com.opera.core.systems.scope.internal.OperaMouseKeys;
import com.opera.core.systems.scope.services.Debugger;
import com.opera.core.systems.scope.services.Exec;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.Color;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Implements WebDriver's {@link WebElement}, but also extends it with Opera specific methods.
 */
public class OperaWebElement extends RemoteWebElement implements CapturesScreen {

  private static final List<String> specialInputs =
      ImmutableList.of("datetime", "date", "month", "week", "time", "datetime-local", "range",
                       "color", "file");

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final int objectId;
  private final int runtimeId;
  private final OperaDriver parent;
  private final Exec exec;
  private final Debugger debugger;

  /**
   * @param parent   driver that this element belongs to
   * @param objectId the EcmaScript object ID of this element
   */
  public OperaWebElement(OperaDriver parent, int objectId) {
    this.parent = parent;
    this.objectId = objectId;
    parent.objectIds.add(objectId);
    debugger = parent.getDebugger();
    exec = parent.getScopeServices().getExec();
    runtimeId = debugger.getRuntimeId();
    setId(String.valueOf(hashCode()));
    setFileDetector(parent.getFileDetector());
  }

  /**
   * Calls the method and parses the result, the result must be a string
   *
   * @param method the method to call
   * @return response of EcmaScript in string presentation
   */
  public final String callMethod(String method) {
    parent.assertConnected();
    return debugger.callFunctionOnObject(method, objectId);
  }

  /**
   * Executes the given script with the element's object ID, but does not parse the response.
   *
   * @param script the script to execute
   */
  private void executeMethod(String script) {
    parent.assertConnected();
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

  // TODO(andreastt): OPDRV-199
  public void click() {
    assertElementNotStale();
    assertElementDisplayed();

    parent.getScopeServices().captureOperaIdle();

    if (getTagName().equals("OPTION")) {
      callMethod("return " + OperaAtom.CLICK + "(locator)");
    } else {
      parent.getMouse().click(getCoordinates());
    }

    try {
      parent.waitForLoadToComplete();
    } catch (ResponseNotReceivedException e) {
      // This might be expected
      logger.fine("Response not received, returning control to user");
    }
  }

  /**
   * Click the middle mouse button at the top left corner of the element.
   *
   * Will not verify whether element is available for interaction first.
   *
   * @deprecated
   */
  @Deprecated
  @SuppressWarnings("unused")
  public void middleClick() {  // TODO(andreastt): Add this to Actions
    Point point = coordinates.getLocationInViewPort();
    exec.mouseAction(point.x, point.y, OperaMouseKeys.MIDDLE);
  }

  public WebElement findElement(By by) {
    return by.findElement(this);
  }

  public List<WebElement> findElements(By by) {
    return by.findElements(this);
  }

  public String getAttribute(String attribute) {
    assertElementNotStale();

    // TODO(andreastt): Investigate whether this check is still needed
    if (attribute.equalsIgnoreCase("value")) {
      return callMethod("if(/^input|select|option|textarea$/i.test(locator.nodeName)){"
                        + "return locator.value;" + "}" + "return locator.textContent;");
    } else {
      return callMethod("return " + OperaAtom.GET_ATTRIBUTE + "(locator, '" + attribute
                        + "')");
    }
  }

  public String getText() {
    assertElementNotStale();
    return callMethod("return " + OperaAtom.GET_TEXT + "(locator)");
  }

  public boolean isDisplayed() {
    assertElementNotStale();
    return (Boolean) evaluateMethod("return " + OperaAtom.IS_DISPLAYED + "(locator)");
  }

  public boolean isEnabled() {
    assertElementNotStale();
    return (Boolean) evaluateMethod("return " + OperaAtom.IS_ENABLED + "(locator)");
  }

  public boolean isSelected() {
    assertElementNotStale();
    return (Boolean) evaluateMethod("return " + OperaAtom.IS_SELECTED + "(locator)");
  }

  public void clear() {
    verifyCanInteractWithElement();

    if (isEnabled() && !Boolean.valueOf(getAttribute("readonly"))) {
      executeMethod("return " + OperaAtom.CLEAR + "(locator)");
    }
  }

  public void sendKeys(CharSequence... keysToSend) {
    verifyCanInteractWithElement();

    // Handle special input types
    String typeAttribute = getAttribute("type").toLowerCase();

    if (getTagName().equals("INPUT") && specialInputs.contains(typeAttribute)) {
      if (typeAttribute.equals("file")) {
        File localFile = fileDetector.getLocalFile(keysToSend);

        if (localFile != null) {
          debugger.setFormElementValue(objectId, localFile.getAbsolutePath());
        }
      } else {
        debugger.setFormElementValue(objectId, Joiner.on("").join(keysToSend));
      }

      return;
    }

    parent.getScopeServices().captureOperaIdle();

    switchFocusToThisIfNeeded();
    parent.getKeyboard().sendKeys(keysToSend);

    try {
      parent.waitForLoadToComplete();
    } catch (ResponseNotReceivedException e) {
      // return control to user
    }
  }

  private void switchFocusToThisIfNeeded() {
    // TODO(andreastt): Check if element is already focused, if not click

    // When a TEXTAREA element is focused it returns the cursor to the last position was at, or
    // places it last.  INPUT @type="text" (or any other textual input element) places the caret at
    // the beginning.  Because of this we are forced to move the caret to the end of the input
    // field.  We do this by setting the selection range through JavaScript, which should move the
    // cursor to the end of the field upon the next focus event.
    String type = callMethod("locator.type");

    if (type.equals("text") || type.equals("textarea")) {
      executeMethod(OperaAtom.MOVE_CARET_TO_END + "(locator)");
    }

    executeMethod("locator.focus()");
  }

  public void submit() {
    parent.getScopeServices().captureOperaIdle();

    evaluateMethod("return " + OperaAtom.SUBMIT + "(locator)");

    try {
      parent.waitForLoadToComplete();
    } catch (ResponseNotReceivedException e) {
      // This might be expected
      logger.fine("Response not received, returning control to user");
    }
  }

  /**
   * Click top left, can be modified to click in the middle
   */
  public Point getLocation() {
    assertElementNotStale();

    String coordinates =
        debugger.callFunctionOnObject("var coords = " + OperaAtom.GET_LOCATION
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
        debugger.callFunctionOnObject("var s=" + OperaAtom.GET_SIZE
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
   * @param filename the location to save the screenshot
   * @return the MD5 hash of the screenshot
   */
  public String saveScreenshot(String filename) {
    return this.saveScreenshot(filename, 10L, true);
  }

  /**
   * Take a screenshot of the area this element covers. Saves a copy of the image to the given
   * filename.
   *
   * @param filename the location to save the screenshot
   * @param timeout  the number of milliseconds to wait before taking the screenshot
   * @return the MD5 hash of the screenshot
   */
  public String saveScreenshot(String filename, long timeout) {
    return this.saveScreenshot(filename, timeout, true);
  }

  /**
   * Take a screenshot of the area this element covers. If the hash of the image matches any of the
   * given hashes then no image is saved, otherwise it saves a copy of the image to the given
   * filename.
   *
   * @param filename     the location to save the screenshot
   * @param timeout      the number of milliseconds to wait before taking the screenshot
   * @param includeImage whether to get the image data. Disable if you just need the MD5 hash
   * @param hashes       known image hashes
   * @return the MD5 hash of the screenshot
   */
  public String saveScreenshot(String filename, long timeout, boolean includeImage,
                               String... hashes) {
    assertElementNotStale();

    Canvas canvas = buildCanvas();
    ScreenCaptureReply reply = execService.screenWatcher(canvas, timeout, includeImage, hashes);

    if (includeImage && reply.getPng() != null) {
      FileChannel stream;

      try {
        stream = new FileOutputStream(filename).getChannel();
        stream.write(ByteBuffer.wrap(reply.getPng()));
        stream.close();
      } catch (IOException e) {
        throw new WebDriverException("Failed to write file: " + e.getMessage(), e);
      }
    }

    return reply.getMd5();
  }

  public ScreenCaptureReply captureScreen() {
    return captureScreen(OperaIntervals.RUNNER_SCREEN_CAPTURE_TIMEOUT.getMs());
  }

  public ScreenCaptureReply captureScreen(long timeout) {
    return captureScreen(timeout, (String) null);
  }

  public ScreenCaptureReply captureScreen(long timeout, String... knownMD5s) {
    assertElementNotStale();
    Canvas canvas = buildCanvas();
    return exec.screenWatcher(canvas, timeout, true, knownMD5s);
  }

  /**
   * Check if the current page contains any of the given colors.  Used on tests that use red to show
   * a failure.
   *
   * @param colors list of colors to check for
   * @return true if the page contains any of the given colors, false otherwise
   * @deprecated
   */
  @SuppressWarnings("unused")
  @Deprecated
  public boolean containsColor(OperaColors... colors) {
    assertElementNotStale();

    Canvas canvas = buildCanvas();
    ScreenCaptureReply reply = execService.containsColor(canvas, 100L, colors);

    List<ColorResult> results = reply.getColorResults();

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

  protected WebElement findElement(final String by, final String using) {
    final OperaWebElement el = this;

    return parent.implicitlyWaitFor(new Callable<WebElement>() {
      public WebElement call() {
        return parent.findElement(by, using, el);
      }
    });
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
      throw new UnsupportedOperationException();
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

    public Object getAuxiliary() {
      throw new UnsupportedOperationException();
    }
  };

  public String getCssValue(String property) {
    assertElementNotStale();

    String value =
        callMethod("return " + OperaAtom.GET_EFFECTIVE_STYLE + "(locator, '" + property + "')");

    // WebDriver specifies that the output from getCssValue() must be in RGBA format
    if (property.contains("color") && value.contains("rgb")) {
      return Color.fromString(value).asRgba();
    }

    return value;
  }

  public WebDriver getWrappedDriver() {
    return parent;
  }

  private void verifyCanInteractWithElement() {
    assertElementNotStale();
    assertElementDisplayed();
    assertElementEnabled();
  }

  private void assertElementDisplayed() {
    assertElementDisplayed("You may only interact with visible elements");
  }

  private void assertElementDisplayed(String message) {
    Boolean displayed = parent.implicitlyWaitFor(new Callable<Boolean>() {
      public Boolean call() {
        return isDisplayed();
      }
    });

    if (displayed == null || !displayed) {
      throw new ElementNotVisibleException(message);
    }
  }

  private void assertElementEnabled() {
    assertElementEnabled("You may only interact with enabled elements");
  }

  private void assertElementEnabled(String message) {
    if (!isEnabled()) {
      throw new InvalidElementStateException(message);
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