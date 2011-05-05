package com.opera.core.systems.scope;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import com.opera.core.systems.ScopeServices;
import com.opera.core.systems.model.RuntimeNode;
import com.opera.core.systems.scope.internal.OperaIntervals;
import com.opera.core.systems.scope.services.IEcmaScriptDebugger;
import com.opera.core.systems.scope.services.IWindowManager;

public abstract class AbstractEcmascriptService extends AbstractService
    implements IEcmaScriptDebugger {

  /// TODO
  protected int retries;
  protected long sleepDuration;

  /// The window manager we are using.
  protected final IWindowManager windowManager;
  /// The frame our current runtime is contained in.
  protected String currentFramePath;
  /// The window our current runtime is contained in.
  protected int activeWindowId;


  protected RuntimeNode root;

  public AbstractEcmascriptService(ScopeServices services, String version) {
    super(services, version);
    services.setDebugger(this);
    this.windowManager = services.getWindowManager();
    this.services = services;
    resetCounters();
    currentFramePath = "_top";
  }

  /**
   * Reset the timeout and retries
   */
  protected void resetCounters() {
    retries = 0;
    sleepDuration = OperaIntervals.SCRIPT_RETRY_INTERVAL.getValue();
  }

  /**
   * Build the script to send with arguments
   *
   * @param elements The web elements to send with the script as argument
   * @param script The script to execute, can have references to argument(s)
   * @param params Params to send with the script, will be parsed in to
   *          arguments
   * @return The script to be sent to Eval command for execution
   */
  protected String buildEvalString(List<WebElement> elements, String script,
      Object... params) {
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
          }
          builder.append("]");
        } else {
          processArgument(object, builder, elements);
        }
      }

      String arguments = builder.toString();
      toSend = "(function(){" + script + "})(" + arguments + ")";
    } else {
      toSend = script;
    }
    return toSend;
  }

  protected void processArgument(Object object, StringBuilder builder,
      List<WebElement> elements) {
    if (object instanceof WebElement) {
      elements.add((WebElement) object);
      builder.append(String.valueOf(object));
    } else if (object instanceof String) {
      builder.append("'" + String.valueOf(object) + "'");
    } else if (object instanceof Integer || object instanceof Long
        || object instanceof Boolean || object instanceof Float
        || object instanceof Double) {
      builder.append(String.valueOf(object));
    } else {
      throw new IllegalArgumentException("The argument type is not supported");
    }
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
      if (number instanceof Long) return number.longValue();
      else return number.doubleValue();
    } catch (ParseException e) {
      throw new WebDriverException(
          "The result from the script can not be parsed");
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
