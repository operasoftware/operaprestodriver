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

package com.opera.core.systems.scope;

import com.opera.core.systems.OperaDriver;
import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.RuntimeNode;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IWindowManager;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

import static com.opera.core.systems.scope.internal.OperaIntervals.SCRIPT_RETRY_INTERVAL;

public abstract class AbstractEcmascriptService extends AbstractService
    implements IEcmaScriptDebugger {

  /**
   * The number of times we've attempted to retrieve a response from an injected JavaScript sent to
   * the browser.
   */
  protected int retries;

  /**
   * Defines for how long to sleep when retrying to execute a JS command.  It will be linearly
   * increased using {@link OperaIntervals#SCRIPT_RETRY_INTERVAL}.
   */
  protected long sleepDuration;

  /**
   * The window manager we are using.
   */
  protected IWindowManager windowManager;

  /**
   * The frame our current runtime is contained in.
   */
  protected String currentFramePath;

  /**
   * The window our current runtime is contained in.
   */
  protected int activeWindowId;

  protected OperaDriver driver;

  protected RuntimeNode root;

  public AbstractEcmascriptService(ScopeServices services, String version) {
    super(services, version);
    services.setDebugger(this);
    this.windowManager = services.getWindowManager();
    resetCounters();
    currentFramePath = "_top";
  }

  public void setDriver(OperaDriver driver) {
    this.driver = driver;
  }

  /**
   * Reset the timeout and retries.  It will use the {@link OperaIntervals#SCRIPT_RETRY_INTERVAL} as
   * the default value of the sleep interval {@link #sleepDuration}.
   */
  protected void resetCounters() {
    retries = 0;
    sleepDuration = SCRIPT_RETRY_INTERVAL.getMs();
  }

  /**
   * Build the script to send with arguments.
   *
   * @param elements the web elements to send with the script as argument
   * @param script   the script to execute, can have references to argument(s)
   * @param params   params to send with the script, will be parsed in to arguments
   * @return the script to be sent to Eval command for execution
   */
  protected String buildEvalString(List<WebElement> elements, String script, Object... params) {
    String toSend;

    if (params != null && params.length > 0) {
      StringBuilder builder = new StringBuilder();

      for (Object object : params) {
        if (builder.toString().length() > 0) {
          builder.append(",");
        }

        if (object instanceof Collection<?>) {
          builder.append("[");
          Collection<?> collection = (Collection<?>) object;

          for (Object argument : collection) {
            processArgument(argument, builder, elements);
            builder.append(",");
          }

          int lastCharIndex = builder.length() - 1;
          if (builder.charAt(lastCharIndex) != '[') {
            builder.deleteCharAt(lastCharIndex);
          }

          builder.append("]");
        } else {
          processArgument(object, builder, elements);
        }
      }

      String arguments = builder.toString();
      toSend = String.format("(function(){%s})(%s)", script, arguments);
    } else {
      toSend = script;
    }

    return toSend;
  }

  protected void processArgument(Object object, StringBuilder builder, List<WebElement> elements) {
    if (object instanceof WebElement) {
      elements.add((WebElement) object);
      builder.append(String.valueOf(object));
    } else if (object instanceof String) {
      builder.append("'").append(escapeQuotes(String.valueOf(object))).append("'");
    } else if (object instanceof Integer || object instanceof Long
               || object instanceof Boolean || object instanceof Float
               || object instanceof Double) {
      builder.append(String.valueOf(object));
    } else {
      throw new IllegalArgumentException("The argument type is not supported");
    }
  }

  private String escapeQuotes(String s) {
    return s.replace("'", "\\'");
  }

  public String executeJavascript(String using) {
    return executeJavascript(using, true);
  }

  public String executeJavascript(String using, boolean responseExpected) {
    Object result = executeScript(using, responseExpected);
    return (result == null) ? null : String.valueOf(result);
  }

  protected void recover() {
    windowManager.findDriverWindow();
    activeWindowId = windowManager.getActiveWindowId();
    updateRuntime();
  }

  protected Object parseNumber(String value) {
    Number number;

    try {
      number = NumberFormat.getInstance().parse(value);
      if (number instanceof Long) {
        return number.longValue();
      } else {
        return number.doubleValue();
      }
    } catch (ParseException e) {
      throw new WebDriverException("A number result from the script can not be parsed: " +
                                   e.getMessage());
    }
  }

  protected boolean isNumber(String name) {
    boolean canParse = true;

    try {
      Integer.valueOf(name);
    } catch (NumberFormatException e) {
      canParse = false;
    }

    return canParse;
  }

  public void cleanUpRuntimes() {
    int windowId = windowManager.getActiveWindowId();
    cleanUpRuntimes(windowId);
  }

}